package featurecat.lizzie.analysis;

import java.util.List;

public class BestMoveObserverFrequencyLimitedCollection extends BestMoveObserverCollection {
    private long minInterval;
    private long lastTime;

    public BestMoveObserverFrequencyLimitedCollection(long minInterval) {
        super();
        this.minInterval = minInterval;
        lastTime = System.currentTimeMillis();
    }

    @Override
    public void bestMovesUpdated(List<MoveData> newBestMoves) {
        boolean callObserver = false;
        long currentTime = System.currentTimeMillis();

        synchronized (this) {
            if (currentTime - lastTime > minInterval) {
                callObserver = true;
                lastTime = currentTime;
            }
        }

        if (callObserver) {
            super.bestMovesUpdated(newBestMoves);
        }
    }
}
