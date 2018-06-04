package featurecat.lizzie.analysis;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public interface Analyzer extends AutoCloseable {
    default List<String> sendGtpCommand(String command) {
        ListenableFuture<List<String>> future = postGtpCommand(command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    default void batchGtpCommands(Runnable operation) {
        boolean analyzingEnabledOriginal = isAnalyzingEnabled();
        if (analyzingEnabledOriginal) {
            disableAnalyzing();
        }

        try {
            operation.run();
        } finally {
            if (analyzingEnabledOriginal) {
                enableAnalyzing();
            }
        }
    }

    @Override
    default void close() {
        shutdown();
    }

    default void shutdown() {
        shutdown(60, TimeUnit.SECONDS);
    }

    void registerBestMoveObserver(BestMoveObserver observer);

    void unregisterBestMoveObserver(BestMoveObserver observer);

    void registerListOfBestMoveObserver(ImmutableList<BestMoveObserver> observers);

    ImmutableList<BestMoveObserver> getRegisteredBestMoveObservers();

    void clearRegisteredBestMoveObservers();

    void startAnalyzing();

    void pauseAnalyzing();

    void disableAnalyzing();

    void enableAnalyzing();

    boolean isAnalyzingOngoing();

    boolean isAnalyzingEnabled();

    ListenableFuture<List<String>> postGtpCommand(String command);

    void shutdown(long timeout, TimeUnit timeUnit);
}
