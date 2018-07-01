package featurecat.lizzie.analysis;

import featurecat.lizzie.Lizzie;
import featurecat.lizzie.rules.BoardHistoryNode;
import featurecat.lizzie.rules.BoardStateChangeObserver;
import featurecat.lizzie.util.ThreadPoolUtil;
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

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PhoenixGoAnalyzer extends AbstractGtpBasedAnalyzer {
    private ExecutorService notificationExecutor;
    private final BoardStateChangeObserver boardSyncObserver;

    public PhoenixGoAnalyzer(GtpClient gtpClient) {
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
                PhoenixGoAnalyzer.this.postGtpCommand(command);
            }
        };

        // Notify engine start
        notificationExecutor.execute(observers::engineRestarted);

        this.gtpClient.registerStderrLineConsumer(this::processEngineOutputLine);
        Lizzie.board.registerBoardStateChangeObserver(boardSyncObserver);
    }

    @Override
    protected void doStartAnalyzing() {

    }

    @Override
    protected void doStopAnalyzing() {

    }

    @Override
    protected boolean isAnalyzingOngoingAfterCommand(String command) {
        return true;
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

    private long lastBestMoveUpdatedTime = 0;

    /**
     * Process the lines in leelaz's output. Example: info move D16 visits 7 winrate 4704 pv D16 Q16 D4
     *
     * @param line an output line
     */
    private void processEngineOutputLine(String line) {
        String trimmed = StringUtils.trim(line);
        if (!StringUtils.startsWith(trimmed, "info")) {
            return;
        }

        final MutableList<MoveData> currentBestMoves = parseMoveDataLine(trimmed);
        if (CollectionUtils.isEmpty(currentBestMoves)) {
            return;
        }

        if (System.currentTimeMillis() - lastBestMoveUpdatedTime < 100) {
            return;
        }

        notificationExecutor.execute(() -> observers.bestMovesUpdated(currentBestMoves));
        lastBestMoveUpdatedTime = System.currentTimeMillis();
    }

    private static final EngineOutputLineParser parser = Parboiled.createParser(EngineOutputLineParser.class);
    private static final AbstractParseRunner<?> runner = new ReportingParseRunner(parser.EngineLine());

    public static MutableList<MoveData> parseMoveDataLine(String line) {
        ParsingResult<?> result = runner.run(line);
        if (!result.matched) {
            return null;
        }

        MutableList<?> analyzed = Lists.mutable.withAll(result.valueStack);
        analyzed.sortThis((o1, o2) -> {
            MutableMap<String, Object> d1 = (MutableMap<String, Object>) o1;
            MutableMap<String, Object> d2 = (MutableMap<String, Object>) o2;
            return Integer.parseInt((String) d2.get("ORDER")) - Integer.parseInt((String) d1.get("ORDER"));
        });
        return analyzed.stream()
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
