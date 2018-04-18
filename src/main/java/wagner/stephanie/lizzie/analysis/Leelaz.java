package wagner.stephanie.lizzie.analysis;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * an interface with leelaz.exe go engine. Can be adapted for GTP, but is specifically designed for GCP's Leela Zero.
 * leelaz is modified to output information as it ponders
 * see www.github.com/gcp/leela-zero
 */
public class Leelaz implements Closeable {
    private static final Logger logger = LogManager.getLogger(Leelaz.class);
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");

    private static final long MINUTE = 60 * 1000; // number of milliseconds in a minute

    private ExecutorService notificationExecutor;
    private ExecutorService miscExecutor;

    private boolean readingPonderOutput;
    private List<MoveData> bestMoves;
    private List<MoveData> bestMovesTemp;

    private boolean pondering;
    private boolean ponderingTempStop;
    private long startPonderTime;

    private boolean normalExit;

    private BestMoveObserverCollection observerCollection;

    private AtomicInteger boardStateCount;

    private GtpClient leelazEngine;

    /**
     * Initializes the leelaz process and starts reading output
     *
     * @throws IOException if any exception
     */
    public Leelaz(String commandline) throws IOException {
        observerCollection = new BestMoveObserverCollection();
        notificationExecutor = Executors.newSingleThreadExecutor();
        miscExecutor = Executors.newSingleThreadExecutor();
        boardStateCount = new AtomicInteger(0);

        normalExit = false;
        ponderingTempStop = false;
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

    public synchronized boolean isPondering() {
        return pondering;
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
                tempStopPonder();
            }

            readingPonderOutput = true;
            bestMovesTemp = new ArrayList<>(32);
        } else if (line.startsWith("~end")) {
            readingPonderOutput = false;
            synchronized (this) {
                bestMoves = bestMovesTemp;
            }
            final int currentBoardStateCount = boardStateCount.get();
            final List<MoveData> currentBestMoves = bestMovesTemp; // Does not need clone because we always allocate a new one
            notificationExecutor.execute(() -> observerCollection.bestMovesUpdated(currentBoardStateCount, currentBestMoves));

        } else {
            if (readingPonderOutput) {
                if (Character.isLetter(line.charAt(0))) {
                    bestMovesTemp.add(new MoveData(line));
                }
            } else {
                final String lineToPrint = line;
                miscExecutor.execute(() -> System.out.println(lineToPrint));
            }
        }
    }

    public void exitNotification(int exitCode) {
        if (!isNormalExit()) {
            // Prevent hang in callbacks
            miscExecutor.execute(() -> JOptionPane.showMessageDialog(null, resourceBundle.getString("Leelaz.prompt.unexpectedProcessEnd"), "Lizzie", JOptionPane.ERROR_MESSAGE));
        } else {
            setNormalExit(false);
        }
    }

    public ListenableFuture<List<String>> postRawGtpCommand(String command) {
        return leelazEngine.postCommand(command);
    }

    /**
     * Post a command for leelaz to execute
     *
     * @param command a GTP command
     */
    public ListenableFuture<List<String>> postGtpCommand(String command) {
        ListenableFuture<List<String>> future = postRawGtpCommand(command);
        syncPonderingStartState();
        return future;
    }

    public List<String> sendRawGtpCommand(String command) {
        return leelazEngine.sendCommand(command);
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
        future.addListener(() -> {
            boardStateCount.incrementAndGet();
            clearBestMovesAndNotify();
        }, notificationExecutor);
    }

    public void undo() {
        ListenableFuture<List<String>> future = postGtpCommand("undo");
        future.addListener(() -> {
            boardStateCount.decrementAndGet();
            clearBestMovesAndNotify();
        }, notificationExecutor);
    }

    private void clearBestMovesAndNotify() {
        bestMoves = Collections.emptyList();
        observerCollection.bestMovesUpdated(boardStateCount.get(), Collections.emptyList());
    }

    public AutoCloseable batchOperation() {
        return new AutoCloseable() {
            private boolean ponderingState = pondering;
            {
                stopPonder();
            }

            @Override
            public void close() throws Exception {
                if (ponderingState) {
                    startPonder();
                }
            }
        };
    }

    /**
     * this initializes leelaz's pondering mode at its current position
     */
    public synchronized void forceStartPonder() {
        pondering = true;
        ponderingTempStop = false;
        startPonderTime = System.currentTimeMillis();
        postRawGtpCommand("time_left b 0 0");
    }

    public synchronized void tempStopPonder() {
        postRawGtpCommand("name");
        ponderingTempStop = true;
    }

    public synchronized void forceStopPonder() {
        postRawGtpCommand("name"); // ends pondering
        pondering = false;
        ponderingTempStop = false;
    }

    public synchronized void startPonder() {
        if (!pondering || ponderingTempStop) {
            forceStartPonder();
        }
    }

    public synchronized void stopPonder() {
        if (pondering) {
            forceStopPonder();
        }
    }

    public synchronized void syncPonderingState() {
        if (pondering) {
            forceStartPonder();
        } else {
            forceStopPonder();
        }
    }

    public synchronized void syncPonderingStartState() {
        if (pondering) {
            forceStartPonder();
        }
    }

    public synchronized void syncPonderingStopState() {
        if (!pondering) {
            forceStopPonder();
        }
    }

    public synchronized void togglePonder() {
        pondering = !pondering;
        if (pondering) {
            forceStartPonder();
        } else {
            forceStopPonder();
        }
    }

    public synchronized void clearBoard() {
        ListenableFuture<List<String>> future = postGtpCommand("clear_board");
        future.addListener(() -> {
            boardStateCount.set(0);
            clearBestMovesAndNotify();
        }, notificationExecutor);
    }

    @Override
    public void close() {
        if (leelazEngine != null) {
            setNormalExit(true);
            leelazEngine.shutdown();
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

    @Override
    protected void finalize() throws Throwable {
        close();
    }

    private void startEngine(String commandline) throws IOException {
        readingPonderOutput = false;
        bestMoves = Collections.emptyList();
        bestMovesTemp = new ArrayList<>(32);

        pondering = false;
        startPonderTime = System.currentTimeMillis();

        // list of commands for the leelaz process
        List<String> commands = new ArrayList<>();
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") > 0) {
            commands.add("leelaz.exe"); // windows
        } else {
            commands.add("./leelaz"); // linux, macosx
        }

        commands.addAll(ArgumentTokenizer.tokenize(commandline.trim()));

        // run leelaz.exe
        leelazEngine = new GeneralGtpClient(commands);
        leelazEngine.registerStderrLineConsumer(this::parseLine);
        leelazEngine.registerEngineExitObserver(this::exitNotification);
        leelazEngine.start();
    }

    public void restartEngine(String commandline) throws IOException, InterruptedException {
        setNormalExit(true);
        leelazEngine.shutdown();

        startEngine(commandline);

        // Wait for some time for the engine start
        waitForEngineStart();

        notificationExecutor.execute(observerCollection::engineRestarted);
    }

    private void waitForEngineStart() throws InterruptedException {
        long startTime = System.currentTimeMillis();

        forceStartPonder();

        while (CollectionUtils.isEmpty(bestMoves) && System.currentTimeMillis() - startTime < 30000) {
            Thread.sleep(250);
        }

        if (CollectionUtils.isEmpty(bestMoves)) {
            JOptionPane.showMessageDialog(null, resourceBundle.getString("Leelaz.prompt.engineNotStart"), "Lizzie", JOptionPane.ERROR_MESSAGE);
        }
    }
}
