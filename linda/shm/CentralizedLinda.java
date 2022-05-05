package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Shared memory implementation of Linda.
 */
public class CentralizedLinda implements Linda {
    private final List<Tuple> tuples = new ArrayList<>();

    // Sorted by creation time
    private final CallbackPool readListeners = new CallbackPool();
    // Sorted by creation time
    private final CallbackPool takeListeners = new CallbackPool();

    public CentralizedLinda() {
    }

    @Override
    // Synchronized on tuples to prevent concurrent modification
    public void write(Tuple t) {
        Tuple newTuple = t.deepclone();

        // unlock readers
        System.out.println("Call all readers (" + readListeners.matchCount(t) + ")");
        readListeners.callAll(t);

        // unlock takers
        boolean taken = takeListeners.callOne(t);
        System.out.println("Call one taker (" + taken + ")");

        if(taken) {
            return;
        }

        synchronized (tuples) {
            tuples.add(newTuple);
        }
    }

    @Override
    public Tuple take(Tuple template) {
        System.out.println("Take " + template);
        return getAsync(template, true).join();
    }

    @Override
    public Tuple read(Tuple template) {
        System.out.println("Before read");
        Tuple tuple = getAsync(template, false).join();
        System.out.println("After read");
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

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        return getAll(template, true);
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        return getAll(template, false);
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

    private CompletableFuture<Tuple> getAsync(Tuple template, boolean remove) {
        eventMode mode = remove ? eventMode.TAKE : eventMode.READ;
        eventTiming timing = eventTiming.IMMEDIATE;
        CompletableFuture<Tuple> future = new CompletableFuture<>();
        eventRegister(mode, timing, template, future::complete);
        return future;
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
    public synchronized void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        if (timing == eventTiming.IMMEDIATE) {
            Tuple tuple;
            if (mode == eventMode.READ) {
                tuple = tryRead(template);
            } else {
                tuple = tryTake(template);
            }
            if (tuple != null) {
                System.out.println("Immediate call");
                // Si on est en mode immédiat et que le listener est parvenu à récupérer un tuple
                // -> pas besoin de l'ajouter à la liste
                callback.call(tuple);
                return;
            }
        }
//        CompletableFuture<Tuple> completableFuture = new CompletableFuture<>();
//        completableFuture.thenAccept(callback::call);

        if (mode == eventMode.READ) {
            readListeners.add(new FutureTuple(template, callback));
        } else {
            takeListeners.add(new FutureTuple(template, callback));
        }
    }

    @Override
    public void debug(String prefix) {

    }
}
