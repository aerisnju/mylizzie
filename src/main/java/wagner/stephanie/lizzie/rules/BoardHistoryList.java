package wagner.stephanie.lizzie.rules;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * Linked list data structure to store board history
 */
public class BoardHistoryList extends AbstractCollection<BoardData> {
    public static class BoardDataIterator implements Iterator<BoardData> {
        private BoardHistoryNode currentNode;

        BoardDataIterator(BoardHistoryNode currentNode) {
            this.currentNode = currentNode;
        }

        @Override
        public boolean hasNext() {
            return currentNode != null;
        }

        @Override
        public BoardData next() {
            if (hasNext()) {
                BoardHistoryNode resultNode = currentNode;
                currentNode = currentNode.getNext();
                return resultNode.getData();
            } else {
                return null;
            }
        }
    }

    private BoardHistoryNode initialNode;

    private BoardHistoryNode head;

    public BoardHistoryNode getHead() {
        return head;
    }

    public BoardHistoryNode getInitialNode() {
        return initialNode;
    }

    /**
     * Initialize a new board history list, whose first node is data
     *
     * @param data the data to be stored for the first entry
     */
    public BoardHistoryList(BoardData data) {
        initialNode = new BoardHistoryNode(data);
        head = initialNode;
    }

    /**
     * Add new data after head. Overwrites any data that may have been stored after head.
     *
     * @param data the data to add
     */
    @Override
    public boolean add(BoardData data) {
        BoardHistoryNode newNode = new BoardHistoryNode(data);

        head.connectNextNode(newNode);
        head = newNode;

        return true;
    }

    /**
     * moves the pointer to the left, returns the data stored there
     *
     * @return data of previous node, null if there is no previous node
     */
    public BoardData previous() {
        if (head.getPrevious() == null)
            return null;
        else
            head = head.getPrevious();

        return head.getData();
    }

    /**
     * moves the pointer to the right, returns the data stored there
     *
     * @return the data of next node, null if there is no next node
     */
    public BoardData next() {
        if (head.getNext() == null)
            return null;
        else
            head = head.getNext();

        return head.getData();
    }

    /**
     * Does not change the pointer position
     *
     * @return the data stored at the next index. null if not present
     */
    public BoardData getNext() {
        if (head.getNext() == null)
            return null;
        else
            return head.getNext().getData();
    }

    public BoardData getPrevious() {
        if (head.getPrevious() == null)
            return null;
        else
            return head.getPrevious().getData();
    }

    /**
     * @return the data of the current node
     */
    public BoardData getData() {
        return head.getData();
    }

    public Stone[] getStones() {
        return head.getData().getStonesOnBoard();
    }

    public int[] getLastMove() {
        return head.getData().getLastMove();
    }

    public Stone getLastMoveColor() {
        return head.getData().getLastMoveColor();
    }

    public boolean isBlacksTurn() {
        return head.getData().isBlackToPlay();
    }

    public Zobrist getZobrist() {
        return head.getData().getZobrist().clone();
    }

    public int getMoveNumber() {
        return head.getData().getMoveNumber();
    }

    public int[] getMoveNumberList() {
        return head.getData().getMoveNumberListOnBoard();
    }

    /**
     * @param data the board position to check against superko
     * @return whether or not the given position violates the superko rule at the head's state
     */
    public boolean violatesSuperko(BoardData data) {
        BoardHistoryNode head = this.head;

        // check to see if this position has occurred before
        while (head.getPrevious() != null) {
            // if two zobrist hashes are equal, and it's the same player to coordinate, they are the same position
            if (data.getZobrist().equals(head.getData().getZobrist()) && data.isBlackToPlay() == head.getData().isBlackToPlay())
                return true;

            head = head.getPrevious();
        }

        // no position matched this position, so it's valid
        return false;
    }

    @NotNull
    @Override
    public Iterator<BoardData> iterator() {
        return new BoardDataIterator(initialNode);
    }

    @Override
    public int size() {
        return initialNode.distanceToEnd() + 1;
    }
}
