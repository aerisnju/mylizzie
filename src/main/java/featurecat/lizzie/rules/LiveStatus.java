package featurecat.lizzie.rules;

public class LiveStatus {
    private int hiddenMoveCount;

    public LiveStatus() {
        hiddenMoveCount = 0;
    }

    public int getHiddenMoveCount() {
        return hiddenMoveCount;
    }

    public void setHiddenMoveCount(int hiddenMoveCount) {
        this.hiddenMoveCount = hiddenMoveCount;
    }
}
