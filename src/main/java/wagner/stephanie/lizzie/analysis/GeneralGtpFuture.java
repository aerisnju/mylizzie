package wagner.stephanie.lizzie.analysis;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GeneralGtpFuture implements GtpFuture {
    private final CountDownLatch completionLatch;
    private final String command;
    private final boolean continuous;
    private List<String> response;
    private GeneralGtpClient gtpClient;
    private boolean triedCancelling;
    private boolean cancelled;
    private boolean normalCompleted;
    private boolean started;
    private MutableList<ImmutablePair<Runnable, Executor>> completedListenerList;
    private MutableList<ImmutablePair<Runnable, Executor>> startedListenerList;

    public GeneralGtpFuture(String command, GeneralGtpClient gtpClient, boolean continuous) {
        completionLatch = new CountDownLatch(1);
        this.command = command;
        this.continuous = continuous;
        this.gtpClient = gtpClient;
        response = new ArrayList<>();
        triedCancelling = false;
        cancelled = false;
        normalCompleted = false;
        started = false;
        completedListenerList = Lists.mutable.empty();
        startedListenerList = Lists.mutable.empty();
    }

    public GeneralGtpFuture(String command, GeneralGtpClient gtpClient) {
        this(command, gtpClient, false);
    }

    public String getCommand() {
        return command;
    }

    public List<String> getResponse() {
        return response;
    }

    public boolean isContinuous() {
        return continuous;
    }

    synchronized void markCompleted() {
        normalCompleted = true;
        completionLatch.countDown();

        notifyCompleted();
    }

    synchronized void markStarted() {
        started = true;

        notifyStarted();
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
        completedListenerList.forEach(pair -> pair.getRight().execute(pair.getLeft()));
        completedListenerList.clear();
    }

    private void notifyStarted() {
        startedListenerList.forEach(pair -> pair.getRight().execute(pair.getLeft()));
        startedListenerList.clear();
    }

    @Override
    public synchronized boolean isCancelled() {
        return cancelled;
    }

    @Override
    public synchronized boolean isDone() {
        return normalCompleted || cancelled;
    }

    public synchronized boolean isStarted() {
        return started;
    }

    @Override
    public List<String> get() throws InterruptedException, ExecutionException {
        completionLatch.await();
        return response;
    }

    @Override
    public List<String> get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (completionLatch.await(timeout, unit)) {
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
            completedListenerList.add(ImmutablePair.of(listener, executor));
        }
    }

    @Override
    public synchronized void addStartedListener(@NotNull Runnable listener, @NotNull Executor executor) {
        if (isStarted()) {
            executor.execute(listener);
        } else {
            startedListenerList.add(ImmutablePair.of(listener, executor));
        }
    }
}
