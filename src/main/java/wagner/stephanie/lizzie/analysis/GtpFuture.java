package wagner.stephanie.lizzie.analysis;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Executor;

public interface GtpFuture extends ListenableFuture<List<String>> {
    default void addCompletionListener(Runnable listener, Executor executor) {
        addListener(listener, executor);
    }

    void addStartedListener(Runnable listener, Executor executor);
}
