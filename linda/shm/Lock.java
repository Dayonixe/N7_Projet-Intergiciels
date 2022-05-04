package linda.shm;

import java.util.concurrent.Semaphore;

public class Lock {
    private final Semaphore semaphore = new Semaphore(0);

    private final LockPool lockPool;

    private boolean wasLocked = false;

    public Lock(LockPool lockPool) {
        this.lockPool = lockPool;
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

    public void unlock() {
        semaphore.release();
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
    }

    public void destroy() {
        lockPool.destroy(this);
    }
}
