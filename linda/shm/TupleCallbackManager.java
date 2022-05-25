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

    public synchronized int matchCount(Tuple tuple) {
        return (int) this.callbacks.stream().filter(future -> future.matches(tuple)).count();
    }

    public void callAll(Tuple tuple) {
        List<TupleCallback> callbacks;
        synchronized (this) {
            callbacks = this.callbacks.stream().filter(future -> future.matches(tuple)).collect(Collectors.toList());

            removeAll(callbacks);
        }

        callbacks.forEach(future -> future.complete(tuple.deepclone()));

        CompletableFuture.allOf(callbacks.toArray(new CompletableFuture[0]));
    }

    public synchronized boolean callOne(Tuple tuple) {
        Optional<TupleCallback> callbackOptional;
        synchronized (this) {
            callbackOptional = callbacks.stream().filter(callback -> callback.matches(tuple)).findFirst();
            if (!callbackOptional.isPresent()) {
                return false;
            }
            callbackOptional.get().complete(tuple.deepclone());
        }
        TupleCallback callback = callbackOptional.get();
        callback.join();
        remove(callback);
        return true;
    }
}
