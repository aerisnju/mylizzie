package wagner.stephanie.lizzie.analysis;

import com.google.common.collect.Streams;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.rules.BoardHistoryNode;
import wagner.stephanie.lizzie.rules.BoardStateChangeObserver;
import wagner.stephanie.lizzie.util.ThreadPoolUtil;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class OfficialLeelazAnalyzerV2 extends AbstractGtpBasedAnalyzer {
    private static final long MILLISECONDS_IN_SECOND = 1000;

    private ExecutorService notificationExecutor;
    private long startPonderTime;
    private final BoardStateChangeObserver boardSyncObserver;

    public OfficialLeelazAnalyzerV2(GtpClient gtpClient) {
        super(gtpClient, true);

        notificationExecutor = Executors.newSingleThreadExecutor();
        boardSyncObserver = new BoardStateSynchronizer() {
            @Override
            public void headMoved(BoardHistoryNode oldHead, BoardHistoryNode newHead) {
                super.headMoved(oldHead, newHead);
                notificationExecutor.execute(() -> observers.bestMovesUpdated(Collections.emptyList()));
            }

            @Override
            public void boardCleared(BoardHistoryNode initialNode, BoardHistoryNode initialHead) {
                super.boardCleared(initialNode, initialHead);
                notificationExecutor.execute(() -> observers.bestMovesUpdated(Collections.emptyList()));
            }

            @Override
            protected void handleGtpCommand(String command) {
                OfficialLeelazAnalyzerV2.this.postGtpCommand(command);
            }
        };

        // Notify engine start
        notificationExecutor.execute(observers::engineRestarted);
        Lizzie.board.registerBoardStateChangeObserver(boardSyncObserver);
    }

    @Override
    protected void doStartAnalyzing() {
        gtpClient.postCommand("lz-analyze 20", true, this::processEngineOutputLine);

        startPonderTime = System.currentTimeMillis();
    }

    @Override
    protected void doStopAnalyzing() {
        gtpClient.postCommand("name");
    }

    @Override
    protected boolean isAnalyzingOngoingAfterCommand(String command) {
        return StringUtils.startsWithIgnoreCase(command, "lz-analyze")
                || StringUtils.startsWithIgnoreCase(command, "lz-genmove_analyze");
    }

    @Override
    protected void doShutdown(long timeout, TimeUnit timeUnit) {
        super.doShutdown(timeout, timeUnit);

        if (notificationExecutor != null) {
            Lizzie.board.unregisterBoardStateChangeObserver(boardSyncObserver);

            ThreadPoolUtil.shutdownAndAwaitTermination(notificationExecutor, timeout, timeUnit);
            notificationExecutor = null;
        }
    }

    /**
     * Process the lines in leelaz's output. Example: info move D16 visits 7 winrate 4704 pv D16 Q16 D4
     *
     * @param line an output line
     */
    private void processEngineOutputLine(String line) {
        if (!StringUtils.startsWith(line, "info")) {
            return;
        }

        final MutableList<MoveData> currentBestMoves = parseMoveDataLine(line);
        if (CollectionUtils.isEmpty(currentBestMoves)) {
            return;
        }

        if (System.currentTimeMillis() - startPonderTime > Lizzie.optionSetting.getMaxAnalysisTime() * MILLISECONDS_IN_SECOND) {
            // we have pondered for enough time. pause pondering
            notificationExecutor.execute(this::pauseAnalyzing);
        }

        notificationExecutor.execute(() -> observers.bestMovesUpdated(currentBestMoves));
    }

    private static final EngineOutputLineParser parser = Parboiled.createParser(EngineOutputLineParser.class);
    private static final AbstractParseRunner<?> runner = new ReportingParseRunner(parser.EngineLine());

    public static MutableList<MoveData> parseMoveDataLine(String line) {
        ParsingResult<?> result = runner.run(line);
        if (!result.matched) {
            return null;
        }

        return Streams.stream(result.valueStack)
                .map(o -> {
                    MutableMap<String, Object> data = (MutableMap<String, Object>) o;

                    MutableList<String> variation = (MutableList<String>) data.get("PV");
                    String coordinate = (String) data.get("MOVE");
                    int playouts = Integer.parseInt((String) data.get("CALCULATION"));
                    double winrate = Double.parseDouble((String) data.get("VALUE")) / 100.0;
                    double probability = getRate((String) data.get("POLICY"));

                    return new MoveData(coordinate, playouts, winrate, probability, variation);
                })
                .collect(Collectors.toCollection(Lists.mutable::empty))
                .reverseThis()
                ;
    }

    private static double getRate(String rateString) {
        if (StringUtils.isEmpty(rateString)) {
            return 0.0;
        }

        if (rateString.indexOf('.') >= 0) {
            return Double.parseDouble(rateString);
        } else {
            return Double.parseDouble(rateString) / 100.0;
        }
    }

    static class EngineOutputLineParser extends BaseParser<Object> {
        Rule EngineLine() {
            return Sequence(
                    MoveData()
                    , ZeroOrMore(
                            Sequence(Spaces(), MoveData())
                    )
            );
        }

        // info move D16 visits 7 winrate 4704 pv D16 Q16 D4
        Rule MoveData() {
            return Sequence(
                    String("info"), pushInitialValueMap()
                    , Spaces()
                    , String("move")
                    , Spaces()
                    , Move(), saveMatchToValueMap("MOVE")
                    , Spaces()
                    , String("visits")
                    , Spaces()
                    , IntNumber(), saveMatchToValueMap("CALCULATION")
                    , Spaces()
                    , String("winrate")
                    , Spaces()
                    , DoubleNumber(), saveMatchToValueMap("VALUE")
                    , Optional(
                            Spaces()
                            , FirstOf(String("network"), String("N"))
                            , Spaces()
                            , DoubleNumber(), saveMatchToValueMap("POLICY")
                    )
                    , Optional(
                            Spaces()
                            , String("order")
                            , Spaces()
                            , DoubleNumber(), saveMatchToValueMap("ORDER")
                    )
                    , Spaces()
                    , String("pv"), saveAttrToValueMap("PV", Lists.mutable.empty())
                    , ZeroOrMore(Sequence(Spaces(), Move(), pushMatchToList("PV")))
            );
        }

        Rule Move() {
            return FirstOf(Coord(), IgnoreCase("pass"));
        }

        Rule Coord() {
            return Sequence(XCoord(), YCoord());
        }

        Rule XCoord() {
            return AnyOf("ABCDEFGHJKLMNOPQRSTabcdefghjklmnopqrst");
        }

        Rule YCoord() {
            return IntNumber();
        }

        Rule DoubleNumber() {
            return Sequence(
                    Optional(AnyOf("+-")),
                    OneOrMore(Digit()),
                    Optional(Ch('.'), ZeroOrMore(Digit()))
            );
        }

        Rule IntNumber() {
            return Sequence(Optional(AnyOf("+-")), OneOrMore(Digit()));
        }

        Rule Digit() {
            return CharRange('0', '9');
        }

        Rule Spaces() {
            return OneOrMore(SpaceChar());
        }

        Rule SpaceChar() {
            return AnyOf(" \t\r\n");
        }

        boolean pushInitialValueMap() {
            MutableMap<String, Object> valueMap = Maps.mutable.empty();
            push(valueMap);

            return true;
        }

        boolean saveMatchToValueMap(String key) {
            return saveAttrToValueMap(key, match());
        }

        boolean saveAttrToValueMap(String key, Object value) {
            MutableMap<String, Object> valueMap = (MutableMap<String, Object>) peek();
            valueMap.put(key, value);

            return true;
        }

        boolean pushMatchToList(String listKey) {
            return pushToList(listKey, match());
        }

        boolean pushToList(String listKey, String value) {
            MutableMap<String, Object> valueMap = (MutableMap<String, Object>) peek();
            MutableList<String> list = (MutableList<String>) valueMap.get(listKey);
            list.add(value);

            return true;
        }
    }
}
