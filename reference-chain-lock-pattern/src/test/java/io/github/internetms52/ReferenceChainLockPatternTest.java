package io.github.internetms52;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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

    @Test
    public void testConcurrentRandomKeys() {
        int threadCount = 100;
        int keySpaceSize = 10; // 10個不同的key，會有重複
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 用來記錄每個key被訪問的次數
        ConcurrentHashMap<String, AtomicInteger> keyUsageCount = new ConcurrentHashMap<>();

        CompletableFuture<Void>[] tasks = new CompletableFuture[threadCount];

        for (int i = 0; i < threadCount; i++) {
            tasks[i] = CompletableFuture.runAsync(() -> {
                try {
                    // 生成隨機key
                    String key = "testKey_" + (int) (Math.random() * keySpaceSize);
                    keyUsageCount.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();

                    Semaphore semaphore = ReferenceChainLockPattern.tryLock(key);
                    System.out.println(Thread.currentThread().getId() + " acquired lock for key: " + key);

                    // 模擬工作時間
                    double sleepSeconds = 50 + 50 * Math.random(); // 50-100ms
                    Thread.sleep((int) sleepSeconds);

                    counter.incrementAndGet();
                    ReferenceChainLockPattern.releaseLock(semaphore, key);
                    System.out.println(Thread.currentThread().getId() + " released lock for key: " + key);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            // 等待所有任務完成
            CompletableFuture.allOf(tasks).join();
            latch.await();

            // 驗證所有線程都成功執行
            assertEquals(threadCount, counter.get());

            // 打印每個key的使用統計
            System.out.println("\nKey usage statistics:");
            keyUsageCount.entrySet().stream()
                    .sorted(Map.Entry.<String, AtomicInteger>comparingByKey())
                    .forEach(entry ->
                            System.out.println(entry.getKey() + ": " + entry.getValue().get() + " times")
                    );

            // 驗證所有key使用次數的總和等於線程數
            int totalKeyUsage = keyUsageCount.values().stream()
                    .mapToInt(AtomicInteger::get)
                    .sum();
            assertEquals(threadCount, totalKeyUsage);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assertions.fail();
        }
    }
}
