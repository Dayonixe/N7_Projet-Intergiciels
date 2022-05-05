package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Shared memory implementation of Linda.
 */
public class CentralizedLinda implements Linda {
    private final TupleLockPool readerLocks = new TupleLockPool();
    private final TupleLockPool takerLocks = new TupleLockPool();

    private final List<Tuple> tuples = new ArrayList<>();
    private final Set<EventListener> listeners = new HashSet<>();

    public CentralizedLinda() {
    }

    @Override
    // Synchronized on tuples to prevent concurrent modification
    public void write(Tuple t) {
        synchronized (tuples) {
            tuples.add(t.deepclone());
        }

        // Unlock all readers that match the new tuple
        readerLocks.unlockAll(t).join();
        // Then unlock one random taker the match the new tuple
        takerLocks.unlockRandom(t).join();

        synchronized (tuples) {
            listeners.stream().filter(l -> t.matches(l.getTemplate()))
                    // intermediate list to prevent concurrent modification on tuples by tryListener
                    .collect(Collectors.toList()).forEach(this::tryListener);
        }
    }

    @Override
    public Tuple take(Tuple template) {
        TupleLock lock = takerLocks.create(template);
        Tuple tuple = getOrLock(template, true, lock);
        lock.destroy();
        return tuple;
    }

    @Override
    public Tuple read(Tuple template) {
        TupleLock lock = readerLocks.create(template);
        Tuple tuple = getOrLock(template, false, lock);
        lock.destroy();
        return tuple;
    }

    @Override
    public Tuple tryTake(Tuple template) {
        return get(template, true);
    }

    @Override
    public Tuple tryRead(Tuple template) {
        return get(template, false);
    }

    // Synchronized on tuples to prevent concurrent modification
    private synchronized Tuple get(Tuple template, boolean remove) {
        Iterator<Tuple> iterator = tuples.iterator();
        while (iterator.hasNext()) {
            Tuple tuple = iterator.next();
            if (tuple.matches(template)) {
                if (remove) {
                    iterator.remove();
                }
                return tuple;
            }
        }
        return null;
    }

    private Tuple getOrLock(Tuple template, boolean remove, TupleLock lock) {
        do {
            Tuple tuple = get(template, remove);
            lock.unlocked();
            if (tuple != null) {
                return tuple;
            }
            // Attendre qu'un nouveau tuple soit soumis
            // TODO : Optimiser en ne regardant que le dernier tuple (celui venant d'être ajouté) ?
            lock.lock();
        } while (true);
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        return getAll(template, true);
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        return getAll(template, false);
    }

    public synchronized Collection<Tuple> getAll(Tuple template, boolean remove) {
        Set<Tuple> tuples = new HashSet<>();
        Iterator<Tuple> iterator = this.tuples.iterator();
        while (iterator.hasNext()) {
            Tuple tuple = iterator.next();
            if (tuple.matches(template)) {
                if (remove) {
                    iterator.remove();
                }
                tuples.add(tuple);
            }
        }
        return tuples;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        EventListener listener = new EventListener(template, mode, timing, callback);
        listeners.add(listener);
        if (timing == eventTiming.IMMEDIATE) {
            tryListener(listener);
        }
    }

    private void tryListener(EventListener listener) {
        boolean remove = listener.getMode() == eventMode.TAKE;
        Tuple tuple = get(listener.getTemplate(), remove);
        if (tuple != null) {
            listener.getCallback().call(tuple);
        }
    }

    @Override
    public void debug(String prefix) {

    }
}
