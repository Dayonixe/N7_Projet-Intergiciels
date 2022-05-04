package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.*;

/**
 * Shared memory implementation of Linda.
 */
public class CentralizedLinda implements Linda {
    private final List<Tuple> tuples = new ArrayList<>();

    public CentralizedLinda() {
    }

    @Override
    public synchronized void write(Tuple t) {
        tuples.add(t.deepclone());
        // On débloque tous les threads en attente de typle pour qu'il vérifie si ce nouveau tuple leur convient
        notifyAll();
    }

    @Override
    public synchronized Tuple take(Tuple template) {
        Iterator<Tuple> iterator = tuples.iterator();
        while (iterator.hasNext()) {
            Tuple tuple = iterator.next();
            if (tuple.matches(template)) {
                iterator.remove();
                return tuple;
            }
        }
        // Attendre qu'il y en ait un qui match
        // TODO : Optimiser en ne regardant que le dernier tuple (celui venant d'être ajouté) ?
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return take(template);
    }

    @Override
    public synchronized Tuple read(Tuple template) {
        for (Tuple tuple : tuples) {
            if (tuple.matches(template)) {
                return tuple;
            }
        }
        // Attendre qu'un nouveau tuple soit soumit
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return read(template);
    }

    @Override
    public synchronized Tuple tryTake(Tuple template) {
        Iterator<Tuple> iterator = tuples.iterator();
        while (iterator.hasNext()) {
            Tuple tuple = iterator.next();
            if (tuple.matches(template)) {
                iterator.remove();
                return tuple;
            }
        }
        // Attendre qu'il y en ait un qui match
        return null;
    }

    @Override
    public synchronized Tuple tryRead(Tuple template) {
        for (Tuple tuple : tuples) {
            if (tuple.matches(template)) {
                return tuple;
            }
        }
        return null;
    }

    @Override
    public synchronized Collection<Tuple> takeAll(Tuple template) {
        Set<Tuple> ans = new HashSet<>();
        Iterator<Tuple> iterator = tuples.iterator();
        while (iterator.hasNext()) {
            Tuple tuple = iterator.next();
            if (tuple.matches(template)) {
                iterator.remove();
                ans.add(tuple);
            }
        }
        return ans;
    }

    @Override
    public synchronized Collection<Tuple> readAll(Tuple template) {
        Set<Tuple> ans = new HashSet<>();
        for (Tuple tuple : tuples) {
            if (tuple.matches(template)) {
                ans.add(tuple);
            }
        }
        return ans;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {

    }

    @Override
    public void debug(String prefix) {

    }

    // TO BE COMPLETED

}
