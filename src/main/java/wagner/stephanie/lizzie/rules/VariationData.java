package wagner.stephanie.lizzie.rules;

import wagner.stephanie.lizzie.analysis.MoveData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VariationData {
    private List<int[]> variation;
    private int playouts;
    private double winrate;

    public VariationData(MoveData moveData) {
        playouts = moveData.getPlayouts();
        winrate = moveData.getWinrate();
        variation = moveData.getVariation().stream().map(Board::convertNameToCoordinates).collect(Collectors.toCollection(ArrayList::new));
    }

    public List<int[]> getVariation() {
        return variation;
    }

    public int getPlayouts() {
        return playouts;
    }

    public double getWinrate() {
        return winrate;
    }
}
