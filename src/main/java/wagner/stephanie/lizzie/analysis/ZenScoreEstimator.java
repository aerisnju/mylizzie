package wagner.stephanie.lizzie.analysis;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import wagner.stephanie.lizzie.rules.Board;

import java.util.List;

public class ZenScoreEstimator extends GtpBasedScoreEstimator {
    public ZenScoreEstimator(String commandLine) {
        super(commandLine);
    }

    @Override
    public ImmutablePair<String, Double> estimateScore() {
        List<String> response = gtpClient.sendCommand("territory_statistics area");;
        MutableIntList influences = GtpClient.parseResponseIntTable(response);
        int blackTerritoryCount = influences.count(value -> value >= 300);
        int whiteTerritoryCount = influences.count(value -> value <= -300);

        MutableIntList prisoners = getPrisoners();

        double score = blackTerritoryCount - whiteTerritoryCount - getKomi();
        String color = "B";
        if (score < 0) {
            color = "W";
            score = -score;
        }

        return ImmutablePair.of(color, score);
    }

    @Override
    public String estimateScoreRaw() {
        ImmutablePair<String, Double> score = estimateScore();
        return score.getLeft() + "+" + score.getRight();
    }

    @Override
    public double[] estimateInfluences() {
        List<String> response = estimateInfluencesRaw();
        MutableIntList territories = GtpClient.parseResponseIntTable(response);
        MutableDoubleList influences = territories.collectDouble(influence -> {
            if (-300 < influence && influence < 300) {
                influence = 0;
            } else if (influence > 800) {
                influence = 800;
            } else if (influence < -800) {
                influence = -800;
            }
            return influence / 800.0;
        }, new DoubleArrayList(Board.BOARD_SIZE * Board.BOARD_SIZE));

        double[] influencesAdjusted = new double[Board.BOARD_SIZE * Board.BOARD_SIZE];
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                influencesAdjusted[Board.getIndex(j, Board.BOARD_SIZE - 1 - i)] = influences.get(Board.getIndex(i, j));
            }
        }

        return influencesAdjusted;
    }

    @Override
    public List<String> estimateInfluencesRaw() {
        return gtpClient.sendCommand("territory_statistics");
    }

    @Override
    public String getEstimatorName() {
        return "Zen";
    }

    private MutableIntList getPrisoners() {
        List<String> response = gtpClient.sendCommand("prisoner_count");
        return GtpClient.parseResponseIntTable(response);
    }
}
