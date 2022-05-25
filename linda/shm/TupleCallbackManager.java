package linda.shm;

import linda.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Gère une liste de TupleCallback et fournit des méthodes pour appeler les cabllacks dont le motif
 * correspondent à un certain tuple.
 */
public class TupleCallbackManager {
    private final List<TupleCallback> callbacks;

    public TupleCallbackManager() {
        this(new ArrayList<>());
    }

    public TupleCallbackManager(List<TupleCallback> callbacks) {
        this.callbacks = callbacks;
    }

    /**
     * Ajoute un callback
     * @param callback
     */
    public synchronized void add(TupleCallback callback) {
        this.callbacks.add(callback);
    }

    /**
     * Supprime une liste de callbacks
     * @param callbacks
     */
    public synchronized void removeAll(List<TupleCallback> callbacks) {
        this.callbacks.removeAll(callbacks);
    }

    /**
     * Supprime un callback
     * @param callback
     */
    public synchronized void remove(TupleCallback callback) {
        this.callbacks.remove(callback);
    }

    /**
     * Compte le nombre de callbacks dont le motif correspond au tuple.
     * @param tuple Tuple auquel le motif correspond
     * @return nombre de callbacks dont le motif correspond au tuple.
     */
    public synchronized int matchCount(Tuple tuple) {
        return (int) this.callbacks.stream().filter(future -> future.matches(tuple)).count();
    }

    /**
     * Appelle tous les callback dont le motif correspond au tuple.
     * @param tuple Tuple auquel le motif correspond
     */
    public void callAll(Tuple tuple) {
        List<TupleCallback> callbacks;
        synchronized (this) {
            callbacks = this.callbacks.stream().filter(future -> future.matches(tuple)).collect(Collectors.toList());

            removeAll(callbacks);
        }

        callbacks.forEach(future -> future.complete(tuple.deepclone()));

        CompletableFuture.allOf(callbacks.toArray(new CompletableFuture[0]));
    }

    /**
     * Appelle le premier callback dont le motif correspond au tuple.
     * @param tuple Tuple auquel le motif correspond
     */
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
