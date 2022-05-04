package linda.shm;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Locks {
    private Set<Lock> locks = new HashSet<>();

    private AtomicInteger lockedCount = new AtomicInteger();

    private CompletableFuture<Void> allUnlockedCallback;

    public synchronized Lock create() {
        Lock lock = new Lock(this);
        locks.add(lock);
        return lock;
    }

    public synchronized void destroy(Lock lock) {
        locks.remove(lock);
    }

    public synchronized CompletableFuture<Void> unlockAll() {
        allUnlockedCallback = new CompletableFuture<>();
        // Aync to allow caller to set CompletableFuture's callback
        CompletableFuture.runAsync(() -> {
            if (locks.size() == 0) {
                allUnlockedCallback.complete(null);
            } else {
                locks.forEach(Lock::unlock);
            }
        });
        return allUnlockedCallback;
    }

    void incrLockedCount() {
        lockedCount.incrementAndGet();
    }

    void decrLockedCount() {
        int lockedCount = this.lockedCount.decrementAndGet();
        if (allUnlockedCallback != null && lockedCount == 0) {
            allUnlockedCallback.complete(null);
            allUnlockedCallback = null;
        }
    }
}
