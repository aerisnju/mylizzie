package wagner.stephanie.lizzie.rules;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import wagner.stephanie.lizzie.Lizzie;
import wagner.stephanie.lizzie.analysis.BestMoveObserver;
import wagner.stephanie.lizzie.analysis.MoveData;
import wagner.stephanie.lizzie.util.ThreadPoolUtil;

import java.io.Closeable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Board implements Closeable {
    public final static String alphabet = "ABCDEFGHJKLMNOPQRST";
    public static int BOARD_SIZE = Lizzie.optionSetting.getBoardSize().getWidth();
    private static List<Consumer<Integer>> boardSizeChangeObserver = new CopyOnWriteArrayList<>();

    private BoardHistoryList history;
    private BoardTryPlayState tryPlayState;
    private BoardStateChangeObserverCollection observerCollection;
    private BestMoveObserver bestMoveObserver;

    private ExecutorService leelazExecutor;

    public Board() {
        leelazExecutor = Executors.newSingleThreadExecutor();
        initBoardHistoryList();
        tryPlayState = null;
        observerCollection = new BoardStateChangeObserverCollection();

        bestMoveObserver = new BestMoveObserver() {
            @Override
            public void bestMovesUpdated(int boardStateCount, List<MoveData> newBestMoves) {
                synchronized (Board.this) {
                    if (CollectionUtils.isNotEmpty(newBestMoves) && boardStateCount == getData().getMoveNumber()) {
                        BoardData boardData = getData();
                        boardData.tryUpdateVariationInfo(newBestMoves);
                    }
                }
            }

            @Override
            public void engineRestarted() {

            }
        };

        Lizzie.leelaz.registerBestMoveObserver(bestMoveObserver);
    }

    private void initBoardHistoryList() {
        Stone[] stones = new Stone[BOARD_SIZE * BOARD_SIZE];
        Arrays.fill(stones, Stone.EMPTY);

        history = new BoardHistoryList(new BoardData(ImmutablePair.of(BOARD_SIZE, BOARD_SIZE), stones, null, Stone.EMPTY, true, new Zobrist(), 0, new int[BOARD_SIZE * BOARD_SIZE]));
    }

    public void clear() {
        synchronized (this) {
            // We don't use history.clear() because it will not update board size
            initBoardHistoryList();
            observerCollection.boardCleared();
            observerCollection.mainStreamAppended(history.getInitialNode(), history.getHead());
        }
    }

    public BoardStateChangeObserverCollection getObserverCollection() {
        return observerCollection;
    }

    public void setObserverCollection(BoardStateChangeObserverCollection observerCollection) {
        this.observerCollection = observerCollection;
    }

    public void registerBoardStateChangeObserver(BoardStateChangeObserver observer) {
        observerCollection.add(observer);

        observer.mainStreamAppended(history.getInitialNode(), history.getHead());
    }

    public void unregisterBoardStateChangeObserver(BoardStateChangeObserver observer) {
        observerCollection.remove(observer);
    }

    public boolean isInTryPlayState() {
        return tryPlayState != null;
    }

    public void enterTryPlayState() {
        synchronized (this) {
            if (!isInTryPlayState()) {
                tryPlayState = new BoardTryPlayState(history.getHead(), history.getHead().getNext());
                tryPlayState.cutMainStream();
                Lizzie.frame.showTryPlayTitle();

                if (tryPlayState.getNextPartBegin() != null) {
                    observerCollection.mainStreamCut(tryPlayState.getMainStreamEnd(), history.getHead());
                }
            }
        }
    }

    public int getTryPlayStateBeginMoveNumber() {
        synchronized (this) {
            if (isInTryPlayState()) {
                return tryPlayState.getMainStreamEnd().getData().getMoveNumber();
            } else {
                return 0;
            }
        }
    }

    public void leaveTryPlayState() {
        synchronized (this) {
            if (isInTryPlayState()) {
                if (!tryPlayState.isMainStreamConnected()) {
                    gotoMove(getTryPlayStateBeginMoveNumber());
                    BoardHistoryNode tryPlayBeginNode = tryPlayState.getMainStreamEnd().getNext();
                    if (tryPlayBeginNode != null) {
                        tryPlayState.getMainStreamEnd().addTryPlayHistory(tryPlayBeginNode);
                    }

                    tryPlayState.restoreMainStream();

                    observerCollection.mainStreamCut(tryPlayState.getMainStreamEnd(), history.getHead());
                }

                Lizzie.frame.restoreDefaultTitle();
                BoardHistoryNode nextBegin = tryPlayState.getNextPartBegin();
                tryPlayState = null;
                if (nextBegin != null) {
                    observerCollection.mainStreamAppended(nextBegin, history.getHead());
                }
            }
        }
    }

    public BoardHistoryList getHistory() {
        return history;
    }

    /**
     * Calculates the array index of a stone stored at (x, y)
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the array index
     */
    public static int getIndex(int x, int y) {
        return x * Board.BOARD_SIZE + y;
    }

    /**
     * Converts a named coordinate eg C16, T5, K10, etc to an x and y coordinate
     *
     * @param namedCoordinate a capitalized version of the named coordinate. Must be a valid 19x19 Go coordinate, without I
     * @return an array containing x, followed by y
     */
    public static int[] convertNameToCoordinates(String namedCoordinate) {
        if (StringUtils.isEmpty(namedCoordinate) || StringUtils.equalsIgnoreCase(namedCoordinate, "pass")) {
            return new int[]{BOARD_SIZE, BOARD_SIZE};
        } else {
            // coordinates take the form C16 A19 Q5 K10 etc. I is not used.
            int x = alphabet.indexOf(namedCoordinate.charAt(0));
            if (x < 0) {
                return new int[]{BOARD_SIZE, BOARD_SIZE};
            }
            int y;
            try {
                y = Integer.parseInt(namedCoordinate.substring(1)) - 1;
            } catch (NumberFormatException e) {
                return new int[]{BOARD_SIZE, BOARD_SIZE};
            }

            return new int[]{x, y};
        }
    }

    public static int[] convertDisplayNameToCoordinates(String namedCoordinate) {
        int[] result = convertNameToCoordinates(namedCoordinate);
        if (isValid(result[0], result[1]) && !Lizzie.optionSetting.isA1OnTop()) {
            result[1] = BOARD_SIZE - 1 - result[1];
        }
        return result;
    }

    /**
     * Converts a x and y coordinate to a named coordinate eg C16, T5, K10, etc
     *
     * @param x x coordinate -- must be valid
     * @param y y coordinate -- must be valid
     * @return a string representing the coordinate
     */
    public static String convertCoordinatesToName(int x, int y) {
        if (isValid(x, y)) {
            // coordinates take the form C16 A19 Q5 K10 etc. I is not used.
            return alphabet.charAt(x) + "" + (y + 1);
        } else {
            return "Pass";
        }
    }

    public static String convertCoordinatesToName(int[] coords) {
        if (ArrayUtils.isEmpty(coords) || coords.length != 2) {
            return "Pass";
        } else {
            return convertCoordinatesToName(coords[0], coords[1]);
        }
    }

    public static String convertCoordinatesToDisplayName(int x, int y) {
        if (isValid(x, y)) {
            // coordinates take the form C16 A19 Q5 K10 etc. I is not used.
            if (Lizzie.optionSetting.isA1OnTop()) {
                return alphabet.charAt(x) + "" + (y + 1);
            } else {
                return alphabet.charAt(x) + "" + (BOARD_SIZE - y);
            }
        } else {
            return "Pass";
        }
    }

    public static String convertCoordinatesToDisplayName(int[] coords) {
        if (ArrayUtils.isEmpty(coords) || coords.length != 2) {
            return "Pass";
        } else {
            return convertCoordinatesToDisplayName(coords[0], coords[1]);
        }
    }

    /**
     * Checks if a coordinate is valid
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return whether or not this coordinate is part of the board
     */
    public static boolean isValid(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    public static boolean isValid(int[] coords) {
        return ArrayUtils.isNotEmpty(coords) && coords.length == 2 && isValid(coords[0], coords[1]);
    }

    /**
     * The pass. Thread safe
     *
     * @param color the type of pass
     */
    private void pass(Stone color) {
        synchronized (this) {
            // Forbid passing if the current move is before the move when try play state began
            if (isInTryPlayState() && history.getMoveNumber() < getTryPlayStateBeginMoveNumber()) {
                return;
            }

            // Forbid successive two passing
            if (history.getLastMove() == null && !Objects.equals(history.getLastMoveColor(), Stone.EMPTY)) {
                return;
            }

            // If pass move happens in history middle, auto swith to try play mode
            if (Lizzie.optionSetting.isAutoEnterTryPlayingMode() && !isInTryPlayState() && getHistory().getHead().getNext() != null) {
                enterTryPlayState();
            }

            Stone[] stones = history.getStones().clone();
            Zobrist zobrist = history.getZobrist();
            int moveNumber = history.getMoveNumber() + 1;
            int[] moveNumberList = history.getMoveNumberList().clone();

            // build the new game state
            BoardData newState = new BoardData(stones, null, color, !history.isBlacksTurn(), zobrist, moveNumber, moveNumberList);

            // update history with pass
            if (history.getHead().getNext() != null) {
                history.getHead().disconnectNextNode();
                observerCollection.mainStreamCut(history.getHead(), history.getHead());
            }
            history.add(newState);
            observerCollection.mainStreamAppended(history.getHead(), history.getHead());

            // update leelaz with pass
            leelazExecutor.execute(() -> {
                Lizzie.leelaz.play(color, "pass");
            });
        }
    }

    /**
     * overloaded method for pass(), chooses color in an alternating pattern
     */
    public void pass() {
        pass(history.isBlacksTurn() ? Stone.BLACK : Stone.WHITE);
    }

    /**
     * Places a stone onto the board representation. Thread safe
     *
     * @param x     x coordinate
     * @param y     y coordinate
     * @param color the type of stone to place
     */
    private void place(int x, int y, Stone color) {
        synchronized (this) {
            if (!isValid(x, y) || history.getStones()[getIndex(x, y)] != Stone.EMPTY)
                return;

            // Forbid placing a stone if the current move is before the move when try play state began
            if (isInTryPlayState() && history.getMoveNumber() < getTryPlayStateBeginMoveNumber()) {
                return;
            }

            // If stone placement happens in history middle, auto swith to try play mode
            if (Lizzie.optionSetting.isAutoEnterTryPlayingMode() && !isInTryPlayState() && getHistory().getHead().getNext() != null) {
                enterTryPlayState();
            }

            // load a copy of the data at the current node of history
            Stone[] stones = history.getStones().clone();
            Zobrist zobrist = history.getZobrist();
            int[] lastMove = new int[]{x, y}; // keep track of the last played stone
            int moveNumber = history.getMoveNumber() + 1;
            int[] moveNumberList = history.getMoveNumberList().clone();

            moveNumberList[Board.getIndex(x, y)] = moveNumber;

            // set the stone at (x, y) to color
            stones[getIndex(x, y)] = color;
            zobrist.toggleStone(x, y, color);

            // remove enemy stones
            removeDeadChain(x + 1, y, color.opposite(), stones, zobrist);
            removeDeadChain(x, y + 1, color.opposite(), stones, zobrist);
            removeDeadChain(x - 1, y, color.opposite(), stones, zobrist);
            removeDeadChain(x, y - 1, color.opposite(), stones, zobrist);

            // check to see if the player made a suicidal coordinate
            boolean isSuicidal = removeDeadChain(x, y, color, stones, zobrist);

            for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
                if (stones[i].equals(Stone.EMPTY)) {
                    moveNumberList[i] = 0;
                }
            }

            // build the new game state
            BoardData newState = new BoardData(stones, lastMove, color, !history.isBlacksTurn(), zobrist, moveNumber, moveNumberList);

            // don't make this coordinate if it is suicidal or violates superko
            if (isSuicidal || history.violatesSuperko(newState))
                return;

            // update history with this coordinate
            if (history.getHead().getNext() != null) {
                history.getHead().disconnectNextNode();
                observerCollection.mainStreamCut(history.getHead(), history.getHead());
            }
            history.add(newState);
            observerCollection.mainStreamAppended(history.getHead(), history.getHead());

            // update leelaz with board position
            final Stone colorToPlay = color;
            final String locationToPlay = convertCoordinatesToName(x, y);
            leelazExecutor.execute(() -> {
                Lizzie.leelaz.play(colorToPlay, locationToPlay);
            });
        }
    }

    /**
     * overloaded method for place(), chooses color in an alternating pattern
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void place(int x, int y) {
        place(x, y, history.isBlacksTurn() ? Stone.BLACK : Stone.WHITE);
    }

    /**
     * overloaded method for place. To be used by the LeelaZ engine. Color is then assumed to be alternating, anyway.
     *
     * @param namedCoordinate the coordinate to place a stone,
     */
    public void place(String namedCoordinate) {
        int[] coordinates = convertNameToCoordinates(namedCoordinate);

        place(coordinates[0], coordinates[1]);
    }

    /**
     * Removes a chain if it has no liberties
     *
     * @param x       x coordinate -- needn't be valid
     * @param y       y coordinate -- needn't be valid
     * @param color   the color of the chain to remove
     * @param stones  the stones array to modify
     * @param zobrist the zobrist object to modify
     * @return whether or not stones were removed
     */
    private boolean removeDeadChain(int x, int y, Stone color, Stone[] stones, Zobrist zobrist) {
        if (!isValid(x, y) || stones[getIndex(x, y)] != color)
            return false;

        boolean hasLiberties = hasLibertiesHelper(x, y, color, stones);

        // either remove stones or reset what hasLibertiesHelper does to the board
        cleanupHasLibertiesHelper(x, y, color.recursed(), stones, zobrist, !hasLiberties);

        // if hasLiberties is false, then we removed stones
        return !hasLiberties;
    }

    /**
     * Recursively determines if a chain has liberties. Alters the state of stones, so it must be counteracted
     *
     * @param x      x coordinate -- needn't be valid
     * @param y      y coordinate -- needn't be valid
     * @param color  the color of the chain to be investigated
     * @param stones the stones array to modify
     * @return whether or not this chain has liberties
     */
    private boolean hasLibertiesHelper(int x, int y, Stone color, Stone[] stones) {
        if (!isValid(x, y))
            return false;

        if (stones[getIndex(x, y)] == Stone.EMPTY)
            return true; // a liberty was found
        else if (stones[getIndex(x, y)] != color)
            return false; // we are either neighboring an enemy stone, or one we've already recursed on

        // set this index to be the recursed color to keep track of where we've already searched
        stones[getIndex(x, y)] = color.recursed();

        // set removeDeadChain to true if any recursive calls return true. Recurse in all 4 directions
        boolean hasLiberties = hasLibertiesHelper(x + 1, y, color, stones) ||
                hasLibertiesHelper(x, y + 1, color, stones) ||
                hasLibertiesHelper(x - 1, y, color, stones) ||
                hasLibertiesHelper(x, y - 1, color, stones);

        return hasLiberties;
    }

    /**
     * cleans up what hasLibertyHelper does to the board state
     *
     * @param x            x coordinate -- needn't be valid
     * @param y            y coordinate -- needn't be valid
     * @param color        color to clean up. Must be a recursed stone type
     * @param stones       the stones array to modify
     * @param zobrist      the zobrist object to modify
     * @param removeStones if true, we will remove all these stones. otherwise, we will set them to their unrecursed version
     */
    private void cleanupHasLibertiesHelper(int x, int y, Stone color, Stone[] stones, Zobrist zobrist, boolean removeStones) {
        if (!isValid(x, y) || stones[getIndex(x, y)] != color)
            return;

        stones[getIndex(x, y)] = removeStones ? Stone.EMPTY : color.unrecursed();
        if (removeStones)
            zobrist.toggleStone(x, y, color.unrecursed());

        // use the flood fill algorithm to replace all adjacent recursed stones
        cleanupHasLibertiesHelper(x + 1, y, color, stones, zobrist, removeStones);
        cleanupHasLibertiesHelper(x, y + 1, color, stones, zobrist, removeStones);
        cleanupHasLibertiesHelper(x - 1, y, color, stones, zobrist, removeStones);
        cleanupHasLibertiesHelper(x, y - 1, color, stones, zobrist, removeStones);
    }

    /**
     * get current board state
     *
     * @return the stones array corresponding to the current board state
     */
    public Stone[] getStones() {
        return history.getStones();
    }

    /**
     * shows where to mark the last coordinate
     *
     * @return the last played stone
     */
    public int[] getLastMove() {
        return history.getLastMove();
    }

    /**
     * get current board move number
     *
     * @return the int array corresponding to the current board move number
     */
    public int[] getMoveNumberList() {
        return history.getMoveNumberList();
    }

    /**
     * Goes to the next coordinate, thread safe
     */
    public boolean nextMove() {
        synchronized (this) {
            BoardHistoryNode oldHead = history.getHead();
            if (history.next() != null) {
                observerCollection.headMoved(oldHead, history.getHead());

                // update leelaz board position, before updating to next node
                final Stone colorToPlay = history.getLastMoveColor();
                if (history.getData().getLastMove() == null) {
                    leelazExecutor.execute(() -> {
                        Lizzie.leelaz.play(colorToPlay, "pass");
                    });
                } else {
                    final String locationToPlay = convertCoordinatesToName(history.getLastMove()[0], history.getLastMove()[1]);
                    leelazExecutor.execute(() -> {
                        Lizzie.leelaz.play(colorToPlay, locationToPlay);
                    });
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public BoardData getData() {
        return history.getData();
    }

    /**
     * Goes to the previous coordinate, thread safe
     */
    public boolean previousMove() {
        synchronized (this) {
            BoardHistoryNode oldHead = history.getHead();
            if (history.previous() != null) {
                observerCollection.headMoved(oldHead, history.getHead());

                leelazExecutor.execute(() -> {
                    Lizzie.leelaz.undo();
                });

                return true;
            } else {
                return false;
            }
        }
    }

    public int getMoveNumber(int x, int y) {
        return history.getMoveNumberList()[getIndex(x, y)];
    }

    public void gotoMove(int moveNumber) {
        if (moveNumber >= 0) {
            int currentMoveNumber = history.getMoveNumber();
            int moveNumberDiff = moveNumber - currentMoveNumber;

            gotoMoveByDiff(moveNumberDiff);
        }
    }

    public void gotoMoveByDiff(int moveDiff) {
        if (moveDiff > 0) {
            goForward(moveDiff);
        } else if (moveDiff < 0) {
            goBackward(-moveDiff);
        }
    }

    private void goForward(int count) {
        try (AutoCloseable closeable = Lizzie.leelaz.batchOperation()) {
            for (int i = 0; i < count; ++i) {
                if (!nextMove()) {
                    break;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void goBackward(int count) {
        try (AutoCloseable closeable = Lizzie.leelaz.batchOperation()) {
            for (int i = 0; i < count; ++i) {
                if (!previousMove()) {
                    break;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    public void dropSuccessiveMoves() {
        synchronized (this) {
            history.getHead().disconnectNextNode();
            observerCollection.mainStreamCut(history.getHead(), history.getHead());
        }
    }

    @Override
    public void close() {
        ThreadPoolUtil.shutdownAndAwaitTermination(leelazExecutor);
        if (bestMoveObserver != null) {
            Lizzie.leelaz.unregisterBestMoveObserver(bestMoveObserver);
            bestMoveObserver = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void playBestMove() {
        Optional<int[]> bestMove;
        synchronized (this) {
            bestMove = history.getData().getBestMove();
        }
        bestMove.ifPresent(coordinates -> {
            if (ArrayUtils.isNotEmpty(coordinates) && coordinates.length == 2 && Board.isValid(coordinates[0], coordinates[1])) {
                place(coordinates[0], coordinates[1]);
            } else {
                pass();
            }
        });
    }

    public int[] getNextMoveCoordinate() {
        BoardHistoryNode nextNode = history.getHead().getNext();
        if (nextNode == null) {
            return null;
        } else {
            return nextNode.getData().getLastMove();
        }
    }

    public void changeMove(int moveNumber, int[] convertedCoords) {
        if (moveNumber <= 0) {
            return;
        }

        int endMoveNumber = history.getEndNode().getData().getMoveNumber();
        if (moveNumber > endMoveNumber) {
            return;
        }

        // Does not support change move in try play state
        leaveTryPlayState();

        int currentMoveNumber = history.getMoveNumber();

        // Go to that number preceding
        gotoMove(moveNumber - 1);

        BoardHistoryNode needRestruct = history.getHead().getNext().getNext();

        // Fix move
        if (Board.isValid(convertedCoords)) {
            place(convertedCoords[0], convertedCoords[1]);
        } else {
            pass();
        }

        if (needRestruct != null) {
            for (BoardData data : needRestruct) {
                if (Board.isValid(data.getLastMove())) {
                    place(data.getLastMove()[0], data.getLastMove()[1]);
                } else {
                    pass();
                }
            }
        }

        gotoMove(currentMoveNumber);
    }

    public static void registerBoardSizeChangeObserver(Consumer<Integer> observer) {
        boardSizeChangeObserver.add(observer);
    }

    public static void unregisterBoardSizeChangeObserver(Consumer<Integer> observer) {
        boardSizeChangeObserver.remove(observer);
    }

    public static void changeBoardSize(final int newSize) {
        BOARD_SIZE = newSize;
        Lizzie.miscExecutor.execute(() -> boardSizeChangeObserver.forEach(observer -> observer.accept(newSize)));
    }
}
