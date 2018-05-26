package wagner.stephanie.lizzie.analysis;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import wagner.stephanie.lizzie.util.GenericLizzieException;

import javax.swing.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * an interface with leelaz.exe go engine. Can be adapted for GTP, but is specifically designed for GCP's Leela Zero.
 * leelaz is modified to output information as it ponders
 * see www.github.com/gcp/leela-zero
 */
public class Leelaz implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(Leelaz.class);
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("wagner.stephanie.lizzie.i18n.GuiBundle");

    private AbstractGtpBasedAnalyzer analyzer;
    private boolean normalExit;
    private String commandLine;
    private ImmutableList<BestMoveObserver> registeredBestMoveObservers;

    /**
     * Initializes the leelaz process and starts reading output
     */
    public Leelaz(String commandline) {
        analyzer = null;
        this.commandLine = commandline;
        registeredBestMoveObservers = Lists.immutable.empty();
    }

    public boolean isNormalExit() {
        return normalExit;
    }

    public void setNormalExit(boolean normalExit) {
        this.normalExit = normalExit;
    }

    private void exitNotification(int exitCode) {
        if (!isNormalExit()) {
            // Prevent hang in callbacks
            new Thread(() -> JOptionPane.showMessageDialog(null, resourceBundle.getString("Leelaz.prompt.unexpectedProcessEnd"), "Lizzie", JOptionPane.ERROR_MESSAGE)).start();
        } else {
            setNormalExit(false);
        }
    }

    public void togglePonder() {
        if (analyzer == null) {
            return;
        }

        boolean newPonderState = !analyzer.isAnalyzingOngoing();
        if (newPonderState) {
            if (!analyzer.isAnalyzingEnabled()) {
                analyzer.enableAnalyzing();
            } else {
                analyzer.startAnalyzing();
            }
        } else {
            analyzer.pauseAnalyzing();
        }
    }

    public void toggleThinking() {
        if (analyzer == null) {
            return;
        }

        boolean newThinkingState = !analyzer.isAnalyzingEnabled();
        if (newThinkingState) {
            analyzer.enableAnalyzing();
        } else {
            analyzer.disableAnalyzing();
        }
    }

    @Override
    public void close() {
        if (analyzer != null) {
            setNormalExit(true);
            analyzer.shutdown(60, TimeUnit.SECONDS);
            analyzer = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void startEngine() {
        setNormalExit(false);

        // Create gtp client
        GtpClient analyzeGtpClient = new GtpClientBuilder()
                .setCommandLine(commandLine)
                .build();

        Consumer<Integer> exitListener = this::exitNotification;
        analyzeGtpClient.registerEngineExitObserver(exitListener);

        try {
            analyzeGtpClient.start();

            analyzer = new GtpBasedAnalyzerBuilder()
                    .setGtpClient(analyzeGtpClient)
                    .build();
            analyzer.registerListOfBestMoveObserver(registeredBestMoveObservers);
        } catch (GenericLizzieException e) {
            String reason = String.valueOf(e.get(GtpBasedAnalyzerBuilder.REASON));
            switch (reason) {
                case GtpBasedAnalyzerBuilder.ENGINE_NOT_FUNCTION:
                    JOptionPane.showMessageDialog(null, resourceBundle.getString("Leelaz.prompt.engineNotStart"), "Lizzie", JOptionPane.ERROR_MESSAGE);
                    break;
                case GtpBasedAnalyzerBuilder.ENGINE_NOT_SUPPORTED:
                    JOptionPane.showMessageDialog(null, resourceBundle.getString("Leelaz.prompt.engineNotCompatible"), "Lizzie", JOptionPane.ERROR_MESSAGE);
                    break;
                default:
                    JOptionPane.showMessageDialog(null, resourceBundle.getString("Leelaz.prompt.engineNotStart"), "Lizzie", JOptionPane.ERROR_MESSAGE);
                    break;
            }

            analyzeGtpClient.unregisterEngineExitObserver(exitListener);
            analyzeGtpClient.shutdown(60, TimeUnit.SECONDS);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(null, resourceBundle.getString("Leelaz.prompt.engineNotStart"), "Lizzie", JOptionPane.ERROR_MESSAGE);
            analyzeGtpClient.unregisterEngineExitObserver(exitListener);
            analyzeGtpClient.shutdown(60, TimeUnit.SECONDS);
        }
    }

    public void shutdownEngine(long timeout, TimeUnit timeUnit) {
        setNormalExit(true);

        if (analyzer != null) {
            analyzer.shutdown(timeout, timeUnit);
            analyzer = null;
        }
    }

    public void shutdownEngine() {
        shutdownEngine(60, TimeUnit.SECONDS);
    }

    public void restartEngine(String commandLine) {
        shutdownEngine();

        this.commandLine = commandLine;
        startEngine();
        setThinking(true);
    }

    public void restartEngine() {
        shutdownEngine();

        startEngine();
        setThinking(true);
    }

    public void registerBestMoveObserver(BestMoveObserver observer) {
        if (analyzer != null) {
            analyzer.registerBestMoveObserver(observer);
        }

        registeredBestMoveObservers = registeredBestMoveObservers.newWith(observer);
    }

    public void unregisterBestMoveObserver(BestMoveObserver observer) {
        if (analyzer != null) {
            analyzer.unregisterBestMoveObserver(observer);
        }
        registeredBestMoveObservers = registeredBestMoveObservers.newWithout(observer);
    }

    public void setThinking(boolean newState) {
        if (analyzer == null) {
            return;
        }

        if (newState) {
            analyzer.enableAnalyzing();
        } else {
            analyzer.disableAnalyzing();
        }
    }

    public void batchGtpCommands(Runnable operation) {
        if (analyzer != null) {
            analyzer.batchGtpCommands(operation);
        }
    }

    public ListenableFuture<List<String>> postGtpCommand(String command) {
        if (analyzer != null) {
            return analyzer.postGtpCommand(command);
        }

        return null;
    }
}
