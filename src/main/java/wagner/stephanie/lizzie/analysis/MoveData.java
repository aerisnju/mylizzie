package wagner.stephanie.lizzie.analysis;

import org.apache.commons.lang3.StringUtils;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.rules.Board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * holds the data from Leelaz's pondering mode
 */
public class MoveData {
    public String coordinate;
    public int playouts;
    public double winrate;
    public List<String> variation;

    /**
     * Parses a leelaz ponder output line
     *
     * @param line line of ponder output
     */
    public MoveData(String line) {
        String[] data = line.trim().split(" +");

        coordinate = data[0];
        playouts = Integer.parseInt(data[2]);
        winrate = Double.parseDouble(data[4].substring(0, data[4].length() - 2));

        variation = new ArrayList<>();
        variation.addAll(Arrays.asList(data).subList(8, data.length));
    }

    public String getMoveDisplayString() {
        if (Lizzie.optionSetting.isA1OnTop()) {
            return coordinate;
        } else {
            return transformAxisForMove(coordinate);
        }
    }

    public String getVariationDisplayString() {
        if (Lizzie.optionSetting.isA1OnTop()) {
            return String.join(" ", variation);
        } else {
            List<String> newVarAxis = variation.stream().map(MoveData::transformAxisForMove).collect(Collectors.toList());
            return String.join(" ", newVarAxis);
        }
    }

    private static String transformAxisForMove(String move) {
        int[] coords = Board.convertNameToCoordinates(move);
        int x = coords[0], y = coords[1];

        if (Board.isValid(x, y)) {
            return Board.alphabet.substring(x, x + 1) + (19 - y);
        } else {
            return "Pass";
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MoveData{");
        sb.append("coordinate='").append(coordinate).append('\'');
        sb.append(", playouts=").append(playouts);
        sb.append(", winrate=").append(winrate);
        sb.append(", variation=").append(variation);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MoveData)) return false;
        MoveData moveData = (MoveData) o;
        return playouts == moveData.playouts &&
                Double.compare(moveData.winrate, winrate) == 0 &&
                Objects.equals(coordinate, moveData.coordinate) &&
                Objects.equals(variation, moveData.variation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinate, playouts, winrate, variation);
    }
}
