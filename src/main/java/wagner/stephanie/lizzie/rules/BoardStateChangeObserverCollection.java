package wagner.stephanie.lizzie.rules;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BoardStateChangeObserverCollection extends AbstractCollection<BoardStateChangeObserver> implements BoardStateChangeObserver {
    private List<BoardStateChangeObserver> observerList;

    public BoardStateChangeObserverCollection() {
        observerList = new CopyOnWriteArrayList<>();
    }

    @Override
    public void mainStreamAppended(BoardHistoryNode newNodeBegin, BoardHistoryNode head) {
        observerList.forEach(boardStateChangeObserver -> boardStateChangeObserver.mainStreamAppended(newNodeBegin, head));
    }

    @Override
    public void mainStreamCut(BoardHistoryNode nodeBeforeCutPoint, BoardHistoryNode head) {
        observerList.forEach(boardStateChangeObserver -> boardStateChangeObserver.mainStreamCut(nodeBeforeCutPoint, head));
    }

    @Override
    public void headMoved(BoardHistoryNode oldHead, BoardHistoryNode newHead) {
        observerList.forEach(boardStateChangeObserver -> boardStateChangeObserver.headMoved(oldHead, newHead));
    }

    @Override
    public void boardCleared() {
        observerList.forEach(BoardStateChangeObserver::boardCleared);
    }

    @Override
    public boolean isEmpty() {
        return observerList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return observerList.contains(o);
    }

    @Override
    public Object[] toArray() {
        return observerList.toArray();
    }

    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return observerList.toArray(a);
    }

    @Override
    public boolean add(BoardStateChangeObserver observer) {
        return observerList.add(observer);
    }

    @Override
    public boolean remove(Object o) {
        return observerList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return observerList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends BoardStateChangeObserver> c) {
        return observerList.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return observerList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return observerList.retainAll(c);
    }

    @Override
    public void clear() {
        observerList.clear();
    }

    @Override
    public String toString() {
        return observerList.toString();
    }

    @Override
    public boolean removeIf(Predicate<? super BoardStateChangeObserver> filter) {
        return observerList.removeIf(filter);
    }

    @Override
    public Spliterator<BoardStateChangeObserver> spliterator() {
        return observerList.spliterator();
    }

    @Override
    public Stream<BoardStateChangeObserver> stream() {
        return observerList.stream();
    }

    @Override
    public Stream<BoardStateChangeObserver> parallelStream() {
        return observerList.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super BoardStateChangeObserver> action) {
        observerList.forEach(action);
    }

    @Override
    public Iterator<BoardStateChangeObserver> iterator() {
        return observerList.iterator();
    }

    @Override
    public int size() {
        return observerList.size();
    }
}
