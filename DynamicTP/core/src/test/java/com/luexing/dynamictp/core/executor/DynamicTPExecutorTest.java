/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.executor;

import com.luexing.dynamictp.core.executor.support.ResizableCapacityLinkedBlockingQueue;
import com.luexing.dynamictp.core.toolkit.ThreadFactoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * DynamicTPExecutor 单元测试
 */
class DynamicTPExecutorTest {

    private final List<DynamicTPExecutor> executors = new ArrayList<>();

    @AfterEach
    void tearDown() {
        executors.forEach(executor -> {
            executor.shutdownNow();
            try {
                executor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        executors.clear();
    }

    @Test
    void rejectCountIncrementsWhenTasksRejected() throws Exception {
        String threadPoolId = UUID.randomUUID().toString();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch block = new CountDownLatch(1);

        DynamicTPExecutor executor = new DynamicTPExecutor(
                threadPoolId,
                1,
                1,
                60L,
                TimeUnit.SECONDS,
                new ResizableCapacityLinkedBlockingQueue<>(1),
                ThreadFactoryBuilder.builder().namePrefix("reject-test-").build(),
                new ThreadPoolExecutor.AbortPolicy(),
                0L
        );
        executors.add(executor);

        executor.execute(() -> {
            started.countDown();
            try {
                block.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertThat(started.await(2, TimeUnit.SECONDS)).isTrue();

        executor.execute(() -> { });
        assertThat(executor.getQueue().size()).isEqualTo(1);

        int rejected = 0;
        for (int i = 0; i < 10; i++) {
            try {
                executor.execute(() -> { });
            } catch (RejectedExecutionException ex) {
                rejected++;
            }
        }

        assertThat(rejected).isGreaterThan(0);
        assertThat(executor.getRejectCount().get()).isGreaterThan(0L);

        block.countDown();
    }

    @Test
    void getThreadPoolIdReturnsConfiguredId() {
        String threadPoolId = UUID.randomUUID().toString();
        DynamicTPExecutor executor = createExecutor(threadPoolId);

        assertThat(executor.getThreadPoolId()).isEqualTo(threadPoolId);
    }

    @Test
    void setRejectedExecutionHandlerWrapsAndToStringIsSimpleName() {
        DynamicTPExecutor executor = createExecutor(UUID.randomUUID().toString());
        ThreadPoolExecutor.DiscardPolicy discardPolicy = new ThreadPoolExecutor.DiscardPolicy();

        executor.setRejectedExecutionHandler(discardPolicy);

        assertThat(executor.getRejectedExecutionHandler().toString()).isEqualTo("DiscardPolicy");
    }

    @Test
    void shutdownWithZeroAwaitTerminationDoesNotHang() {
        DynamicTPExecutor executor = createExecutor(UUID.randomUUID().toString());

        long start = System.currentTimeMillis();
        executor.shutdown();
        long elapsed = System.currentTimeMillis() - start;

        assertThat(executor.isShutdown()).isTrue();
        assertThat(elapsed).isLessThan(2000L);
    }

    private DynamicTPExecutor createExecutor(String threadPoolId) {
        DynamicTPExecutor executor = new DynamicTPExecutor(
                threadPoolId,
                1,
                1,
                60L,
                TimeUnit.SECONDS,
                new ResizableCapacityLinkedBlockingQueue<>(10),
                ThreadFactoryBuilder.builder().namePrefix("dynamic-test-").build(),
                new ThreadPoolExecutor.AbortPolicy(),
                0L
        );
        executors.add(executor);
        return executor;
    }
}
