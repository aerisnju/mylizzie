package wagner.stephanie.lizzie.analysis;

import com.google.common.collect.Streams;
import org.apache.commons.collections4.IterableUtils;
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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class OfficialLeelazAnalyzer extends AbstractGtpBasedAnalyzer {
    private static final long MILLISECONDS_IN_MINUTE = 60 * 1000; // number of milliseconds in a minute

    private ExecutorService notificationExecutor;
    private MutableMap<String, MoveData> bestMoves;
    private long startPonderTime;
    private final BoardStateChangeObserver boardSyncObserver;

    public OfficialLeelazAnalyzer(GtpClient gtpClient) {
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
                OfficialLeelazAnalyzer.this.postGtpCommand(command);
            }
        };

        // Notify engine start
        notificationExecutor.execute(observers::engineRestarted);
        Lizzie.board.registerBoardStateChangeObserver(boardSyncObserver);
    }

    @Override
    protected void doStartAnalyzing() {
        gtpClient.postCommand("lz-analyze 20", this::processEngineOutputLine).addListener(() -> {
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

        if (System.currentTimeMillis() - startPonderTime > Lizzie.optionSetting.getMaxAnalysisTimeInMinutes() * MILLISECONDS_IN_MINUTE) {
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

        MutableList<String> data = Streams.stream(result.valueStack)
                .map(String::valueOf)
                .collect(Collectors.toCollection(Lists.mutable::empty))
                .reverseThis();

        MutableList<String> basicMoveData = data.subList(0, 3);
        MutableList<String> variation;
        if (data.size() >= 4) {
            variation = data.subList(3, data.size());
        } else {
            variation = Lists.mutable.empty();
        }

        String coordinate = basicMoveData.get(0);
        int playouts = Integer.parseInt(basicMoveData.get(1));
        double winrate = Double.parseDouble(basicMoveData.get(2)) / 100.0;
        double probability = 0.0;

        return new MoveData(coordinate, playouts, winrate, probability, variation);
    }

    static class EngineOutputLineParser extends BaseParser<Object> {
        // info move D16 visits 7 winrate 4704 pv D16 Q16 D4
        public Rule EngineLine() {
            return Sequence(
                    String("info")
                    , SpaceSep()
                    , String("move")
                    , SpaceSep()
                    , Move(), push(match())
                    , SpaceSep()
                    , String("visits")
                    , SpaceSep()
                    , Digits(), push(match())
                    , SpaceSep()
                    , String("winrate")
                    , SpaceSep()
                    , Digits(), push(match())
                    , SpaceSep()
                    , String("pv")
                    , ZeroOrMore(Sequence(SpaceSep(), Move(), push(match())))
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
            return Digits();
        }

        Rule Digits() {
            return OneOrMore(Digit());
        }

        Rule Digit() {
            return CharRange('0', '9');
        }

        Rule SpaceSep() {
            return OneOrMore(Space());
        }

        Rule Space() {
            return AnyOf(" \t\r\n");
        }
    }

    public static void main(String[] args) {
        ParsingResult<?> result = runner.run("info move D16 visits 7 winrate 4704 pv D16 Q16 D4");
        System.out.println(result.matched);
        MutableList<String> data = Streams.stream(result.valueStack)
                .map(String::valueOf)
                .collect(Collectors.toCollection(Lists.mutable::empty))
                .reverseThis();
        System.out.println(IterableUtils.toString(data));

        result = runner.run("info move d1");
        System.out.println(result.matched);

        result = runner.run("info move pass visits 1537 winrate 4704 pv pass Q16 pass D1");
        System.out.println(result.matched);
        data = Streams.stream(result.valueStack)
                .map(String::valueOf)
                .collect(Collectors.toCollection(Lists.mutable::empty))
                .reverseThis()
        ;
        System.out.println(IterableUtils.toString(data));
    }
}
