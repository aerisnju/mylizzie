package featurecat.lizzie.rules;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.OptionalInt;

/**
 * Node structure for a special doubly linked list
 */
public class BoardHistoryNode implements Iterable<BoardData> {
    private BoardHistoryNode previous;
    private BoardHistoryNode next;

    private BoardData data;

    private ArrayList<BoardHistoryNode> tryPlayHistory;

    /**
     * Initializes a new list node
     */
    public BoardHistoryNode(BoardData data) {
        previous = null;
        next = null;
        this.data = data;
        tryPlayHistory = new ArrayList<>();
    }

    /**
     * Sets up for a new node. Overwrites future history.
     *
     * @param node the node following this one
     */
    public void connectNextNode(BoardHistoryNode node) {
        next = node;
        if (node != null) {
            node.previous = this;
        }
    }

    public void disconnectNextNode() {
        BoardHistoryNode nextNode = next;
        next = null;
        if (nextNode != null) {
            nextNode.previous = null;
        }
    }

    /**
     * @return data stored on this node
     */
    public BoardData getData() {
        return data;
    }

    public BoardHistoryNode getPrevious() {
        return previous;
    }

    public BoardHistoryNode getNext() {
        return next;
    }

    public void setPrevious(BoardHistoryNode previous) {
        this.previous = previous;
    }

    public void setNext(BoardHistoryNode next) {
        this.next = next;
    }

    public ArrayList<BoardHistoryNode> getTryPlayHistory() {
        return tryPlayHistory;
    }

    public void addTryPlayHistory(BoardHistoryNode tryPlayBeginNode) {
        if (!tryPlayHistory.contains(tryPlayBeginNode)) {
            tryPlayHistory.add(tryPlayBeginNode);
            tryPlayBeginNode.previous = this;
        }
    }

    public boolean isJustBefore(BoardHistoryNode anotherNode) {
        if (next != anotherNode) {
            return false;
        }

        return anotherNode != null && anotherNode.previous == this;
    }

    public boolean isJustAfter(BoardHistoryNode anotherNode) {
        if (previous != anotherNode) {
            return false;
        }

        return previous != null && anotherNode.next == this;
    }

    public OptionalInt distanceTo(BoardHistoryNode anotherNode) {
        if (anotherNode == null) {
            return OptionalInt.empty();
        }

        // Forward
        int count = 0;
        for (BoardHistoryNode node = this; node != null; node = node.next, ++count) {
            if (node == anotherNode) {
                return OptionalInt.of(count);
            }
        }

        // Backaward
        count = 0;
        for (BoardHistoryNode node = this; node != null; node = node.previous, --count) {
            if (node == anotherNode) {
                return OptionalInt.of(count);
            }
        }

        return OptionalInt.empty();
    }

    public int distanceToEnd() {
        // Forward
        int count = 0;
        for (BoardHistoryNode node = this; node != null; node = node.next, ++count);
        return count;
    }

    private int distanceToBegin() {
        // Backaward
        int count = 0;
        for (BoardHistoryNode node = this; node != null; node = node.previous, --count);
        return count;
    }

    public void exchangeDataWith(BoardHistoryNode anotherNode) {
        if (anotherNode != null) {
            BoardData temp = data;
            data = anotherNode.data;
            anotherNode.data = temp;
        }
    }

    @NotNull
    @Override
    public Iterator<BoardData> iterator() {
        return new BoardHistoryList.BoardDataIterator(this);
    }
}
