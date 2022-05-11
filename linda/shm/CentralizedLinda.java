package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Shared memory implementation of Linda.
 */
public class CentralizedLinda implements Linda {
    private final List<Tuple> tuples = new ArrayList<>();

    // Sorted by creation time
    private final TupleListenerPool readers = new TupleListenerPool();
    // Sorted by creation time
    private final TupleListenerPool takers = new TupleListenerPool();

    public CentralizedLinda() {
    }

    @Override
    public synchronized void write(Tuple t) {
        Tuple newTuple = t.deepclone();

        // unlock readers
        System.out.println("Call all readers (" + readers.matchCount(t) + ")");
        // TODO: interblocage si call et add en même temps ?
        readers.callAll(t);

        // unlock one taker
        boolean taken = takers.callOne(t);
        System.out.println("Call one taker (" + taken + ")");

        if(taken) {
            return;
        }

        tuples.add(newTuple);
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

    private synchronized Tuple get(Tuple template, boolean remove) {
        Optional<Tuple> tuple = this.tuples.stream().filter(t -> t.matches(template)).findFirst();
        if(!tuple.isPresent()) {
            return null;
        }
        if(remove) {
            this.tuples.remove(tuple.get());
        }
        return tuple.get();
    }

    private CompletableFuture<Tuple> getAsync(Tuple template, boolean remove) {
        eventMode mode = remove ? eventMode.TAKE : eventMode.READ;
        eventTiming timing = eventTiming.IMMEDIATE;
        CompletableFuture<Tuple> future = new CompletableFuture<>();
        eventRegister(mode, timing, template, future::complete);
        return future;
    }

    public synchronized Collection<Tuple> getAll(Tuple template, boolean remove) {
        List<Tuple> tuples = this.tuples.stream()
                .filter(t -> t.matches(template)).collect(Collectors.toList());
        if(remove) {
            this.tuples.removeAll(tuples);
        }
        return tuples;
    }

    @Override
    public synchronized void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        if (timing == eventTiming.IMMEDIATE) {
            Tuple tuple = get(template, mode == eventMode.TAKE);

            if (tuple != null) {
                System.out.println("Immediate call");
                // Si on est en mode immédiat et que le listener est parvenu à récupérer un tuple
                // -> pas besoin de l'ajouter à la liste
                callback.call(tuple);
                return;
            }
        }

        if (mode == eventMode.READ) {
            readers.add(new TupleListener(template, callback));
        } else {
            takers.add(new TupleListener(template, callback));
        }
    }

    @Override
    public void debug(String prefix) {

    }
}
