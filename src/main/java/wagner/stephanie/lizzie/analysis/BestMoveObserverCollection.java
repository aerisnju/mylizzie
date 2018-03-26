package wagner.stephanie.lizzie.analysis;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BestMoveObserverCollection extends AbstractCollection<BestMoveObserver> implements BestMoveObserver {
    private List<BestMoveObserver> observerList;

    public BestMoveObserverCollection() {
        observerList = new LinkedList<>();
    }

    public List<BestMoveObserver> getObserverList() {
        return observerList;
    }

    public void setObserverList(List<BestMoveObserver> observerList) {
        this.observerList = observerList;
    }

    @Override
    public void bestMovesUpdated(int boardStateCount, List<MoveData> newBestMoves) {
        observerList.forEach(observer -> observer.bestMovesUpdated(boardStateCount, newBestMoves));
    }

    @Override
    public void engineRestarted() {
        observerList.forEach(BestMoveObserver::engineRestarted);
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
    public boolean add(BestMoveObserver observer) {
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
    public boolean addAll(Collection<? extends BestMoveObserver> c) {
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
    public boolean removeIf(Predicate<? super BestMoveObserver> filter) {
        return observerList.removeIf(filter);
    }

    @Override
    public Spliterator<BestMoveObserver> spliterator() {
        return observerList.spliterator();
    }

    @Override
    public Stream<BestMoveObserver> stream() {
        return observerList.stream();
    }

    @Override
    public Stream<BestMoveObserver> parallelStream() {
        return observerList.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super BestMoveObserver> action) {
        observerList.forEach(action);
    }

    @Override
    public Iterator<BestMoveObserver> iterator() {
        return observerList.iterator();
    }

    @Override
    public int size() {
        return observerList.size();
    }
}
