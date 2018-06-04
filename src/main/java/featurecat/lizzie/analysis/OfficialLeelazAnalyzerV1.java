package featurecat.lizzie.analysis;

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
import featurecat.lizzie.Lizzie;
import featurecat.lizzie.rules.BoardHistoryNode;
import featurecat.lizzie.rules.BoardStateChangeObserver;
import featurecat.lizzie.util.ThreadPoolUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OfficialLeelazAnalyzerV1 extends AbstractGtpBasedAnalyzer {
    private static final long MILLISECONDS_IN_SECOND = 1000;

    private ExecutorService notificationExecutor;
    private MutableMap<String, MoveData> bestMoves;
    private long startPonderTime;
    private final BoardStateChangeObserver boardSyncObserver;

    public OfficialLeelazAnalyzerV1(GtpClient gtpClient) {
        super(gtpClient, true);

        notificationExecutor = Executors.newSingleThreadExecutor();
        bestMoves = Maps.mutable.empty();
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
                OfficialLeelazAnalyzerV1.this.postGtpCommand(command);
            }
        };

        // Notify engine start
        notificationExecutor.execute(observers::engineRestarted);
        Lizzie.board.registerBoardStateChangeObserver(boardSyncObserver);
    }

    @Override
    protected void doStartAnalyzing() {
        gtpClient.postCommand("lz-analyze 20", true, this::processEngineOutputLine).addListener(() -> {
            bestMoves = Maps.mutable.empty();
        }, notificationExecutor);

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

        MoveData moveData = parseMoveDataLine(line);
        if (moveData == null) {
            return;
        }

        if (System.currentTimeMillis() - startPonderTime > Lizzie.optionSetting.getMaxAnalysisTime() * MILLISECONDS_IN_SECOND) {
            // we have pondered for enough time. pause pondering
            notificationExecutor.execute(this::pauseAnalyzing);
        }

        bestMoves.put(moveData.getCoordinate(), moveData);

        final List<MoveData> currentBestMoves = bestMoves.toSortedList(Comparator.comparingInt(MoveData::getPlayouts).reversed());
        notificationExecutor.execute(() -> observers.bestMovesUpdated(currentBestMoves));
    }

    private static final EngineOutputLineParser parser = Parboiled.createParser(EngineOutputLineParser.class);
    private static final AbstractParseRunner<?> runner = new ReportingParseRunner(parser.EngineLine());

    public static MoveData parseMoveDataLine(String line) {
        ParsingResult<?> result = runner.run(line);
        if (!result.matched) {
            return null;
        }

        MutableMap<String, Object> data = (MutableMap<String, Object>) result.valueStack.peek();

        MutableList<String> variation = (MutableList<String>) data.get("PV");
        String coordinate = (String) data.get("MOVE");
        int playouts = Integer.parseInt((String) data.get("CALCULATION"));
        double winrate = Double.parseDouble((String) data.get("VALUE")) / 100.0;
        double probability = getRate((String) data.get("POLICY"));

        return new MoveData(coordinate, playouts, winrate, probability, variation);
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
        // info move D16 visits 7 winrate 4704 pv D16 Q16 D4
        Rule EngineLine() {
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

    public static void main(String[] args) {
        ParsingResult<?> result = runner.run("info move D16 visits 7 winrate 4704 pv D16 Q16 D4");
        System.out.println(result.matched);
        System.out.println(result.valueStack.peek());

        result = runner.run("info move d1");
        System.out.println(result.matched);

        result = runner.run("info move pass visits 1537 winrate 4704 pv pass Q16 pass D1");
        System.out.println(result.matched);
        System.out.println(result.valueStack.peek());

        result = runner.run("info move pass visits 1537 winrate 4704 N 2305 pv pass Q16 pass D1");
        System.out.println(result.matched);
        System.out.println(result.valueStack.peek());
        MutableMap<String, Object> data = (MutableMap<String, Object>) result.valueStack.peek();

        result = runner.run("info move pass visits 1537 winrate 4704 network 2305 pv pass Q16 pass D1");
        System.out.println(result.matched);
        System.out.println(result.valueStack.peek());
        System.out.println(data.equals(result.valueStack.peek()));

        result = runner.run("info move pass visits 1537 winrate 4704 network 23.05 pv pass Q16 pass D1");
        System.out.println(result.matched);
        data = (MutableMap<String, Object>) result.valueStack.peek();
        System.out.println(Double.parseDouble((String) data.getOrDefault("POLICY", "0.0")));
    }
}
