package linda.shm;

import linda.Tuple;

import java.util.ArrayList;
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

    public void callAll(Tuple tuple) {
        List<TupleListener> callbacks;
        synchronized (this.callbacks) {
            callbacks = this.callbacks.stream().filter(future -> future.matches(tuple)).collect(Collectors.toList());
            List<CompletableFuture<Tuple>> futures = callbacks.stream().map(TupleListener::future).collect(Collectors.toList());
            System.out.println("Complete them all.");
            futures.forEach(future -> future.complete(tuple));
            // TODO : est-ce-que allOf attend bien que les threads soient relancés ?
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            /*
            OU
            CompletableFuture<Void> lock = waitFor(futures);
            futures.forEach(future -> future.complete(tuple));
            futures.join();
             */
        }
        removeAll(callbacks);
    }

    public boolean callOne(Tuple tuple) {
        Optional<TupleListener> first;
        synchronized (callbacks) {
            first = callbacks.stream().filter(callback -> callback.matches(tuple)).findFirst();
            if (!first.isPresent()) {
                return false;
            }
            CompletableFuture<Tuple> future = first.get().future();
            future.complete(tuple);
            future.join();
        }
        remove(first.get());
        return true;
    }

    private static CompletableFuture<Void> waitFor(CompletableFuture<?>... cfs) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        AtomicInteger count = new AtomicInteger(cfs.length);
        for (CompletableFuture<?> cf : cfs) {
            cf.thenRun(() -> {
                int val = count.decrementAndGet();
                if(val == 0) {
                    future.complete(null);
                }
            });
        }
        return future;
    }
}
