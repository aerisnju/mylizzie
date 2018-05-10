package wagner.stephanie.lizzie.analysis;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class GeneralGtpFuture implements ListenableFuture<List<String>> {
    private CountDownLatch countDownLatch;
    private String command;
    private List<String> response;
    private GeneralGtpClient gtpClient;
    private boolean triedCancelling;
    private boolean cancelled;
    private boolean normalCompleted;
    private List<ImmutablePair<Runnable, Executor>> listenerList;

    public GeneralGtpFuture(String command, GeneralGtpClient gtpClient) {
        countDownLatch = new CountDownLatch(1);
        this.command = command;
        this.gtpClient = gtpClient;
        response = new ArrayList<>();
        triedCancelling = false;
        cancelled = false;
        normalCompleted = false;
        listenerList = new LinkedList<>();
    }

    public String getCommand() {
        return command;
    }

    public List<String> getResponse() {
        return response;
    }

    public synchronized void markComplete() {
        normalCompleted = true;
        countDownLatch.countDown();

        notifyCompleted();
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (!triedCancelling) {
            cancelled = gtpClient.removeCommandFromStagineQueue(this);
            triedCancelling = true;

            if (cancelled) {
                notifyCompleted();
            }
        }

        return cancelled;
    }

    private void notifyCompleted() {
        listenerList.forEach(pair -> pair.getRight().execute(pair.getLeft()));
        listenerList.clear();
    }

    @Override
    public synchronized boolean isCancelled() {
        return cancelled;
    }

    @Override
    public synchronized boolean isDone() {
        return normalCompleted || cancelled;
    }

    @Override
    public List<String> get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return response;
    }

    @Override
    public List<String> get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (countDownLatch.await(timeout, unit)) {
            return response;
        } else {
            throw new TimeoutException("Timeout when waiting for command " + getCommand());
        }
    }

    @Override
    public synchronized void addListener(@NotNull Runnable listener, @NotNull Executor executor) {
        if (isDone()) {
            executor.execute(listener);
        } else {
            listenerList.add(ImmutablePair.of(listener, executor));
        }
    }
}
