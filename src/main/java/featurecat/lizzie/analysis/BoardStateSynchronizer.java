package featurecat.lizzie.analysis;

import featurecat.lizzie.rules.*;

import java.util.OptionalInt;

public abstract class BoardStateSynchronizer implements BoardStateChangeObserver {
    @Override
    public void mainStreamAppended(BoardHistoryNode newNodeBegin, BoardHistoryNode head) {
    }

    @Override
    public void mainStreamCut(BoardHistoryNode nodeBeforeCutPoint, BoardHistoryNode head) {
    }

    @Override
    public void headMoved(BoardHistoryNode oldHead, BoardHistoryNode newHead) {
        if (oldHead.getNext() == newHead) {
            // Move forward
            replayMove(newHead.getData());
        } else if (oldHead.getPrevious() == newHead) {
            // Moved back
            handleGtpCommand("undo");
        } else {
            OptionalInt distanceOpt = oldHead.distanceTo(newHead);
            if (distanceOpt.isPresent()) {
                int distance = distanceOpt.getAsInt();
                if (distance > 0) {
                    // Forward
                    BoardHistoryNode p = oldHead.getNext();
                    while (true) {
                        replayMove(p.getData());

                        if (p == newHead) {
                            break;
                        }
                        p = p.getNext();
                    }
                } else if (distance < 0) {
                    for (int i = 0; i < -distance; ++i) {
                        handleGtpCommand("undo");
                    }
                }
            }
        }
    }

    @Override
    public void boardCleared(BoardHistoryNode initialNode, BoardHistoryNode initialHead) {
        handleGtpCommand("clear_board");
    }

    private void replayMove(BoardData data) {
        String move;
        if (data.getLastMove() == null) {
            move = "pass";
        } else {
            move = Board.convertCoordinatesToName(data.getLastMove());
        }

        if (data.getLastMoveColor() == Stone.BLACK) {
            handleGtpCommand(String.format("play %s %s", "B", move));
        } else if (data.getLastMoveColor() == Stone.WHITE) {
            handleGtpCommand(String.format("play %s %s", "W", move));
        }
    }

    protected abstract void handleGtpCommand(String command);
}
