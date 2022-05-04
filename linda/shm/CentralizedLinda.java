package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Shared memory implementation of Linda.
 */
public class CentralizedLinda implements Linda {
    private final List<Tuple> tuples = new ArrayList<>();
    private final Set<EventListener> listeners = new HashSet<>();

    public CentralizedLinda() {
    }

    @Override
    public synchronized void write(Tuple t) {
        tuples.add(t.deepclone());
        // On débloque tous les threads en attente de typle pour qu'il vérifie si ce nouveau tuple leur convient
        notifyAll();

        listeners.stream().filter(l -> t.matches(l.getTemplate()))
                // intermediate list to prevent concurrent modification on tuples by tryListener
                .collect(Collectors.toList()).forEach(this::tryListener);
    }

    @Override
    public Tuple take(Tuple template) {
        return get(template, true, this::take);
    }

    @Override
    public Tuple read(Tuple template) {
        return get(template, false, this::read);
    }

    @Override
    public synchronized Tuple tryTake(Tuple template) {
        return get(template, true, null);
    }

    @Override
    public synchronized Tuple tryRead(Tuple template) {
        return get(template, false, null);
    }

    private synchronized Tuple get(Tuple template, boolean remove, Function<Tuple, Tuple> callAfterBlock) {
        Iterator<Tuple> iterator = tuples.iterator();
        while(iterator.hasNext()) {
            Tuple tuple = iterator.next();
            if (tuple.matches(template)) {
                if(remove) {
                    iterator.remove();
                }
                return tuple;
            }
        }

        if(callAfterBlock == null) {
            return null;
        }

        // Attendre qu'un nouveau tuple soit soumit
        // TODO : Optimiser en ne regardant que le dernier tuple (celui venant d'être ajouté) ?
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return callAfterBlock.apply(template);
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
        EventListener listener = new EventListener(template, mode, timing, callback);
        listeners.add(listener);
        if(timing == eventTiming.IMMEDIATE) {
            tryListener(listener);
        }
    }

    private void tryListener(EventListener listener) {
        boolean remove = listener.getMode() == eventMode.TAKE;
        Tuple tuple = get(listener.getTemplate(), remove, null);
        if(tuple != null) {
            listener.getCallback().call(tuple);
        }
    }

    @Override
    public void debug(String prefix) {

    }
}
