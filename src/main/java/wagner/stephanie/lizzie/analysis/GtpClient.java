package wagner.stephanie.lizzie.analysis;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface GtpClient extends AutoCloseable {
    default List<String> sendCommand(String command) {
        ListenableFuture<List<String>> commandFuture = postCommand(command);
        try {
            return commandFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            return Collections.emptyList();
        }
    }

    default ListenableFuture<List<String>> postCommand(String command) {
        return postCommand(command, null);
    }

    ListenableFuture<List<String>> postCommand(String command, Consumer<String> continuousOutputConsumer);

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

    static boolean isSuccessfulResponse(List<String> response) {
        boolean result = false;

        if (CollectionUtils.isNotEmpty(response)) {
            String beginElement = response.get(0);
            if (beginElement.startsWith("= ")) {
                result = true;
            }
        }

        return result;
    }

    static void removeResponseHeader(List<String> response) {
        if (CollectionUtils.isNotEmpty(response)) {
            String beginElement = response.get(0);
            if (beginElement.startsWith("= ") || beginElement.startsWith("? ")) {
                beginElement = beginElement.substring(2);
                response.set(0, beginElement);
            }
        }
    }

    static MutableIntList parseResponseIntTable(List<String> response) {
        MutableIntList result = new IntArrayList();
        for (String lineString : response) {
            String[] line = lineString.split("\\s+");
            for (String lineItemString : line) {
                try {
                    int lineItem = Integer.parseInt(lineItemString);
                    result.add(lineItem);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }

        return result;
    }

    static MutableDoubleList parseResponseDoubleTable(List<String> response) {
        MutableDoubleList result = new DoubleArrayList();
        for (String lineString : response) {
            String[] line = lineString.split("\\s+");
            for (String lineItemString : line) {
                try {
                    double lineItem = Double.parseDouble(lineItemString);
                    result.add(lineItem);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }

        return result;
    }
}
