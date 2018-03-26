package wagner.stephanie.lizzie.rules;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

public class BoardData {
    private ImmutablePair<Integer, Integer> boardSize;

    private Stone[] stonesOnBoard;
    private int[] lastMove;
    private Stone lastMoveColor;
    private boolean blackToPlay;
    private Zobrist zobrist;
    private int moveNumber;
    private int[] moveNumberListOnBoard;

    private double blackWinrate;
    private int calculationCount;

    public BoardData(ImmutablePair<Integer, Integer> boardSize, Stone[] stonesOnBoard, int[] lastMove, Stone lastMoveColor, boolean blackToPlay, Zobrist zobrist, int moveNumber, int[] moveNumberListOnBoard, double blackWinrate, int calculationCount) {
        this.boardSize = boardSize;
        this.stonesOnBoard = stonesOnBoard;
        this.lastMove = lastMove;
        this.lastMoveColor = lastMoveColor;
        this.blackToPlay = blackToPlay;
        this.zobrist = zobrist;
        this.moveNumber = moveNumber;
        this.moveNumberListOnBoard = moveNumberListOnBoard;
        this.blackWinrate = blackWinrate;
        this.calculationCount = calculationCount;
    }

    public BoardData(ImmutablePair<Integer, Integer> boardSize, Stone[] stonesOnBoard, int[] lastMove, Stone lastMoveColor, boolean blackToPlay, Zobrist zobrist, int moveNumber, int[] moveNumberListOnBoard) {
        this(boardSize, stonesOnBoard, lastMove, lastMoveColor, blackToPlay, zobrist, moveNumber, moveNumberListOnBoard, 50, 0);
    }

    public BoardData(Stone[] stonesOnBoard, int[] lastMove, Stone lastMoveColor, boolean blackToPlay, Zobrist zobrist, int moveNumber, int[] moveNumberListOnBoard) {
        this(ImmutablePair.of(19, 19), stonesOnBoard, lastMove, lastMoveColor, blackToPlay, zobrist, moveNumber, moveNumberListOnBoard, 50, 0);
    }

    public ImmutablePair<Integer, Integer> getBoardSize() {
        return boardSize;
    }

    public Stone[] getStonesOnBoard() {
        return stonesOnBoard;
    }

    public void setStonesOnBoard(Stone[] stonesOnBoard) {
        this.stonesOnBoard = stonesOnBoard;
    }

    public int[] getLastMove() {
        return lastMove;
    }

    public void setLastMove(int[] lastMove) {
        this.lastMove = lastMove;
    }

    public Stone getLastMoveColor() {
        return lastMoveColor;
    }

    public void setLastMoveColor(Stone lastMoveColor) {
        this.lastMoveColor = lastMoveColor;
    }

    public boolean isBlackToPlay() {
        return blackToPlay;
    }

    public void setBlackToPlay(boolean blackToPlay) {
        this.blackToPlay = blackToPlay;
    }

    public Zobrist getZobrist() {
        return zobrist;
    }

    public void setZobrist(Zobrist zobrist) {
        this.zobrist = zobrist;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
    }

    public int[] getMoveNumberListOnBoard() {
        return moveNumberListOnBoard;
    }

    public void setMoveNumberListOnBoard(int[] moveNumberListOnBoard) {
        this.moveNumberListOnBoard = moveNumberListOnBoard;
    }

    public double getBlackWinrate() {
        return blackWinrate;
    }

    public void setBlackWinrate(double blackWinrate) {
        this.blackWinrate = blackWinrate;
    }

    public int getCalculationCount() {
        return calculationCount;
    }

    public void setCalculationCount(int calculationCount) {
        this.calculationCount = calculationCount;
    }

    public Optional<Boolean> isBlack() {
        switch (lastMoveColor) {
            case BLACK:
                return Optional.of(true);
            case WHITE:
                return Optional.of(false);
            default:
                return Optional.empty();
        }
    }

    public boolean isPass() {
        return lastMove == null || lastMove[0] < 0 || lastMove[0] >= boardSize.getLeft() || lastMove[1] < 0 || lastMove[1] >= boardSize.getRight();
    }

    public void tryUpdateWinrate(double newWinrate, int newCalculationCount, boolean isBlack) {
        if (newCalculationCount > calculationCount) {
            calculationCount = newCalculationCount;
            if (isBlack) {
                blackWinrate = newWinrate;
            } else {
                blackWinrate = 100 - newWinrate;
            }
        }
    }

    public int coordsToIndex(int row, int col) {
        return row * boardSize.getLeft() + col;
    }

    public Stone getStoneOnBoard(int row, int col) {
        int index = coordsToIndex(row, col);
        try {
            return stonesOnBoard[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public int getMoveNumberOnBoard(int row, int col) {
        int index = coordsToIndex(row, col);
        try {
            return moveNumberListOnBoard[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }
}
