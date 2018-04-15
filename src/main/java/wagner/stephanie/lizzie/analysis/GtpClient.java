package wagner.stephanie.lizzie.analysis;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public interface GtpClient extends Closeable {
    default List<String> sendCommand(String command) {
        ListenableFuture<List<String>> commandFuture = postCommand(command);
        try {
            return commandFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            return Collections.emptyList();
        }
    }

    ListenableFuture<List<String>> postCommand(String command);

    void start();

    int shutdown();

    boolean isRunning();

    void registerStdoutLineConsumer(Consumer<String> consumer);

    void unregisterStdoutLineConsumer(Consumer<String> consumer);

    void registerStderrLineConsumer(Consumer<String> consumer);

    void unregisterStderrLineConsumer(Consumer<String> consumer);

    void registerEngineStartedObserver(Consumer<Integer> observer);

    void unregisterEngineStartedObserver(Consumer<Integer> observer);

    void registerEngineExitObserver(Consumer<Integer> observer);

    void unregisterEngineExitObserver(Consumer<Integer> observer);
}
