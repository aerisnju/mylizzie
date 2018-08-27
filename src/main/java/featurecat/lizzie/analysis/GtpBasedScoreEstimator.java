package featurecat.lizzie.analysis;

import featurecat.lizzie.Lizzie;
import featurecat.lizzie.rules.Board;
import featurecat.lizzie.rules.BoardHistoryNode;
import featurecat.lizzie.rules.BoardStateChangeObserver;
import featurecat.lizzie.rules.GameInfo;

import java.util.function.Consumer;

public abstract class GtpBasedScoreEstimator implements ScoreEstimator {
    protected BoardStateChangeObserver boardStateChangeObserver;
    protected Consumer<Integer> boardSizeChangeObserver;
    protected GameInfo.GameInfoChangeListener gameInfoChangeListener;

    protected GeneralGtpClient gtpClient;
    protected double komi;

    public GtpBasedScoreEstimator(String commandLine) {
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
        };

        gameInfoChangeListener = (info, changedItemTypes) -> {
            if (changedItemTypes.contains(GameInfo.StateChangedItemType.KOMI)) {
                setKomi(info.getKomi());
            }
        };

        gtpClient = new GeneralGtpClient(commandLine);
        gtpClient.start();

        gtpClient.postCommand("boardsize " + Board.BOARD_SIZE);
        setKomi(Lizzie.gameInfo.getKomi());

        Lizzie.board.registerBoardStateChangeObserver(boardStateChangeObserver);
        Board.registerBoardSizeChangeObserver(boardSizeChangeObserver);
        Lizzie.gameInfo.registerGameInfoChangeListener(gameInfoChangeListener);
    }

    @Override
    public void setKomi(double komi) {
        this.komi = komi;
        gtpClient.postCommand(String.format("komi %.1f", komi));
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
            Lizzie.gameInfo.unregisterGameInfoChangeListener(gameInfoChangeListener);
            Board.unregisterBoardSizeChangeObserver(boardSizeChangeObserver);
            Lizzie.board.unregisterBoardStateChangeObserver(boardStateChangeObserver);
            gtpClient.close();

            gtpClient = null;
        }
    }
}
