package featurecat.lizzie.analysis;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractAnalyzer implements Analyzer {
    private final AtomicBoolean shutdownCalled;

    protected volatile boolean analyzingOngoing;
    protected volatile boolean analyzingEnabled;

    protected BestMoveObserverCollection observers;

    public AbstractAnalyzer() {
        shutdownCalled = new AtomicBoolean(false);

        analyzingEnabled = false;
        analyzingOngoing = false;
        observers = new BestMoveObserverCollection();
    }

    @Override
    public void registerBestMoveObserver(BestMoveObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unregisterBestMoveObserver(BestMoveObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void registerListOfBestMoveObserver(ImmutableList<BestMoveObserver> observers) {
        this.observers.addAll(observers.castToList());
    }

    @Override
    public ImmutableList<BestMoveObserver> getRegisteredBestMoveObservers() {
        return Lists.immutable.ofAll(observers);
    }

    @Override
    public void clearRegisteredBestMoveObservers() {
        observers.clear();
    }

    @Override
    public synchronized void startAnalyzing() {
        if (!analyzingOngoing) {
            doStartAnalyzing();
            analyzingOngoing = true;
        }
    }

    @Override
    public synchronized void pauseAnalyzing() {
        if (analyzingOngoing) {
            doStopAnalyzing();
            analyzingOngoing = false;
        }
    }

    @Override
    public synchronized void disableAnalyzing() {
        pauseAnalyzing();
        if (analyzingEnabled) {
            analyzingEnabled = false;
        }
    }

    @Override
    public synchronized void enableAnalyzing() {
        startAnalyzing();
        if (!analyzingEnabled) {
            analyzingEnabled = true;
        }
    }

    @Override
    public synchronized boolean isAnalyzingOngoing() {
        return analyzingOngoing;
    }

    @Override
    public synchronized boolean isAnalyzingEnabled() {
        return analyzingEnabled;
    }

    @Override
    public synchronized ListenableFuture<List<String>> postGtpCommand(String command) {
        try {
            return postRawGtpCommand(command);
        } finally {
            boolean analyzingOngoingAfter = isAnalyzingOngoingAfterCommand(command);
            if (isAnalyzingEnabled()) {
                analyzingOngoing = true;
                if (!analyzingOngoingAfter) {
                    doStartAnalyzing();
                }
            } else {
                analyzingOngoing = false;
                if (analyzingOngoingAfter) {
                    doStopAnalyzing();
                }
            }
        }
    }

    @Override
    public void shutdown(long timeout, TimeUnit timeUnit) {
        if (!shutdownCalled.getAndSet(true)) {
            disableAnalyzing();
            doShutdown(timeout, timeUnit);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    protected abstract ListenableFuture<List<String>> postRawGtpCommand(String command);

    protected abstract void doStartAnalyzing();

    protected abstract void doStopAnalyzing();

    protected abstract boolean isAnalyzingOngoingAfterCommand(String command);

    protected abstract void doShutdown(long timeout, TimeUnit timeUnit);
}
