package featurecat.lizzie.rules;

public class GameInfo {
    private final boolean blackFirst;
    private final double komi;
    private final int blackHandicap;
    private final int whiteHandicap;

    GameInfo() {
        this(true, 7.5, 0, 0);
    }

    GameInfo(boolean blackFirst, double komi, int blackHandicap, int whiteHandicap) {
        this.blackFirst = blackFirst;
        this.komi = komi;
        this.blackHandicap = blackHandicap;
        this.whiteHandicap = whiteHandicap;
    }

    public boolean isBlackFirst() {
        return blackFirst;
    }

    public double getKomi() {
        return komi;
    }

    public int getBlackHandicap() {
        return blackHandicap;
    }

    public int getWhiteHandicap() {
        return whiteHandicap;
    }

    GameInfo newKomi(double komi) {
        return new GameInfo(blackFirst, komi, blackHandicap, whiteHandicap);
    }
}
