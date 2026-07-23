/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.executor;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DynamicTPRegistry 单元测试
 */
class DynamicTPRegistryTest {

    @Test
    void putHolderGetHolderAndGetAllHolders() {
        String threadPoolId = UUID.randomUUID().toString();
        ThreadPoolExecutorProperties properties = ThreadPoolExecutorProperties.builder()
                .threadPoolId(threadPoolId)
                .corePoolSize(1)
                .maximumPoolSize(2)
                .build();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 2, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10)
        );

        try {
            DynamicTPRegistry.putHolder(threadPoolId, executor, properties);

            ThreadPoolExecutorHolder holder = DynamicTPRegistry.getHolder(threadPoolId);

            assertThat(holder).isNotNull();
            assertThat(holder.getThreadPoolId()).isEqualTo(threadPoolId);
            assertThat(holder.getExecutor()).isSameAs(executor);
            assertThat(holder.getExecutorProperties()).isSameAs(properties);
            assertThat(DynamicTPRegistry.getAllHolders()).contains(holder);
        } finally {
            executor.shutdownNow();
        }
    }
}
