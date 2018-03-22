package wagner.stephanie.lizzie.rules;

public class BoardTryPlayState {
    private BoardHistoryNode mainStreamEnd;
    private BoardHistoryNode nextPartBegin;

    public BoardTryPlayState(BoardHistoryNode mainStreamEnd, BoardHistoryNode nextPartBegin) {
        this.mainStreamEnd = mainStreamEnd;
        this.nextPartBegin = nextPartBegin;
    }

    public BoardHistoryNode getMainStreamEnd() {
        return mainStreamEnd;
    }

    public BoardHistoryNode getNextPartBegin() {
        return nextPartBegin;
    }

    public void cutMainStream() {
        mainStreamEnd.setNext(null);
    }

    public void restoreMainStream() {
        mainStreamEnd.connectNextNode(nextPartBegin);
    }

    public boolean isMainStreamConnected() {
        return mainStreamEnd.isJustBefore(nextPartBegin);
    }

    public int countRestoreUndoStep(BoardHistoryNode head) {
        if (head == null) {
            return mainStreamEnd.distanceToEnd();
        } else {
            return mainStreamEnd.distanceTo(head).orElse(Integer.MIN_VALUE);
        }
    }
}
