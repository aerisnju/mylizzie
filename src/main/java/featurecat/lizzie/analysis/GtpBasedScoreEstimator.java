package featurecat.lizzie.analysis;

import featurecat.lizzie.Lizzie;
import featurecat.lizzie.rules.*;

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

        boardStateChangeObserver = new BoardStateSynchronizer() {
            @Override
            public void headMoved(BoardHistoryNode oldHead, BoardHistoryNode newHead) {
                super.headMoved(oldHead, newHead);

                Lizzie.frame.getBoardRenderer().updateInfluences(null);
            }

            @Override
            public void boardCleared(BoardHistoryNode initialNode, BoardHistoryNode initialHead) {
                super.boardCleared(initialNode, initialHead);

                Lizzie.frame.getBoardRenderer().updateInfluences(null);
            }

            @Override
            protected void handleGtpCommand(String command) {
                gtpClient.postCommand(command);
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
    public void close() {
        if (gtpClient != null) {
            Lizzie.board.unregisterBoardStateChangeObserver(boardStateChangeObserver);
            Board.unregisterBoardSizeChangeObserver(boardSizeChangeObserver);
            gtpClient.close();

            gtpClient = null;
        }
    }
}
