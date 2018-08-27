package featurecat.lizzie.analysis;

import org.apache.commons.math3.util.Precision;

import java.util.Objects;

public interface DetailedScoreEstimator extends ScoreEstimator {
    class DetailedScore {
        private final int blackTerritoryCount;
        private final int whiteTerritoryCount;
        private final int blackDeadCount;
        private final int whiteDeadCount;
        private final int blackPrisonerCount;
        private final int whitePrisonerCount;
        private final int blackAreaCount;
        private final int whiteAreaCount;
        private final int dameCount;
        private final double score;

        public DetailedScore(int blackTerritoryCount, int whiteTerritoryCount, int blackDeadCount, int whiteDeadCount, int blackPrisonerCount, int whitePrisonerCount, int blackAreaCount, int whiteAreaCount, int dameCount, double score) {
            this.blackTerritoryCount = blackTerritoryCount;
            this.whiteTerritoryCount = whiteTerritoryCount;
            this.blackDeadCount = blackDeadCount;
            this.whiteDeadCount = whiteDeadCount;
            this.blackPrisonerCount = blackPrisonerCount;
            this.whitePrisonerCount = whitePrisonerCount;
            this.blackAreaCount = blackAreaCount;
            this.whiteAreaCount = whiteAreaCount;
            this.dameCount = dameCount;
            this.score = score;
        }

        public int getBlackTerritoryCount() {
            return blackTerritoryCount;
        }

        public int getWhiteTerritoryCount() {
            return whiteTerritoryCount;
        }

        public int getBlackDeadCount() {
            return blackDeadCount;
        }

        public int getWhiteDeadCount() {
            return whiteDeadCount;
        }

        public int getBlackPrisonerCount() {
            return blackPrisonerCount;
        }

        public int getWhitePrisonerCount() {
            return whitePrisonerCount;
        }

        public int getBlackAreaCount() {
            return blackAreaCount;
        }

        public int getWhiteAreaCount() {
            return whiteAreaCount;
        }

        public int getDameCount() {
            return dameCount;
        }

        public double getScore() {
            return score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DetailedScore that = (DetailedScore) o;
            return blackTerritoryCount == that.blackTerritoryCount &&
                    whiteTerritoryCount == that.whiteTerritoryCount &&
                    blackDeadCount == that.blackDeadCount &&
                    whiteDeadCount == that.whiteDeadCount &&
                    blackPrisonerCount == that.blackPrisonerCount &&
                    whitePrisonerCount == that.whitePrisonerCount &&
                    blackAreaCount == that.blackAreaCount &&
                    whiteAreaCount == that.whiteAreaCount &&
                    dameCount == that.dameCount &&
                    Precision.equals(that.score, score, 0.001);
        }

        @Override
        public int hashCode() {
            return Objects.hash(blackTerritoryCount, whiteTerritoryCount, blackDeadCount, whiteDeadCount, blackPrisonerCount, whitePrisonerCount, blackAreaCount, whiteAreaCount, dameCount, score);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("DetailedScore{");
            sb.append("blackTerritoryCount=").append(blackTerritoryCount);
            sb.append(", whiteTerritoryCount=").append(whiteTerritoryCount);
            sb.append(", blackDeadCount=").append(blackDeadCount);
            sb.append(", whiteDeadCount=").append(whiteDeadCount);
            sb.append(", blackPrisonerCount=").append(blackPrisonerCount);
            sb.append(", whitePrisonerCount=").append(whitePrisonerCount);
            sb.append(", blackAreaCount=").append(blackAreaCount);
            sb.append(", whiteAreaCount=").append(whiteAreaCount);
            sb.append(", dameCount=").append(dameCount);
            sb.append(", score=").append(score);
            sb.append('}');
            return sb.toString();
        }
    }

    DetailedScore estimateDetailedScore();
}
