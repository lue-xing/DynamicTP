/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.executor.support;

import com.luexing.dynamictp.core.executor.DynamicTPExecutor;
import com.luexing.dynamictp.core.toolkit.ThreadPoolExecutorBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 拒绝策略动态代理测试
 */
class RejectedExecutionHandlerProxyTest {

    private ThreadPoolExecutor executor;

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            executor = null;
        }
    }

    @Test
    void rejectedExecutionIncrementsRejectCount() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch block = new CountDownLatch(1);

        executor = ThreadPoolExecutorBuilder.builder()
                .threadPoolId("test-rejected-proxy")
                .corePoolSize(1)
                .maximumPoolSize(1)
                .keepAliveTime(10000L)
                .workQueueType(BlockingQueueTypeEnum.ARRAY_BLOCKING_QUEUE)
                .workQueueCapacity(1)
                .threadFactory("test-rejected-proxy_")
                .rejectedHandler(new ThreadPoolExecutor.AbortPolicy())
                .dynamicPool()
                .build();

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

        int rejected = 0;
        for (int i = 0; i < 10; i++) {
            try {
                executor.execute(() -> { });
            } catch (RejectedExecutionException ex) {
                rejected++;
            }
        }

        DynamicTPExecutor dynamicExecutor = (DynamicTPExecutor) executor;

        assertThat(rejected).isGreaterThan(0);
        assertThat(dynamicExecutor.getRejectCount().get()).isGreaterThan(0L);

        block.countDown();
    }
}
