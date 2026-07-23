/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.executor.support;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RejectedProxyInvocationHandler 单元测试
 */
class RejectedProxyInvocationHandlerTest {

    @Test
    void proxyIncrementsRejectCountOnRejectedExecution() {
        AtomicLong rejectCount = new AtomicLong();
        RejectedExecutionHandler target = new ThreadPoolExecutor.DiscardPolicy();
        RejectedExecutionHandler proxy = (RejectedExecutionHandler) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                new Class[]{RejectedExecutionHandler.class},
                new RejectedProxyInvocationHandler(target, rejectCount)
        );

        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 60L, java.util.concurrent.TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(1));

        try {
            assertThat(rejectCount.get()).isZero();

            proxy.rejectedExecution(() -> { }, executor);

            assertThat(rejectCount.get()).isEqualTo(1L);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void toStringReturnsTargetSimpleName() {
        AtomicLong rejectCount = new AtomicLong();
        RejectedExecutionHandler target = new ThreadPoolExecutor.DiscardPolicy();
        RejectedExecutionHandler proxy = (RejectedExecutionHandler) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                new Class[]{RejectedExecutionHandler.class},
                new RejectedProxyInvocationHandler(target, rejectCount)
        );

        assertThat(proxy.toString()).isEqualTo("DiscardPolicy");
    }
}
