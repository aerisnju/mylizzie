package wagner.stephanie.lizzie.analysis;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.Closeable;

public interface ScoreEstimator extends Closeable {
    ImmutablePair<String, Double> estimateScore();

    String estimateScoreRaw();

    boolean isRunning();
}
