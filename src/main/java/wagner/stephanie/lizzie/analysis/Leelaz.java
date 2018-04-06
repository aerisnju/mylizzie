package wagner.stephanie.lizzie.analysis;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.rules.Stone;
import wagner.stephanie.lizzie.util.ArgumentTokenizer;
import wagner.stephanie.lizzie.util.ThreadPoolUtil;

import javax.swing.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * an interface with leelaz.exe go engine. Can be adapted for GTP, but is specifically designed for GCP's Leela Zero.
 * leelaz is modified to output information as it ponders
 * see www.github.com/gcp/leela-zero
 */
public class Leelaz implements Closeable {
    private static final Logger logger = LogManager.getLogger(Leelaz.class);
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");

    private static final long MINUTE = 60 * 1000; // number of milliseconds in a minute

    private Process process;
    private Thread leelazMonitorThread;
    private ExecutorService notificationExecutor;
    private ExecutorService miscExecutor;

    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;

    private boolean isReadingPonderOutput;
    private List<MoveData> bestMoves;
    private List<MoveData> bestMovesTemp;

    private boolean isPondering;
    private long startPonderTime;

    private boolean normalExit;

    private BestMoveObserverCollection observerCollection;

    private int boardStateCount;

    /**
     * Initializes the leelaz process and starts reading output
     *
     * @throws IOException
     */
    public Leelaz(String commandline) throws IOException {
        observerCollection = new BestMoveObserverCollection();
        notificationExecutor = Executors.newSingleThreadExecutor();
        miscExecutor = Executors.newSingleThreadExecutor();
        boardStateCount = 0;

        normalExit = false;
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

        final int currentBoardStateCount = boardStateCount;
        final List<MoveData> currentBestMoves = bestMoves; // Does not need clone because we always allocate a new one
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

    /**
     * Initializes the input and output streams
     */
    private void initializeStreams() {
        inputStream = new BufferedInputStream(process.getInputStream());
        outputStream = new BufferedOutputStream(process.getOutputStream());
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

        synchronized (this) {
            if (line.startsWith("~begin")) {
                if (System.currentTimeMillis() - startPonderTime > Lizzie.optionSetting.getMaxAnalysisTimeInMinutes() * MINUTE) {
                    // we have pondered for enough time. pause pondering
                    togglePonder();
                }

                isReadingPonderOutput = true;
                bestMovesTemp = new ArrayList<>();
            } else if (line.startsWith("~end")) {
                isReadingPonderOutput = false;
                bestMoves = bestMovesTemp;

                final int currentBoardStateCount = boardStateCount;
                final List<MoveData> currentBestMoves = bestMoves; // Does not need clone because we always allocate a new one
                notificationExecutor.execute(() -> observerCollection.bestMovesUpdated(currentBoardStateCount, currentBestMoves));
            } else {
                if (isReadingPonderOutput) {
                    if (Character.isLetter(line.charAt(0))) {
                        bestMovesTemp.add(new MoveData(line));
                    }
                } else {
                    final String lineToPrint = line;
                    miscExecutor.execute(() -> System.out.println(lineToPrint));
                }
            }
        }
    }

    /**
     * Continually reads and processes output from leelaz
     */
    private void read() {
        try {
            int c;
            StringBuilder line = new StringBuilder();
            while ((c = inputStream.read()) != -1) {
                line.append((char) c);
                if ((c == '\n')) {
                    final String lineString = line.toString();
                    miscExecutor.execute(() -> parseLine(lineString));
                    line = new StringBuilder();
                }
            }
            // this line will be reached when Leelaz shuts down
            System.out.println("Leelaz process ended.");

            shutdown();
            saveRestoreSgf();
        } catch (Exception e) {
            e.printStackTrace();
            saveRestoreSgf();
        }
    }

    public void saveRestoreSgf() {
        if (!isNormalExit()) {
            JOptionPane.showMessageDialog(null, "Waring: leelaz process is terminated unexpectedly. Please check!", "Lizzie", JOptionPane.ERROR_MESSAGE);
            Lizzie.storeGameByFile(Paths.get("restore.sgf"));
        }
    }

    /**
     * Sends a command for leelaz to execute
     *
     * @param command a GTP command containing no newline characters
     */
    public void sendCommand(String command) {
        try {
            outputStream.write((command + "\n").getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param color color of stone to play
     * @param move  coordinate of the coordinate
     */
    public void playMove(Stone color, String move) {
        synchronized (this) {
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

            sendCommand("play " + colorString + " " + move);
            bestMoves = new ArrayList<>();

            ++boardStateCount;
        }
    }

    public void undo() {
        synchronized (this) {
            sendCommand("undo");
            bestMoves = new ArrayList<>();

            --boardStateCount;
        }
    }

    /**
     * this initializes leelaz's pondering mode at its current position
     */
    public void ponder() {
        isPondering = true;
        startPonderTime = System.currentTimeMillis();
        sendCommand("time_left b 0 0");
    }

    public void togglePonder() {
        isPondering = !isPondering;
        if (isPondering) {
            ponder();
        } else {
            sendCommand("name"); // ends pondering
        }
    }

    /**
     * End the process
     */
    public void shutdown() {
        if (process.isAlive()) {
            stopPonder();
            sendCommand("quit");

            try {
                process.waitFor(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // Do nothing
            }

            if (process.isAlive()) {
                process.destroy();
            }
        }

        try {
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<MoveData> getBestMoves() {
        synchronized (this) {
            return bestMoves;
        }
    }

    public void clearBoard() {
        if (isPondering) {
            sendCommand("name"); // ends pondering
        }
        sendCommand("clear_board");
        boardStateCount = 0;
        if (isPondering) {
            ponder();
        }
    }

    public void stopPonder() {
        if (isPondering) {
            sendCommand("name"); // ends pondering
            isPondering = false;
        }
    }

    @Override
    public void close() {
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

    private void startLeelazMonitoringThread() {
        if (leelazMonitorThread == null || !leelazMonitorThread.isAlive()) {
            leelazMonitorThread = new Thread(this::read);
            leelazMonitorThread.start();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }

    private void startEngine(String commandline) throws IOException {
        isReadingPonderOutput = false;
        bestMoves = new ArrayList<>();
        bestMovesTemp = new ArrayList<>();

        isPondering = false;
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
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(new File("."));
        processBuilder.redirectErrorStream(true);
        process = processBuilder.start();

        initializeStreams();

        // start a thread to continuously read Leelaz output
        startLeelazMonitoringThread();
    }

    public void restartEngine(String commandline) throws IOException, InterruptedException {
        // Mute the message
        setNormalExit(true);

        stopPonder();
        shutdown();

        setNormalExit(false);

        startEngine(commandline);

        // Wait for some time for the engine start
        waitForEngineStart();
    }

    private void waitForEngineStart() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ponder();
        while (CollectionUtils.isEmpty(bestMoves) && System.currentTimeMillis() - startTime < 30000) {
            Thread.sleep(100);
        }

        if (CollectionUtils.isEmpty(bestMoves)) {
            JOptionPane.showMessageDialog(null, resourceBundle.getString("Leelaz.prompt.engineNotStart"), "Lizzie", JOptionPane.ERROR_MESSAGE);
        }
    }
}
