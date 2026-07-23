/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.nacos.cloud.example.configuration;

import com.luexing.dynamictp.core.executor.support.BlockingQueueTypeEnum;
import com.luexing.dynamictp.core.toolkit.ThreadPoolExecutorBuilder;
import com.luexing.dynamictp.spring.base.DynamicThreadPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 动态线程池配置
 * <p>
 */
@Configuration
public class DynamicThreadPoolConfiguration {

    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor luexing1() {
        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("luexing-1")
                .corePoolSize(2)
                .maximumPoolSize(4)
                .keepAliveTime(9999L)
                .awaitTerminationMillis(5000L)
                .workQueueType(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE)
                .threadFactory("luexing-1_")
                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .dynamicPool()
                .build();
    }

    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor luexing2() {
        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("luexing-2")
                .corePoolSize(4)
                .maximumPoolSize(6)
                .keepAliveTime(9999L)
                .workQueueType(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE)
                .threadFactory("luexing-2_")
                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .dynamicPool()
                .build();
    }
}
