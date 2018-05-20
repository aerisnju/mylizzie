package wagner.stephanie.lizzie.analysis;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface GtpClient extends ExtendedGtpCommand, AutoCloseable {
    void start();

    int shutdown(long timeout, TimeUnit timeUnit);

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
