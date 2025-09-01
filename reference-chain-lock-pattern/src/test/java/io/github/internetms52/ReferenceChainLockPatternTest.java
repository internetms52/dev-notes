package io.github.internetms52;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReferenceChainLockPatternTest {
    ReferenceChainLockPattern referenceChainLockPattern = new ReferenceChainLockPattern();

    @Test
    public void testConcurrentSameKey() {
        int threadCount = 100;
        String key = "testKey";
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 創建5個 CompletableFuture 任務
        CompletableFuture<Void>[] tasks = new CompletableFuture[threadCount];

        for (int i = 0; i < threadCount; i++) {
            tasks[i] = CompletableFuture.runAsync(() -> {
                try {
                    Semaphore semaphore = ReferenceChainLockPattern.tryLock(key);
                    System.out.println(Thread.currentThread().getId() + " is working.");
                    // 模擬工作時間
                    double sleepSeconds = 100 * Math.random();
                    Thread.sleep((int) sleepSeconds);
                    counter.incrementAndGet();
                    ReferenceChainLockPattern.releaseLock(semaphore, key);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                    System.out.println(Thread.currentThread().getId() + " is finished.");
                }
            });
        }

        CompletableFuture.allOf(tasks).join();
        // 等待所有任務完成
        Arrays.stream(tasks).forEach(voidCompletableFuture -> {
            try {
                voidCompletableFuture.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertEquals(threadCount, counter.get()); // 所有線程都應該成功執行
    }
}
