package wagner.stephanie.lizzie.analysis;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.Closeable;
import java.util.List;

public interface ScoreEstimator extends Closeable {
    ImmutablePair<String, Double> estimateScore();

    String estimateScoreRaw();

    double[] estimateInfluences();

    List<String> estimateInfluencesRaw();

    boolean isRunning();
}
