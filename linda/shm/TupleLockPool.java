package linda.shm;

import linda.Tuple;
import sun.security.rsa.RSAUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TupleLockPool {
    private final Random rand = new Random();
    private List<TupleLock> locks = new ArrayList<>();

    private AtomicInteger lockedCount = new AtomicInteger();

    private CompletableFuture<Void> allUnlockedCallback;

    public TupleLockPool() {
    }

    public TupleLockPool(List<TupleLock> locks) {
        this.locks = locks;
    }

    public synchronized TupleLock create(Tuple template) {
        TupleLock lock = new TupleLock(this, template);
        locks.add(lock);
        return lock;
    }

    public synchronized void destroy(TupleLock lock) {
        locks.remove(lock);
    }

    public synchronized CompletableFuture<Void> unlockAll(Tuple tuple) {
        this.allUnlockedCallback = new CompletableFuture<>();
        List<TupleLock> locks = this.locks.stream()
                .filter(lock -> tuple.matches(lock.getTemplate()))
                .collect(Collectors.toList());
        // Aync to allow caller to set CompletableFuture's callback
        CompletableFuture.runAsync(() -> {
            if (locks.size() == 0) {
                allUnlockedCallback.complete(null);
            } else {
                lockedCount.set(locks.size());
                locks.forEach(TupleLock::unlock);
            }
        });
        return allUnlockedCallback;
    }

    public CompletableFuture<Void> unlockRandom(Tuple tuple) {
        List<TupleLock> locks = this.locks.stream().filter(lock -> tuple.matches(lock.getTemplate())).collect(Collectors.toList());
        if (locks.isEmpty()) {
            CompletableFuture<Void> callback = new CompletableFuture<>();
            CompletableFuture.runAsync(() -> callback.complete(null));
            return callback;
        }
        TupleLock lock = locks.get(rand.nextInt(locks.size()));
        return lock.unlock();
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
