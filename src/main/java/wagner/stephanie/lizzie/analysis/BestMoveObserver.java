package wagner.stephanie.lizzie.analysis;

import java.util.List;

public interface BestMoveObserver {
    void bestMovesUpdated(int boardStateCount, List<MoveData> newBestMoves);

    void engineRestarted();
}
