package wagner.stephanie.lizzie.analysis;

import java.util.Objects;

public interface DetailedScoreEstimator extends ScoreEstimator {
    class DetailedScore {
        private int blackTerritoryCount;
        private int whiteTerritoryCount;
        private int blackDeadCount;
        private int whiteDeadCount;
        private int blackPrisonerCount;
        private int whitePrisonerCount;
        private double score;

        public DetailedScore() {
        }

        public DetailedScore(int blackTerritoryCount, int whiteTerritoryCount, int blackDeadCount, int whiteDeadCount, int blackPrisonerCount, int whitePrisonerCount, double score) {
            this.blackTerritoryCount = blackTerritoryCount;
            this.whiteTerritoryCount = whiteTerritoryCount;
            this.blackDeadCount = blackDeadCount;
            this.whiteDeadCount = whiteDeadCount;
            this.blackPrisonerCount = blackPrisonerCount;
            this.whitePrisonerCount = whitePrisonerCount;
            this.score = score;
        }

        public int getBlackTerritoryCount() {
            return blackTerritoryCount;
        }

        public void setBlackTerritoryCount(int blackTerritoryCount) {
            this.blackTerritoryCount = blackTerritoryCount;
        }

        public int getWhiteTerritoryCount() {
            return whiteTerritoryCount;
        }

        public void setWhiteTerritoryCount(int whiteTerritoryCount) {
            this.whiteTerritoryCount = whiteTerritoryCount;
        }

        public int getBlackDeadCount() {
            return blackDeadCount;
        }

        public void setBlackDeadCount(int blackDeadCount) {
            this.blackDeadCount = blackDeadCount;
        }

        public int getWhiteDeadCount() {
            return whiteDeadCount;
        }

        public void setWhiteDeadCount(int whiteDeadCount) {
            this.whiteDeadCount = whiteDeadCount;
        }

        public int getBlackPrisonerCount() {
            return blackPrisonerCount;
        }

        public void setBlackPrisonerCount(int blackPrisonerCount) {
            this.blackPrisonerCount = blackPrisonerCount;
        }

        public int getWhitePrisonerCount() {
            return whitePrisonerCount;
        }

        public void setWhitePrisonerCount(int whitePrisonerCount) {
            this.whitePrisonerCount = whitePrisonerCount;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
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
                    Double.compare(that.score, score) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(blackTerritoryCount, whiteTerritoryCount, blackDeadCount, whiteDeadCount, blackPrisonerCount, whitePrisonerCount, score);
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
            sb.append(", score=").append(score);
            sb.append('}');
            return sb.toString();
        }
    }

    DetailedScore estimateDetailedScore();
}
