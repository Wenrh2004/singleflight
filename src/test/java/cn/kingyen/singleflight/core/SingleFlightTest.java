package cn.kingyen.singleflight.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SingleFlightTest {

    @Test
    void testBasicExecution() {
        SingleFlight<String, String> sf = new SingleFlight<>();
        AtomicInteger counter = new AtomicInteger(0);

        String result = sf.doOnce("test-key", () -> {
            counter.incrementAndGet();
            return "result";
        });

        assertEquals("result", result);
        assertEquals(1, counter.get());

        // 第二次调用应该执行新的操作
        String result2 = sf.doOnce("test-key", () -> {
            counter.incrementAndGet();
            return "result2";
        });

        assertEquals("result2", result2);
        assertEquals(2, counter.get());
    }

    @Test
    void testFutureExecution() throws ExecutionException, InterruptedException {
        SingleFlight<String, String> sf = new SingleFlight<>();
        AtomicInteger counter = new AtomicInteger(0);

        CompletableFuture<String> future = sf.doOnceFuture("test-key", () -> {
            counter.incrementAndGet();
            return "future-result";
        });

        assertEquals("future-result", future.get());
        assertEquals(1, counter.get());
    }

    @Test
    void testConcurrentCallsExecuteOnlyOnce() throws InterruptedException {
        SingleFlight<String, Integer> sf = new SingleFlight<>();
        AtomicInteger counter = new AtomicInteger(0);
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    Integer result = sf.doOnce("same-key", () -> {
                        // 模拟耗时操作
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return counter.incrementAndGet();
                    });
                    assertEquals(1, result.intValue());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(1, counter.get());
    }

    @Test
    void testConcurrentFutureCallsExecuteOnlyOnce() throws InterruptedException, ExecutionException, TimeoutException {
        SingleFlight<String, Integer> sf = new SingleFlight<>();
        AtomicInteger counter = new AtomicInteger(0);
        int numThreads = 10;

        List<CompletableFuture<Integer>> futures = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numThreads);

        // 创建多个线程同时发起请求
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    // 等待统一开始信号
                    startLatch.await();

                    CompletableFuture<Integer> future = sf.doOnceFuture("same-key", () -> {
                        // 模拟耗时操作
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return counter.incrementAndGet();
                    });
                    futures.add(future);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // 发出开始信号，所有线程同时开始执行
        startLatch.countDown();

        // 等待所有线程完成提交任务
        completionLatch.await();

        // 等待所有Future完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        allFutures.get(5, TimeUnit.SECONDS);

        // 关闭线程池
        executor.shutdown();

        // 验证所有Future返回相同的结果
        for (CompletableFuture<Integer> future : futures) {
            assertEquals(1, future.get().intValue());
        }

        assertEquals(1, counter.get());
    }

    @Test
    void testDifferentKeysExecuteSeparately() {
        SingleFlight<String, Integer> sf = new SingleFlight<>();
        AtomicInteger counter = new AtomicInteger(0);

        Integer result1 = sf.doOnce("key1", counter::incrementAndGet);
        Integer result2 = sf.doOnce("key2", counter::incrementAndGet);

        assertEquals(1, result1.intValue());
        assertEquals(2, result2.intValue());
        assertEquals(2, counter.get());
    }

    @Test
    void testExceptionHandling() {
        SingleFlight<String, String> sf = new SingleFlight<>();
        RuntimeException expectedException = new RuntimeException("Test exception");

        Exception actualException = assertThrows(RuntimeException.class, () -> sf.doOnce("error-key", () -> {
            throw expectedException;
        }));

        assertEquals(expectedException, actualException);
    }

    @Test
    void testFutureExceptionHandling() {
        SingleFlight<String, String> sf = new SingleFlight<>();
        RuntimeException expectedException = new RuntimeException("Test exception");

        CompletableFuture<String> future = sf.doOnceFuture("error-key", () -> {
            throw expectedException;
        });

        ExecutionException executionException = assertThrows(ExecutionException.class, future::get);
        assertEquals(expectedException, executionException.getCause());
    }

    @Test
    void testForgetCancelsOperation() {
        SingleFlight<String, String> sf = new SingleFlight<>();

        // 启动一个长时间运行的任务
        // 创建一个线程池
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // 在新线程中执行长时间任务
        CompletableFuture<String> future;
        try {
            future = CompletableFuture.supplyAsync(() -> sf.doOnceFuture("long-task", () -> {
                try {
                    Thread.sleep(10000); // 长时间任务
                    return "completed";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }), executor).thenCompose(f -> f);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }

        // 确保任务已经开始
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 取消任务
        boolean forgotten = sf.forget("long-task");
        assertTrue(forgotten);

        // 验证future已经被异常完成
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("cancelled"));
    }

    @Test
    void testForgetNonExistingKey() {
        SingleFlight<String, String> sf = new SingleFlight<>();

        boolean result = sf.forget("non-existing-key");
        assertFalse(result);
    }

    @Test
    void testPendingCount() {
        SingleFlight<String, String> sf = new SingleFlight<>();

        assertEquals(0, sf.pendingCount());

        // 启动一个长时间运行的任务
        // 创建一个线程池
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // 在新线程中执行长时间任务
        CompletableFuture<String> future;
        try {
            future = CompletableFuture.supplyAsync(() -> sf.doOnceFuture("count-task", () -> {
                try {
                    Thread.sleep(500);
                    return "done";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }), executor).thenCompose(f -> f);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }

        // 确保任务已经开始但还未完成
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertEquals(1, sf.pendingCount());

        // 等待任务完成
        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail("Task should complete normally");
        }

        // 给一些时间进行清理
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertEquals(0, sf.pendingCount());
    }

    @Test
    @Timeout(value = 5)
    void testConcurrentCallsWithDifferentKeys() throws InterruptedException {
        SingleFlight<String, Integer> sf = new SingleFlight<>();
        int numThreads = 100;
        int numKeys = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

        for (int i = 0; i < numKeys; i++) {
            counters.put("key-" + i, new AtomicInteger(0));
        }

        for (int i = 0; i < numThreads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    String key = "key-" + (index % numKeys);
                    Integer result = sf.doOnce(key, () -> {
                        // 模拟耗时操作
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return counters.get(key).incrementAndGet();
                    });
                    assertEquals(1, result.intValue());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 验证每个key只执行了一次
        for (int i = 0; i < numKeys; i++) {
            assertEquals(1, counters.get("key-" + i).get());
        }
    }

    @Test
    void testReusabilityAfterCompletion() {
        SingleFlight<String, Integer> sf = new SingleFlight<>();
        AtomicInteger counter = new AtomicInteger(0);

        // 第一次调用
        Integer result1 = sf.doOnce("reuse-key", counter::incrementAndGet);
        assertEquals(1, result1.intValue());

        // 确保第一次调用已完成
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 第二次调用同一个key
        Integer result2 = sf.doOnce("reuse-key", counter::incrementAndGet);
        assertEquals(2, result2.intValue());

        assertEquals(2, counter.get());
    }

    @Test
    void testNestedCalls() {
        SingleFlight<String, String> sf = new SingleFlight<>();
        AtomicInteger counter = new AtomicInteger(0);

        String result = sf.doOnce("outer-key", () -> {
            counter.incrementAndGet();
            // 在一个调用内部进行另一个调用
            return sf.doOnce("inner-key", () -> {
                counter.incrementAndGet();
                return "nested-result";
            });
        });

        assertEquals("nested-result", result);
        assertEquals(2, counter.get());
    }

    @Test
    void testConcurrentForget() throws InterruptedException {
        SingleFlight<String, String> sf = new SingleFlight<>();
        int numThreads = 5;
        ExecutorService taskExecutor = Executors.newSingleThreadExecutor(); // 用于长任务的执行器
        ExecutorService forgetExecutor = Executors.newFixedThreadPool(numThreads); // 用于forget操作的执行器
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        // 在新线程中执行长时间任务
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sf.doOnceFuture("forget-key", () -> {
            try {
                Thread.sleep(1000);
                return "result";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }), taskExecutor).thenCompose(f -> f);

        // 确保任务已经开始
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 多个线程同时尝试forget
        for (int i = 0; i < numThreads; i++) {
            forgetExecutor.submit(() -> {
                try {
                    startLatch.await();
                    sf.forget("forget-key");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 让所有线程同时开始
        doneLatch.await();

        // 验证future已经被异常完成
        assertThrows(CompletionException.class, future::join);

        // 关闭线程池
        taskExecutor.shutdown();
        forgetExecutor.shutdown();
    }
}