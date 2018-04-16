package wagner.stephanie.lizzie.analysis;

import com.google.common.primitives.Doubles;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.rules.*;

import java.io.IOException;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;

public class GnuGoScoreEstimator implements ScoreEstimator {
    private BoardStateChangeObserver boardStateChangeObserver;
    private Consumer<Integer> boardSizeChangeObserver;
    private GeneralGtpClient gnuGoClient;

    public GnuGoScoreEstimator(String commandLine) {
        gnuGoClient = new GeneralGtpClient(commandLine);
        gnuGoClient.start();

        gnuGoClient.postCommand("boardsize " + Board.BOARD_SIZE);
        if (Board.BOARD_SIZE == 19) {
            gnuGoClient.postCommand("komi 7.5");
        } else {
            gnuGoClient.postCommand("komi 7");
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
                    gnuGoClient.postCommand(String.format("play %s %s", "B", move));
                } else if (data.getLastMoveColor() == Stone.WHITE) {
                    gnuGoClient.postCommand(String.format("play %s %s", "W", move));
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

                Lizzie.frame.getBoardRenderer().updateInfluences(null);
            }

            @Override
            public void boardCleared() {
                gnuGoClient.postCommand("clear_board");
                Lizzie.frame.getBoardRenderer().updateInfluences(null);
            }
        };

        boardSizeChangeObserver = newSize -> {
            gnuGoClient.postCommand("boardsize " + newSize);
            if (newSize == 19) {
                gnuGoClient.postCommand("komi 7.5");
            } else {
                gnuGoClient.postCommand("komi 7");
            }
        };

        Lizzie.board.registerBoardStateChangeObserver(boardStateChangeObserver);
        Board.registerBoardSizeChangeObserver(boardSizeChangeObserver);
    }

    @Override
    public ImmutablePair<String, Double> estimateScore() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public String estimateScoreRaw() {
        return gnuGoClient.sendCommand("estimate_score").get(0).substring(2);
    }

    @Override
    public double[] estimateInfluences() {
        List<String> response = estimateInfluencesRaw();
        MutableDoubleList influences = new DoubleArrayList(Board.BOARD_SIZE * Board.BOARD_SIZE);
        for (String influenceLineString : response) {
            String[] influenceLine = influenceLineString.split("\\s+");
            for (String influenceString : influenceLine) {
                try {
                    influences.add(Doubles.constrainToRange(Double.parseDouble(influenceString), -1.0, 1.0));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }

        return influences.toArray();
    }

    @Override
    public List<String> estimateInfluencesRaw() {
        return gnuGoClient.sendCommand(
                String.format("initial_influence %s territory_value", Lizzie.board.getData().isBlackToPlay() ? "b" : "w")
        );
    }

    @Override
    public boolean isRunning() {
        return gnuGoClient.isRunning();
    }

    @Override
    public void close() throws IOException {
        if (gnuGoClient != null) {
            Lizzie.board.unregisterBoardStateChangeObserver(boardStateChangeObserver);
            Board.unregisterBoardSizeChangeObserver(boardSizeChangeObserver);
            gnuGoClient.close();

            gnuGoClient = null;
        }
    }

    private static boolean isSuccessful(List<String> response) {
        boolean result = false;

        if (CollectionUtils.isNotEmpty(response)) {
            String beginElement = response.get(0);
            if (beginElement.startsWith("= ")) {
                result = true;
            }
        }

        return result;
    }

    private static void removeResponseHeader(List<String> response) {
        if (CollectionUtils.isNotEmpty(response)) {
            String beginElement = response.get(0);
            if (beginElement.startsWith("= ") || beginElement.startsWith("? ")) {
                beginElement = beginElement.substring(2);
                response.set(0, beginElement);
            }
        }
    }
}
