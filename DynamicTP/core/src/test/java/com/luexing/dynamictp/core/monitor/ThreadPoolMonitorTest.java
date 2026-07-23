/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.monitor;

import com.luexing.dynamictp.core.config.BootstrapConfigProperties;
import com.luexing.dynamictp.core.executor.DynamicTPRegistry;
import com.luexing.dynamictp.core.executor.ThreadPoolExecutorProperties;
import com.luexing.dynamictp.core.executor.support.BlockingQueueTypeEnum;
import com.luexing.dynamictp.core.toolkit.ThreadPoolExecutorBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ThreadPoolMonitor 单元测试
 */
class ThreadPoolMonitorTest {

    private ThreadPoolExecutor executor;
    private String threadPoolId;
    private BootstrapConfigProperties original;

    @BeforeEach
    void setUp() {
        original = BootstrapConfigProperties.getInstance();
        threadPoolId = "monitor-" + UUID.randomUUID();
        executor = ThreadPoolExecutorBuilder.builder()
                .threadPoolId(threadPoolId)
                .corePoolSize(1)
                .maximumPoolSize(2)
                .keepAliveTime(30L)
                .workQueueType(BlockingQueueTypeEnum.LINKED_BLOCKING_QUEUE)
                .workQueueCapacity(10)
                .threadFactory(threadPoolId + "_")
                .rejectedHandler(new ThreadPoolExecutor.AbortPolicy())
                .dynamicPool()
                .build();
        DynamicTPRegistry.putHolder(threadPoolId, executor, ThreadPoolExecutorProperties.builder()
                .threadPoolId(threadPoolId)
                .corePoolSize(1)
                .maximumPoolSize(2)
                .build());
    }

    @AfterEach
    void tearDown() {
        BootstrapConfigProperties.setInstance(original);
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void start_doesNothingWhenMonitorDisabled() {
        BootstrapConfigProperties props = new BootstrapConfigProperties();
        props.getMonitor().setEnable(false);
        BootstrapConfigProperties.setInstance(props);

        ThreadPoolMonitor monitor = new ThreadPoolMonitor();
        monitor.start();
        monitor.stop();
    }

    @Test
    void buildThreadPoolRuntimeInfo_mapsExecutorState() throws Exception {
        ThreadPoolMonitor monitor = new ThreadPoolMonitor();
        Method method = ThreadPoolMonitor.class.getDeclaredMethod(
                "buildThreadPoolRuntimeInfo",
                com.luexing.dynamictp.core.executor.ThreadPoolExecutorHolder.class);
        method.setAccessible(true);

        ThreadPoolRuntimeInfo info = (ThreadPoolRuntimeInfo) method.invoke(
                monitor, DynamicTPRegistry.getHolder(threadPoolId));

        assertThat(info.getThreadPoolId()).isEqualTo(threadPoolId);
        assertThat(info.getCorePoolSize()).isEqualTo(1);
        assertThat(info.getMaximumPoolSize()).isEqualTo(2);
        assertThat(info.getWorkQueueCapacity()).isEqualTo(10);
        assertThat(info.getRejectCount()).isEqualTo(0L);
    }

    @Test
    void startAndStop_withLogCollectType_runsWithoutError() throws InterruptedException {
        BootstrapConfigProperties props = new BootstrapConfigProperties();
        props.getMonitor().setEnable(true);
        props.getMonitor().setCollectType("log");
        props.getMonitor().setCollectInterval(1L);
        BootstrapConfigProperties.setInstance(props);

        ThreadPoolMonitor monitor = new ThreadPoolMonitor();
        monitor.start();
        Thread.sleep(100);
        monitor.stop();
    }
}
