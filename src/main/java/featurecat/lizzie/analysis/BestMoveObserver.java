package featurecat.lizzie.analysis;

import java.util.List;

public interface BestMoveObserver {
    void bestMovesUpdated(List<MoveData> newBestMoves);

    void engineRestarted();
}
