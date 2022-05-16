package linda.shm;

import linda.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TupleCallbackManager {
    private final List<TupleCallback> callbacks;

    public TupleCallbackManager() {
        this(new ArrayList<>());
    }

    public TupleCallbackManager(List<TupleCallback> callbacks) {
        this.callbacks = callbacks;
    }

    public synchronized void add(TupleCallback callback) {
        this.callbacks.add(callback);
    }

    public synchronized void removeAll(List<TupleCallback> callbacks) {
        this.callbacks.removeAll(callbacks);
    }

    public synchronized void remove(TupleCallback callback) {
        this.callbacks.remove(callback);
    }

    public synchronized List<TupleCallback> getAll() {
        return this.callbacks;
    }

    public synchronized int matchCount(Tuple tuple) {
        return (int) this.callbacks.stream().filter(future -> future.matches(tuple)).count();
    }

    public synchronized void callAll(Tuple tuple) {
        List<TupleCallback> callbacks = this.callbacks.stream().filter(future -> future.matches(tuple)).collect(Collectors.toList());
        callbacks.forEach(future -> future.complete(tuple));

        CompletableFuture.allOf(callbacks.toArray(new CompletableFuture[0]));

        removeAll(callbacks);
    }

    public synchronized boolean callOne(Tuple tuple) {
        Optional<TupleCallback> callbackOptional;
        callbackOptional = callbacks.stream().filter(callback -> callback.matches(tuple)).findFirst();
        if (!callbackOptional.isPresent()) {
            return false;
        }
        TupleCallback callback = callbackOptional.get();
        callback.complete(tuple);
        callback.join();
        remove(callback);
        return true;
    }
}
