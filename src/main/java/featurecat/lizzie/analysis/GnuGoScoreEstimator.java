package featurecat.lizzie.analysis;

import com.google.common.primitives.Doubles;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import featurecat.lizzie.Lizzie;
import featurecat.lizzie.rules.Board;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class GnuGoScoreEstimator extends GtpBasedScoreEstimator {
    public GnuGoScoreEstimator(String commandLine) {
        super(commandLine);
    }

    @Override
    public ImmutablePair<String, Double> estimateScore() {
        final String rawScore = estimateScoreRaw();
        try {
            final Pattern SCORE_REGEX = Pattern.compile("([BbWw])\\+?\\b([0-9]*\\.?[0-9]+)\\b");
            final Matcher matcher = SCORE_REGEX.matcher(rawScore);
            if (matcher.find()) {
                final String color = matcher.group(1).toUpperCase();
                final String scoreString = matcher.group(2);
                final Double score = Double.valueOf(scoreString);

                return ImmutablePair.of(color, score);
            } else {
                // Should not happen
                return ImmutablePair.of("B", 0.0);
            }
        } catch (PatternSyntaxException | NumberFormatException | NullPointerException e) {
            // Syntax error in the regular expression. This SHOULD NOT HAPPEN!
            e.printStackTrace();
            return ImmutablePair.of("B", 0.0);
        }
    }

    @Override
    public String estimateScoreRaw() {
        return gtpClient.sendCommand("estimate_score").get(0).substring(2);
    }

    @Override
    public double[] estimateInfluences() {
        List<String> response = estimateInfluencesRaw();
        MutableDoubleList influences = new DoubleArrayList(Board.BOARD_SIZE * Board.BOARD_SIZE);
        for (String influenceLineString : response) {
            String[] influenceLine = influenceLineString.split("\\s+");
            for (String influenceString : influenceLine) {
                try {
                    influences.add(Doubles.constrainToRange(Double.parseDouble(influenceString), -1.0, 1.0));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }

        double[] influencesAdjusted = new double[Board.BOARD_SIZE * Board.BOARD_SIZE];
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                influencesAdjusted[Board.getIndex(i, j)] = -influences.get(Board.getIndex(Board.BOARD_SIZE - 1 - j, i));
            }
        }

        return influencesAdjusted;
    }

    @Override
    public List<String> estimateInfluencesRaw() {
        return gtpClient.sendCommand(
                String.format("initial_influence %s territory_value", Lizzie.board.getData().isBlackToPlay() ? "b" : "w")
        );
    }

    @Override
    public String getEstimatorName() {
        return "GNU Go";
    }
}
