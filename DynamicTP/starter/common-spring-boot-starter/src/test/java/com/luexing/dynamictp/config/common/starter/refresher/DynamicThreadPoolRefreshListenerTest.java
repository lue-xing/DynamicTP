/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.config.common.starter.refresher;

import com.luexing.dynamictp.core.config.BootstrapConfigProperties;
import com.luexing.dynamictp.core.executor.DynamicTPRegistry;
import com.luexing.dynamictp.core.executor.ThreadPoolExecutorHolder;
import com.luexing.dynamictp.core.executor.ThreadPoolExecutorProperties;
import com.luexing.dynamictp.core.executor.support.BlockingQueueTypeEnum;
import com.luexing.dynamictp.core.executor.support.ResizableCapacityLinkedBlockingQueue;
import com.luexing.dynamictp.core.notification.dto.ThreadPoolConfigChangeDTO;
import com.luexing.dynamictp.core.notification.service.NotifierDispatcher;
import com.luexing.dynamictp.core.toolkit.ThreadPoolExecutorBuilder;
import com.luexing.dynamictp.spring.base.support.ApplicationContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DynamicThreadPoolRefreshListener 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DynamicThreadPoolRefreshListenerTest {

    @Mock
    private NotifierDispatcher notifierDispatcher;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private Environment environment;

    private DynamicThreadPoolRefreshListener listener;
    private ThreadPoolExecutor executor;
    private String threadPoolId;

    @BeforeEach
    void setUp() {
        listener = new DynamicThreadPoolRefreshListener(notifierDispatcher);
        threadPoolId = "refresh-" + UUID.randomUUID();
        executor = ThreadPoolExecutorBuilder.builder()
                .threadPoolId(threadPoolId)
                .corePoolSize(1)
                .maximumPoolSize(2)
                .keepAliveTime(30L)
                .workQueueType(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE)
                .workQueueCapacity(10)
                .threadFactory(threadPoolId + "_")
                .rejectedHandler(new ThreadPoolExecutor.AbortPolicy())
                .dynamicPool()
                .build();

        ThreadPoolExecutorProperties properties = ThreadPoolExecutorProperties.builder()
                .threadPoolId(threadPoolId)
                .corePoolSize(1)
                .maximumPoolSize(2)
                .queueCapacity(10)
                .keepAliveTime(30L)
                .workQueue(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE.getName())
                .rejectedHandler("AbortPolicy")
                .allowCoreThreadTimeOut(false)
                .notify(new ThreadPoolExecutorProperties.NotifyConfig("dev", 5))
                .build();
        DynamicTPRegistry.putHolder(threadPoolId, executor, properties);

        new ApplicationContextHolder().setApplicationContext(applicationContext);
        org.mockito.Mockito.lenient().when(applicationContext.getBean(Environment.class)).thenReturn(environment);
        org.mockito.Mockito.lenient().when(environment.getProperty("spring.profiles.active", "dev")).thenReturn("test");
        org.mockito.Mockito.lenient().when(environment.getProperty("spring.application.name")).thenReturn("dynamictp-ut");
    }

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void onApplicationEvent_skipsWhenExecutorsEmpty() {
        BootstrapConfigProperties remote = new BootstrapConfigProperties();
        remote.setExecutors(Collections.emptyList());

        listener.onApplicationEvent(new ThreadPoolConfigUpdateEvent(this, remote));

        verify(notifierDispatcher, never()).sendChangeMessage(any());
        assertThat(executor.getCorePoolSize()).isEqualTo(1);
    }

    @Test
    void onApplicationEvent_skipsWhenConfigUnchanged() {
        ThreadPoolExecutorProperties remoteProps = ThreadPoolExecutorProperties.builder()
                .threadPoolId(threadPoolId)
                .corePoolSize(1)
                .maximumPoolSize(2)
                .queueCapacity(10)
                .keepAliveTime(30L)
                .rejectedHandler("AbortPolicy")
                .allowCoreThreadTimeOut(false)
                .notify(new ThreadPoolExecutorProperties.NotifyConfig("dev", 5))
                .build();
        BootstrapConfigProperties remote = new BootstrapConfigProperties();
        remote.setExecutors(List.of(remoteProps));

        listener.onApplicationEvent(new ThreadPoolConfigUpdateEvent(this, remote));

        verify(notifierDispatcher, never()).sendChangeMessage(any());
    }

    @Test
    void onApplicationEvent_updatesCoreMaxQueueAndNotifies() {
        ThreadPoolExecutorProperties remoteProps = ThreadPoolExecutorProperties.builder()
                .threadPoolId(threadPoolId)
                .corePoolSize(2)
                .maximumPoolSize(4)
                .queueCapacity(20)
                .keepAliveTime(60L)
                .rejectedHandler("CallerRunsPolicy")
                .allowCoreThreadTimeOut(true)
                .workQueue(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE.getName())
                .notify(new ThreadPoolExecutorProperties.NotifyConfig("alice", 5))
                .build();
        BootstrapConfigProperties remote = new BootstrapConfigProperties();
        remote.setExecutors(List.of(remoteProps));

        listener.onApplicationEvent(new ThreadPoolConfigUpdateEvent(this, remote));

        assertThat(executor.getCorePoolSize()).isEqualTo(2);
        assertThat(executor.getMaximumPoolSize()).isEqualTo(4);
        assertThat(executor.getKeepAliveTime(TimeUnit.SECONDS)).isEqualTo(60L);
        assertThat(executor.allowsCoreThreadTimeOut()).isTrue();
        assertThat(executor.getQueue()).isInstanceOf(ResizableCapacityLinkedBlockingQueue.class);
        assertThat(executor.getQueue().remainingCapacity() + executor.getQueue().size()).isEqualTo(20);

        ArgumentCaptor<ThreadPoolConfigChangeDTO> captor = ArgumentCaptor.forClass(ThreadPoolConfigChangeDTO.class);
        verify(notifierDispatcher).sendChangeMessage(captor.capture());
        assertThat(captor.getValue().getThreadPoolId()).isEqualTo(threadPoolId);
        assertThat(captor.getValue().getApplicationName()).isEqualTo("dynamictp-ut");

        ThreadPoolExecutorHolder holder = DynamicTPRegistry.getHolder(threadPoolId);
        assertThat(holder.getExecutorProperties().getCorePoolSize()).isEqualTo(2);
    }

    @Test
    void onApplicationEvent_raisesCoreAboveOriginalMax_setsMaxFirst() {
        ThreadPoolExecutorProperties remoteProps = ThreadPoolExecutorProperties.builder()
                .threadPoolId(threadPoolId)
                .corePoolSize(5)
                .maximumPoolSize(5)
                .queueCapacity(10)
                .keepAliveTime(30L)
                .rejectedHandler("AbortPolicy")
                .notify(new ThreadPoolExecutorProperties.NotifyConfig("dev", 5))
                .build();
        BootstrapConfigProperties remote = new BootstrapConfigProperties();
        remote.setExecutors(List.of(remoteProps));

        listener.onApplicationEvent(new ThreadPoolConfigUpdateEvent(this, remote));

        assertThat(executor.getCorePoolSize()).isEqualTo(5);
        assertThat(executor.getMaximumPoolSize()).isEqualTo(5);
        verify(notifierDispatcher).sendChangeMessage(any());
    }

    @Test
    void onApplicationEvent_unknownPoolId_doesNotNotify() {
        ThreadPoolExecutorProperties remoteProps = ThreadPoolExecutorProperties.builder()
                .threadPoolId("missing-" + UUID.randomUUID())
                .corePoolSize(3)
                .maximumPoolSize(3)
                .notify(new ThreadPoolExecutorProperties.NotifyConfig("dev", 5))
                .build();
        BootstrapConfigProperties remote = new BootstrapConfigProperties();
        remote.setExecutors(List.of(remoteProps));

        listener.onApplicationEvent(new ThreadPoolConfigUpdateEvent(this, remote));

        verify(notifierDispatcher, never()).sendChangeMessage(any());
        assertThat(executor.getCorePoolSize()).isEqualTo(1);
    }
}
