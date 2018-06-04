package featurecat.lizzie.analysis;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface GtpClient extends ExtendedGtpCommand, AutoCloseable {
    void start();

    int shutdown(long timeout, TimeUnit timeUnit);

    boolean isRunning();

    boolean isShutdown();

    void registerStdoutLineConsumer(Consumer<String> consumer);

    void unregisterStdoutLineConsumer(Consumer<String> consumer);

    void registerStderrLineConsumer(Consumer<String> consumer);

    void unregisterStderrLineConsumer(Consumer<String> consumer);

    void registerEngineStartedObserver(Consumer<Integer> observer);

    void unregisterEngineStartedObserver(Consumer<Integer> observer);

    void registerEngineExitObserver(Consumer<Integer> observer);

    void unregisterEngineExitObserver(Consumer<Integer> observer);

    void registerGtpCommandObserver(Consumer<String> observer);

    void unregisterGtpCommandObserver(Consumer<String> observer);
}
