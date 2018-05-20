package wagner.stephanie.lizzie.analysis;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jtrim2.utils.ObjectFinalizer;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.rules.Stone;
import wagner.stephanie.lizzie.util.ArgumentTokenizer;
import wagner.stephanie.lizzie.util.ThreadPoolUtil;

import javax.swing.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * an interface with leelaz.exe go engine. Can be adapted for GTP, but is specifically designed for GCP's Leela Zero.
 * leelaz is modified to output information as it ponders
 * see www.github.com/gcp/leela-zero
 */
public class Leelaz implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(Leelaz.class);
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");

    private static final long MINUTE = 60 * 1000; // number of milliseconds in a minute

    private final ObjectFinalizer objectFinalizer;

    private ExecutorService notificationExecutor;
    private ExecutorService miscExecutor;

    private boolean readingPonderOutput;
    private volatile List<MoveData> bestMoves;
    private List<MoveData> bestMovesNext;

    /* 'thinking' is the logical state of analyzing and 'pondering' is the actual engine state. */
    private boolean thinking;
    private boolean pondering;
    private long startPonderTime;

    private boolean normalExit;

    private BestMoveObserverCollection observerCollection;

    private AtomicInteger boardStateCount;

    private GtpClient leelazEngine;

    /**
     * Initializes the leelaz process and starts reading output
     *
     */
    public Leelaz(String commandline) {
        objectFinalizer = new ObjectFinalizer(this::doCleanup, "Shutdown engine");

        observerCollection = new BestMoveObserverCollection();
        notificationExecutor = Executors.newSingleThreadExecutor();
        miscExecutor = Executors.newSingleThreadExecutor();
        boardStateCount = new AtomicInteger(0);

        startEngine(commandline);
    }

    public BestMoveObserverCollection getObserverCollection() {
        return observerCollection;
    }

    public void setObserverCollection(BestMoveObserverCollection observerCollection) {
        this.observerCollection = observerCollection;
    }

    public void registerBestMoveObserver(BestMoveObserver observer) {
        observerCollection.add(observer);

        final int currentBoardStateCount;
        final List<MoveData> currentBestMoves; // Does not need clone because we always allocate a new one

        synchronized (this) {
            currentBoardStateCount = boardStateCount.get();
            currentBestMoves = bestMoves;
        }
        notificationExecutor.execute(() -> observer.bestMovesUpdated(currentBoardStateCount, currentBestMoves));
    }

    public void unregisterBestMoveObserver(BestMoveObserver observer) {
        observerCollection.remove(observer);
    }

    public boolean isNormalExit() {
        return normalExit;
    }

    public void setNormalExit(boolean normalExit) {
        this.normalExit = normalExit;
    }

    public synchronized boolean isThinking() {
        return thinking;
    }

    public synchronized void setThinking(boolean thinking) {
        this.thinking = thinking;
    }

    public boolean isRunning() {
        return leelazEngine != null && leelazEngine.isRunning();
    }

    /**
     * Parse a line of Leelaz output
     *
     * @param line output line
     */
    private void parseLine(String line) {
        line = line.trim();
        if (StringUtils.isEmpty(line)) {
            return;
        }

        if (line.startsWith("~begin")) {
            if (System.currentTimeMillis() - startPonderTime > Lizzie.optionSetting.getMaxAnalysisTimeInMinutes() * MINUTE) {
                // we have pondered for enough time. pause pondering
                miscExecutor.execute(this::endPondering);
            }

            readingPonderOutput = true;
            bestMovesNext = new ArrayList<>(32);
        } else if (line.startsWith("~end")) {
            readingPonderOutput = false;
            bestMoves = bestMovesNext;

            final int currentBoardStateCount = boardStateCount.get();
            final List<MoveData> currentBestMoves = bestMovesNext; // Does not need clone because we always allocate a new one
            notificationExecutor.execute(() -> observerCollection.bestMovesUpdated(currentBoardStateCount, currentBestMoves));
        } else {
            if (readingPonderOutput) {
                if (Character.isLetter(line.charAt(0))) {
                    bestMovesNext.add(new MoveData(line));
                }
            } else {
                final String lineToPrint = line;
                miscExecutor.execute(() -> System.out.println(lineToPrint));
            }
        }
    }

    private void exitNotification(int exitCode) {
        if (!isNormalExit()) {
            // Prevent hang in callbacks
            new Thread(() -> JOptionPane.showMessageDialog(null, resourceBundle.getString("Leelaz.prompt.unexpectedProcessEnd"), "Lizzie", JOptionPane.ERROR_MESSAGE)).start();
        } else {
            setNormalExit(false);
        }
    }

    /**
     * Post a command for leelaz to execute, only with pondering state sync
     *
     * @param command a GTP command
     */
    public synchronized ListenableFuture<List<String>> postRawGtpCommand(String command) {
        pondering = isPonderingCommand(command);
        return leelazEngine.postCommand(command);
    }

    private boolean isPonderingCommand(String command) {
        return StringUtils.startsWithIgnoreCase(command, "time_left b 0 0");
    }

    /**
     * Post a command for leelaz to execute, with thinking and pondering state sync
     *
     * @param command a GTP command
     */
    public synchronized ListenableFuture<List<String>> postGtpCommand(String command) {
        ListenableFuture<List<String>> future = postRawGtpCommand(command);
        syncThinkingState();
        return future;
    }

    /**
     * Send a command and waiting for leelaz to execute
     *
     * @param command a GTP command
     */
    public List<String> sendGtpCommand(String command) {
        try {
            return postGtpCommand(command).get();
        } catch (InterruptedException | ExecutionException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Plays a specific move or pass
     *
     * @param color color of stone to play
     * @param move  coordinate of the coordinate
     */
    public void play(Stone color, String move) {
        String colorString;
        switch (color) {
            case BLACK:
                colorString = "B";
                break;
            case WHITE:
                colorString = "W";
                break;
            default:
                throw new IllegalArgumentException("The stone color must be BLACK or WHITE, but was " + color.toString());
        }

        ListenableFuture<List<String>> future = postGtpCommand("play " + colorString + " " + move);
        future.addListener(() -> clearBestMovesAndNotify(boardStateCount.incrementAndGet()), notificationExecutor);
    }

    public void undo() {
        ListenableFuture<List<String>> future = postGtpCommand("undo");
        future.addListener(() -> clearBestMovesAndNotify(boardStateCount.decrementAndGet()), notificationExecutor);
    }

    private void clearBestMovesAndNotify(int newBoardStateCount) {
        bestMoves = Collections.emptyList();
        observerCollection.bestMovesUpdated(newBoardStateCount, Collections.emptyList());
    }

    public void batchOperation(Runnable operation) {
        boolean originalThinkingState = thinking;

        stopThinking();
        try {
            operation.run();
        } finally {
            thinking = originalThinkingState;
            syncThinkingState();
        }
    }

    public synchronized void beginPondering() {
        postRawGtpCommand("time_left b 0 0");
        startPonderTime = System.currentTimeMillis();
    }

    public synchronized void endPondering() {
        postRawGtpCommand("name");
    }

    /**
     * this initializes leelaz's thinking mode at its current position
     */
    public synchronized void startThinking() {
        thinking = true;
        syncThinkingState();
    }

    public synchronized void stopThinking() {
        thinking = false;
        syncThinkingState();
    }

    /**
     * If it is thinking, make sure it is pondering.
     * If it is not thinking, make sure it stops pondering.
     */
    public synchronized void syncThinkingState() {
        if (thinking && !pondering) {
            beginPondering();
        } else if (!thinking && pondering) {
            endPondering();
        }
    }

    public synchronized void togglePonder() {
        if (pondering = !pondering) {
            beginPondering();
        } else {
            endPondering();
        }
    }

    public synchronized void toggleThinking() {
        if (thinking) {
            stopThinking();
        } else {
            startThinking();
        }
    }

    public synchronized void clearBoard() {
        ListenableFuture<List<String>> future = postGtpCommand("clear_board");
        future.addListener(() -> {
            boardStateCount.set(0);
            clearBestMovesAndNotify(0);
        }, notificationExecutor);
    }

    @Override
    public void close() {
        objectFinalizer.doFinalize();
    }

    private void doCleanup() {
        if (leelazEngine != null) {
            setNormalExit(true);
            leelazEngine.shutdown(60, TimeUnit.SECONDS);
        }

        if (notificationExecutor != null) {
            try {
                ThreadPoolUtil.shutdownAndAwaitTermination(notificationExecutor);
                notificationExecutor = null;
            } catch (Exception e) {
                logger.error("Cannot close notification executor.", e);
            }
        }

        if (miscExecutor != null) {
            try {
                ThreadPoolUtil.shutdownAndAwaitTermination(miscExecutor);
                miscExecutor = null;
            } catch (Exception e) {
                logger.error("Cannot close misc executor.", e);
            }
        }
    }

    private void startEngine(String commandline) {
        boardStateCount.set(0);
        normalExit = false;

        thinking = false;
        pondering = false;
        readingPonderOutput = false;

        bestMoves = Collections.emptyList();
        bestMovesNext = new ArrayList<>(32);

        // list of commands for the leelaz process
        List<String> commands = ArgumentTokenizer.tokenize(commandline.trim());

        // run leelaz
        leelazEngine = new GeneralGtpClient(commands);
        leelazEngine.registerStderrLineConsumer(this::parseLine);
        leelazEngine.registerEngineExitObserver(this::exitNotification);
        leelazEngine.start();
    }

    public void shutdownEngine(long timeout, TimeUnit timeUnit) {
        setNormalExit(true);

        leelazEngine.shutdown(timeout, timeUnit);
    }

    public void shutdownEngine() {
        shutdownEngine(60, TimeUnit.SECONDS);
    }

    public void restartEngine(String commandline) throws InterruptedException {
        shutdownEngine();

        startEngine(commandline);

        // Wait for some time for the engine start
        waitForEngineStart();

        notificationExecutor.execute(observerCollection::engineRestarted);
    }

    public void waitForEngineStart() throws InterruptedException {
        // Check for engine ready
        ListenableFuture<List<String>> future = leelazEngine.postCommand("name");
        List<String> response = null;
        try {
            response = future.get(60, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            // Do nothing
        }

        if (CollectionUtils.isEmpty(response) || !leelazEngine.isRunning()) {
            JOptionPane.showMessageDialog(null, resourceBundle.getString("Leelaz.prompt.engineNotStart"), "Lizzie", JOptionPane.ERROR_MESSAGE);
        } else {
            startThinking();
            long startTime = System.currentTimeMillis();
            while (CollectionUtils.isEmpty(bestMoves) && System.currentTimeMillis() - startTime < 10000) {
                Thread.sleep(500);
            }

            if (CollectionUtils.isEmpty(bestMoves)) {
                JOptionPane.showMessageDialog(null, resourceBundle.getString("Leelaz.prompt.engineNotCompatible"), "Lizzie", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
