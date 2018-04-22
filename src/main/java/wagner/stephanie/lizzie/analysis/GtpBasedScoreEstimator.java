package wagner.stephanie.lizzie.analysis;

import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.rules.*;

import java.io.IOException;
import java.util.OptionalInt;
import java.util.function.Consumer;

public abstract class GtpBasedScoreEstimator implements ScoreEstimator {
    protected BoardStateChangeObserver boardStateChangeObserver;
    protected Consumer<Integer> boardSizeChangeObserver;

    protected GeneralGtpClient gtpClient;
    protected double komi;

    public GtpBasedScoreEstimator(String commandLine) {
        gtpClient = new GeneralGtpClient(commandLine);
        gtpClient.start();

        gtpClient.postCommand("boardsize " + Board.BOARD_SIZE);
        if (Board.BOARD_SIZE == 19) {
            gtpClient.postCommand("komi 7.5");
            komi = 7.5;
        } else {
            gtpClient.postCommand("komi 6.5");
            komi = 6.5;
        }

        boardStateChangeObserver = new BoardStateChangeObserver() {
            @Override
            public void mainStreamAppended(BoardHistoryNode newNodeBegin, BoardHistoryNode head) {
                BoardHistoryNode p = newNodeBegin;
                while (true) {
                    replayMove(p.getData());

                    if (p == head) {
                        break;
                    }
                    p = p.getNext();
                }

                Lizzie.frame.getBoardRenderer().updateInfluences(null);
            }

            private void replayMove(BoardData data) {
                String move;
                if (data.getLastMove() == null) {
                    move = "pass";
                } else {
                    move = Board.convertCoordinatesToName(data.getLastMove());
                }

                if (data.getLastMoveColor() == Stone.BLACK) {
                    gtpClient.postCommand(String.format("play %s %s", "B", move));
                } else if (data.getLastMoveColor() == Stone.WHITE) {
                    gtpClient.postCommand(String.format("play %s %s", "W", move));
                }

                Lizzie.frame.getBoardRenderer().updateInfluences(null);
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
                    gtpClient.postCommand("undo");
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
                                gtpClient.postCommand("undo");
                            }
                        }
                    }
                }

                Lizzie.frame.getBoardRenderer().updateInfluences(null);
            }

            @Override
            public void boardCleared() {
                gtpClient.postCommand("clear_board");
                Lizzie.frame.getBoardRenderer().updateInfluences(null);
            }
        };

        boardSizeChangeObserver = newSize -> {
            gtpClient.postCommand("boardsize " + newSize);
            if (newSize == 19) {
                gtpClient.postCommand("komi 7.5");
                komi = 7.5;
            } else {
                gtpClient.postCommand("komi 6.5");
                komi = 6.5;
            }
        };

        Lizzie.board.registerBoardStateChangeObserver(boardStateChangeObserver);
        Board.registerBoardSizeChangeObserver(boardSizeChangeObserver);
    }

    @Override
    public void setKomi(double komi) {
        gtpClient.postCommand("komi " + komi);
        this.komi = komi;
    }

    @Override
    public double getKomi() {
        return komi;
    }

    @Override
    public boolean isRunning() {
        return gtpClient.isRunning();
    }

    @Override
    public void close() throws IOException {
        if (gtpClient != null) {
            Lizzie.board.unregisterBoardStateChangeObserver(boardStateChangeObserver);
            Board.unregisterBoardSizeChangeObserver(boardSizeChangeObserver);
            gtpClient.close();

            gtpClient = null;
        }
    }
}
