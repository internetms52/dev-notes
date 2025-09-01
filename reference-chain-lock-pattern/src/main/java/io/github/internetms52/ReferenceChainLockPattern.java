package io.github.internetms52;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ReferenceChainLockPattern {
    private final static ConcurrentHashMap<String, Semaphore> memberSemaphores = new ConcurrentHashMap<>();

    static Semaphore tryLock(String key) throws InterruptedException {
        Semaphore lock = memberSemaphores.computeIfAbsent(key, k -> new Semaphore(1));
        lock.acquire();
        return lock;
    }

    static void releaseLock(Semaphore semaphore, String key) {
        memberSemaphores.remove(key, semaphore);
        semaphore.release(); // 直接釋放，讓 Semaphore 自己處理
    }
}
