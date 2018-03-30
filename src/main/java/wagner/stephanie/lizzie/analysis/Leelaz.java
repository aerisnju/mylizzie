package wagner.stephanie.lizzie.analysis;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * an interface with leelaz.exe go engine. Can be adapted for GTP, but is specifically designed for GCP's Leela Zero.
 * leelaz is modified to output information as it ponders
 * see www.github.com/gcp/leela-zero
 */
public class Leelaz implements Closeable {
    private static final Logger logger = LogManager.getLogger(Leelaz.class);

    private static final long MINUTE = 60 * 1000; // number of milliseconds in a minute
    private static final long MAX_PONDER_TIME_MILLIS = 15 * MINUTE;

    private Process process;
    private ExecutorService notificationExecutor;

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
        boardStateCount = 0;

        normalExit = false;
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
        new Thread(this::read).start();
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
        if (line.isEmpty()) {
            return;
        }

        synchronized (this) {
            if (line.startsWith("~begin")) {
                if (System.currentTimeMillis() - startPonderTime > MAX_PONDER_TIME_MILLIS) {
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
                if (Character.isAlphabetic(line.charAt(0)) && isReadingPonderOutput) {
                    bestMovesTemp.add(new MoveData(line));
                } else {
                    System.out.println(line);
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
                    parseLine(line.toString());
                    line = new StringBuilder();
                }
            }
            // this line will be reached when Leelaz shuts down
            System.out.println("Leelaz process ended.");

            shutdown();

            cleanupAndExit();
        } catch (Exception e) {
            e.printStackTrace();
            cleanupAndExit();
        }
    }

    private void cleanupAndExit() {
        if (!isNormalExit()) {
            JOptionPane.showMessageDialog(null, "Waring: leelaz process is terminated unexpectedly. Please check!", "Lizzie", JOptionPane.ERROR_MESSAGE);
            Lizzie.storeGameByFile(Paths.get("restore.sgf"));
            close();
        }

        System.exit(isNormalExit() ? 0 : -1);
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
        process.destroy();
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
                logger.error("Cannot close.", e);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }
}
