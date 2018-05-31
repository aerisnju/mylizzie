package wagner.stephanie.lizzie.analysis;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.impl.factory.Lists;
import org.jetbrains.annotations.NotNull;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.rules.BoardHistoryNode;
import wagner.stephanie.lizzie.rules.BoardStateChangeObserver;
import wagner.stephanie.lizzie.util.ThreadPoolUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClassicModifiedLeelazAnalyzer extends AbstractGtpBasedAnalyzer {
    private static final long MILLISECONDS_IN_MINUTE = 60 * 1000; // number of milliseconds in a minute

    private ExecutorService notificationExecutor;
    private boolean readingPonderOutput;
    private List<MoveData> bestMoves;
    private long startPonderTime;
    private BoardStateChangeObserver boardSyncObserver;

    public ClassicModifiedLeelazAnalyzer(GtpClient gtpClient) {
        super(gtpClient, true);

        notificationExecutor = Executors.newSingleThreadExecutor();
        readingPonderOutput = false;
        bestMoves = Lists.mutable.withInitialCapacity(32);
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
                ClassicModifiedLeelazAnalyzer.this.postGtpCommand(command);
            }
        };

        // Notify engine start
        notificationExecutor.execute(observers::engineRestarted);

        this.gtpClient.registerStderrLineConsumer(this::processEngineOutputLine);
        Lizzie.board.registerBoardStateChangeObserver(boardSyncObserver);
    }

    @Override
    protected void doStartAnalyzing() {
        gtpClient.postCommand("time_left b 0 0");

        startPonderTime = System.currentTimeMillis();
    }

    @Override
    protected void doStopAnalyzing() {
        gtpClient.postCommand("name");
    }

    @Override
    protected boolean isAnalyzingOngoingAfterCommand(String command) {
        return StringUtils.startsWithIgnoreCase(command, "time_left b 0 0");
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
     * Process the lines in a classic modified leelaz's output
     *
     * @param line an output line
     */
    private void processEngineOutputLine(String line) {
        line = line.trim();
        if (StringUtils.isEmpty(line)) {
            return;
        }

        if (line.startsWith("~begin")) {
            if (System.currentTimeMillis() - startPonderTime > Lizzie.optionSetting.getMaxAnalysisTimeInMinutes() * MILLISECONDS_IN_MINUTE) {
                // we have pondered for enough time. pause pondering
                notificationExecutor.execute(this::pauseAnalyzing);
            }

            readingPonderOutput = true;
            bestMoves = Lists.mutable.withInitialCapacity(32);
        } else if (line.startsWith("~end")) {
            readingPonderOutput = false;

            final List<MoveData> currentBestMoves = bestMoves; // Does not need clone because we always allocate a new one
            notificationExecutor.execute(() -> observers.bestMovesUpdated(currentBestMoves));
        } else {
            if (readingPonderOutput) {
                if (Character.isLetter(line.charAt(0))) {
                    bestMoves.add(parseMoveDataLine(line));
                }
            }
        }
    }

    @NotNull
    public static MoveData parseMoveDataLine(String line) {
        String[] data = line.trim().split(" +");
        String coordinate = data[0];
        int playouts = Integer.parseInt(data[2]);
        double winrate = Double.parseDouble(data[4].substring(0, data[4].length() - 2));
        double probability = Double.parseDouble(data[6].substring(0, data[6].length() - 2));
        List<String> variation = Arrays.asList(data).subList(8, data.length);

        return new MoveData(coordinate, playouts, winrate, probability, variation);
    }
}
