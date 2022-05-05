package linda.shm;

import linda.Tuple;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

public class TupleLock {
    private final TupleLockPool lockPool;
    private final Tuple template;

    private CompletableFuture<Void> unlockedCallback;

    private final Semaphore semaphore = new Semaphore(0);
    private boolean wasLocked = false;

    public TupleLock(TupleLockPool lockPool, Tuple template) {
        this.lockPool = lockPool;
        this.template = template;
    }

    public void lock() {
        try {
            lockPool.incrLockedCount();
            wasLocked = true;
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Void> unlock() {
        unlockedCallback = new CompletableFuture<>();
        semaphore.release();
        return unlockedCallback;
    }

    /**
     * Method to call to ensure thread has been unlocked before others can run
     */
    public void unlocked() {
        if(!wasLocked) {
            return;
        }
        lockPool.decrLockedCount();
        wasLocked = false;
        unlockedCallback.complete(null);
    }

    public void destroy() {
        lockPool.destroy(this);
    }

    public Tuple getTemplate() {
        return template;
    }
}
