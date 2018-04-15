package wagner.stephanie.lizzie.analysis;

import org.apache.commons.lang3.tuple.ImmutablePair;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.rules.*;

import java.io.IOException;
import java.util.OptionalInt;

public class GnuGoScoreEstimator implements ScoreEstimator {
    private BoardStateChangeObserver boardStateChangeObserver;
    private GeneralGtpClient gnuGoClient;

    public GnuGoScoreEstimator(String commandLine) {
        gnuGoClient = new GeneralGtpClient(commandLine);
        gnuGoClient.start();

        gnuGoClient.postCommand("komi 7.5");

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
            }

            private void replayMove(BoardData data) {
                String move;
                if (data.getLastMove() == null) {
                    move = "pass";
                } else {
                    move = Board.convertCoordinatesToName(data.getLastMove());
                }

                if (data.getLastMoveColor() == Stone.BLACK) {
                    gnuGoClient.postCommand(String.format("play %s %s", "B", move));
                } else if (data.getLastMoveColor() == Stone.WHITE) {
                    gnuGoClient.postCommand(String.format("play %s %s", "W", move));
                }
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
                    gnuGoClient.postCommand("undo");
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
                                gnuGoClient.postCommand("undo");
                            }
                        }
                    }
                }
            }

            @Override
            public void boardCleared() {
                gnuGoClient.postCommand("clear_board");
            }
        };

        Lizzie.board.registerBoardStateChangeObserver(boardStateChangeObserver);
    }

    @Override
    public ImmutablePair<String, Double> estimateScore() {
        return null;
    }

    @Override
    public String estimateScoreRaw() {
        return gnuGoClient.sendCommand("estimate_score").get(0).substring(2);
    }

    @Override
    public boolean isRunning() {
        return gnuGoClient.isRunning();
    }

    @Override
    public void close() throws IOException {
        if (gnuGoClient != null) {
            Lizzie.board.unregisterBoardStateChangeObserver(boardStateChangeObserver);
            gnuGoClient.close();

            gnuGoClient = null;
        }
    }
}
