/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.alarm;

import com.luexing.dynamictp.core.executor.DynamicTPExecutor;
import com.luexing.dynamictp.core.executor.DynamicTPRegistry;
import com.luexing.dynamictp.core.executor.ThreadPoolExecutorHolder;
import com.luexing.dynamictp.core.executor.ThreadPoolExecutorProperties;
import com.luexing.dynamictp.core.executor.support.ResizableCapacityLinkedBlockingQueue;
import com.luexing.dynamictp.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.luexing.dynamictp.core.notification.service.NotifierDispatcher;
import com.luexing.dynamictp.core.toolkit.ThreadFactoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * ThreadPoolAlarmChecker 单元测试
 */
class ThreadPoolAlarmCheckerTest {

    private NotifierDispatcher notifierDispatcher;
    private ThreadPoolAlarmChecker checker;
    private DynamicTPExecutor executor;
    private String threadPoolId;
    private CountDownLatch workerBlock;

    @BeforeEach
    void setUp() throws Exception {
        clearRegistry();
        notifierDispatcher = mock(NotifierDispatcher.class);
        checker = new ThreadPoolAlarmChecker(notifierDispatcher);
        threadPoolId = UUID.randomUUID().toString();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (workerBlock != null) {
            workerBlock.countDown();
        }
        checker.stop();
        if (executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        clearRegistry();
    }

    @Test
    void checkQueueUsageTriggersAlarmWhenThresholdExceeded() throws Exception {
        executor = createExecutor(1, 1);
        fillQueueToCapacity();

        ThreadPoolExecutorProperties properties = createAlarmProperties(1, 80);
        registerHolder(properties);

        invokePrivateMethod("checkQueueUsage", ThreadPoolExecutorHolder.class,
                DynamicTPRegistry.getHolder(threadPoolId));

        verify(notifierDispatcher, atLeastOnce()).sendAlarmMessage(any(ThreadPoolAlarmNotifyDTO.class));

        ArgumentCaptor<ThreadPoolAlarmNotifyDTO> captor = ArgumentCaptor.forClass(ThreadPoolAlarmNotifyDTO.class);
        verify(notifierDispatcher, atLeastOnce()).sendAlarmMessage(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(ThreadPoolAlarmNotifyDTO::getAlarmType)
                .contains("Capacity");
    }

    @Test
    void checkRejectCountTriggersAlarmWhenRejectCountIncreases() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch block = new CountDownLatch(1);

        executor = createExecutor(1, 1);
        ThreadPoolExecutorProperties properties = createAlarmProperties(80, 80);
        registerHolder(properties);

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

        for (int i = 0; i < 5; i++) {
            try {
                executor.execute(() -> { });
            } catch (Exception ignored) {
                // expected rejections
            }
        }
        assertThat(executor.getRejectCount().get()).isGreaterThan(0L);

        invokePrivateMethod("checkRejectCount", ThreadPoolExecutorHolder.class,
                DynamicTPRegistry.getHolder(threadPoolId));

        verify(notifierDispatcher, atLeastOnce()).sendAlarmMessage(any(ThreadPoolAlarmNotifyDTO.class));

        ArgumentCaptor<ThreadPoolAlarmNotifyDTO> captor = ArgumentCaptor.forClass(ThreadPoolAlarmNotifyDTO.class);
        verify(notifierDispatcher, atLeastOnce()).sendAlarmMessage(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(ThreadPoolAlarmNotifyDTO::getAlarmType)
                .contains("Reject");

        block.countDown();
    }

    @Test
    void checkAlarmSkipsWhenAlarmDisabled() throws Exception {
        executor = createExecutor(1, 1);

        ThreadPoolExecutorProperties properties = createAlarmProperties(1, 80);
        properties.getAlarm().setEnable(false);
        registerHolder(properties);

        invokePrivateMethod("checkAlarm");

        verify(notifierDispatcher, never()).sendAlarmMessage(any());
    }

    private DynamicTPExecutor createExecutor(int core, int max) {
        return new DynamicTPExecutor(
                threadPoolId,
                core,
                max,
                60L,
                TimeUnit.SECONDS,
                new ResizableCapacityLinkedBlockingQueue<>(1),
                ThreadFactoryBuilder.builder().namePrefix("alarm-test-").build(),
                new ThreadPoolExecutor.AbortPolicy(),
                0L
        );
    }

    private ThreadPoolExecutorProperties createAlarmProperties(int queueThreshold, int activeThreshold) {
        ThreadPoolExecutorProperties properties = new ThreadPoolExecutorProperties();
        properties.setThreadPoolId(threadPoolId);
        properties.setNotify(new ThreadPoolExecutorProperties.NotifyConfig("test@example.com", 5));
        properties.setAlarm(new ThreadPoolExecutorProperties.AlarmConfig(true, queueThreshold, activeThreshold));
        return properties;
    }

    private void registerHolder(ThreadPoolExecutorProperties properties) {
        DynamicTPRegistry.putHolder(threadPoolId, executor, properties);
    }

    private void fillQueueToCapacity() throws InterruptedException {
        CountDownLatch workerStarted = new CountDownLatch(1);
        workerBlock = new CountDownLatch(1);
        executor.execute(() -> {
            workerStarted.countDown();
            try {
                workerBlock.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertThat(workerStarted.await(2, TimeUnit.SECONDS)).isTrue();
        executor.execute(() -> { });
        assertThat(executor.getQueue().size()).isEqualTo(1);
    }

    private void invokePrivateMethod(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = ThreadPoolAlarmChecker.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(checker);
    }

    private void invokePrivateMethod(String methodName, Class<?> paramType, Object arg) throws Exception {
        Method method = ThreadPoolAlarmChecker.class.getDeclaredMethod(methodName, paramType);
        method.setAccessible(true);
        method.invoke(checker, arg);
    }

    @SuppressWarnings("unchecked")
    private void clearRegistry() throws Exception {
        Field field = DynamicTPRegistry.class.getDeclaredField("HOLDER_MAP");
        field.setAccessible(true);
        ((Map<String, ThreadPoolExecutorHolder>) field.get(null)).clear();
    }
}
