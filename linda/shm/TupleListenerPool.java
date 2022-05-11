package linda.shm;

import linda.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TupleListenerPool {
    private final List<TupleListener> callbacks = new ArrayList<>();

    public synchronized void add(TupleListener callback) {
        this.callbacks.add(callback);
    }

    public synchronized void removeAll(List<TupleListener> callbacks) {
        this.callbacks.removeAll(callbacks);
    }

    public synchronized void remove(TupleListener callback) {
        this.callbacks.remove(callback);
    }

    public synchronized int matchCount(Tuple tuple) {
        return (int) this.callbacks.stream().filter(future -> future.matches(tuple)).count();
    }

    public synchronized void callAll(Tuple tuple) {
        List<TupleListener> callbacks = this.callbacks.stream().filter(future -> future.matches(tuple)).collect(Collectors.toList());
        List<CompletableFuture<Tuple>> futures = callbacks.stream().map(TupleListener::future).collect(Collectors.toList());
        futures.forEach(future -> future.complete(tuple));

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        removeAll(callbacks);
    }

    public synchronized boolean callOne(Tuple tuple) {
        Optional<TupleListener> first;
        first = callbacks.stream().filter(callback -> callback.matches(tuple)).findFirst();
        if (!first.isPresent()) {
            return false;
        }
        CompletableFuture<Tuple> future = first.get().future();
        future.complete(tuple);
        future.join();
        remove(first.get());
        return true;
    }
}
