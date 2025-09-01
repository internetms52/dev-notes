package io.github.internetms52;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ReferenceChainLockPattern {
    private final ConcurrentHashMap<String, ReentrantLock> memberLocks = new ConcurrentHashMap<>();

    boolean tryLock(String key) throws InterruptedException {
        ReentrantLock lock = memberLocks.computeIfAbsent(key, k -> new ReentrantLock());
        return lock.tryLock(30, TimeUnit.SECONDS);
    }

    void releaseLock(String memberUid) {
        ReentrantLock lock = memberLocks.remove(memberUid);
        if (lock != null) {
            lock.unlock();
        }
    }

}
