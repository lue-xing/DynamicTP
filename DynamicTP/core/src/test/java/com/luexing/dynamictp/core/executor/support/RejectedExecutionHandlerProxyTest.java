/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.executor.support;

import cn.hutool.core.thread.ThreadUtil;
import com.luexing.dynamictp.core.executor.DynamicTPExecutor;
import com.luexing.dynamictp.core.toolkit.ThreadPoolExecutorBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 拒绝策略动态代理测试
 * <p>
 */
@Slf4j
public class RejectedExecutionHandlerProxyTest {

    @Test
    public void testRejectedExecutionHandlerProxy() {
        ThreadPoolExecutor executor = ThreadPoolExecutorBuilder.builder()
                .threadPoolId("test-rejected-proxy")
                .corePoolSize(1)
                .maximumPoolSize(1)
                .keepAliveTime(10000L)
                .workQueueType(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE)
                .threadFactory("test-rejected-proxy_")
                .rejectedHandler(new ThreadPoolExecutor.AbortPolicy())
                .dynamicPool()
                .build();

        for (int i = 0; i < 10; i++) {
            try {
                executor.execute(() -> ThreadUtil.sleep(Integer.MAX_VALUE));
            } catch (Exception ex) {
                log.error("ThreadPool name :: {}, Exception :: ", Thread.currentThread().getName(), ex.getMessage());
            }
        }

        ThreadUtil.sleep(1000);

        DynamicTPExecutor dynamicThreadPoolExecutor = (DynamicTPExecutor) executor;
        Long rejectCount = dynamicThreadPoolExecutor.getRejectCount().get();
        log.info("ThreadPool name :: {}, Reject count :: {}", Thread.currentThread().getName(), rejectCount);
    }
}
