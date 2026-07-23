/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.notification.service;

import com.luexing.dynamictp.core.config.BootstrapConfigProperties;
import com.luexing.dynamictp.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * NotifierDispatcher 单元测试
 */
class NotifierDispatcherTest {

    private BootstrapConfigProperties originalInstance;
    private NotifierDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        originalInstance = BootstrapConfigProperties.getInstance();
        dispatcher = new NotifierDispatcher();
    }

    @AfterEach
    void restoreBootstrapConfig() {
        BootstrapConfigProperties.setInstance(originalInstance);
    }

    @Test
    void sendAlarmMessageIsNoOpWhenNotifyPlatformsIsNull() {
        BootstrapConfigProperties config = new BootstrapConfigProperties();
        config.setNotifyPlatforms(null);
        BootstrapConfigProperties.setInstance(config);

        ThreadPoolAlarmNotifyDTO alarm = buildAlarm(UUID.randomUUID().toString());

        assertThatCode(() -> dispatcher.sendAlarmMessage(alarm)).doesNotThrowAnyException();
    }

    @Test
    void sendAlarmMessageIsNoOpWhenPlatformIsUnknown() {
        BootstrapConfigProperties config = new BootstrapConfigProperties();
        BootstrapConfigProperties.NotifyPlatformsConfig notifyConfig = new BootstrapConfigProperties.NotifyPlatformsConfig();
        notifyConfig.setPlatform("XXX");
        config.setNotifyPlatforms(notifyConfig);
        BootstrapConfigProperties.setInstance(config);

        ThreadPoolAlarmNotifyDTO alarm = buildAlarm(UUID.randomUUID().toString());

        assertThatCode(() -> dispatcher.sendAlarmMessage(alarm)).doesNotThrowAnyException();
    }

    @Test
    void sendAlarmMessageInvokesMockOnceThenRateLimiterSuppressesSecondCall() throws Exception {
        NotifierService mockNotifier = mock(NotifierService.class);
        injectMockNotifier("TEST", mockNotifier);

        BootstrapConfigProperties config = new BootstrapConfigProperties();
        BootstrapConfigProperties.NotifyPlatformsConfig notifyConfig = new BootstrapConfigProperties.NotifyPlatformsConfig();
        notifyConfig.setPlatform("TEST");
        config.setNotifyPlatforms(notifyConfig);
        BootstrapConfigProperties.setInstance(config);

        String threadPoolId = UUID.randomUUID().toString();
        ThreadPoolAlarmNotifyDTO alarm = buildAlarm(threadPoolId);

        dispatcher.sendAlarmMessage(alarm);
        dispatcher.sendAlarmMessage(alarm);

        verify(mockNotifier, times(1)).sendAlarmMessage(any(ThreadPoolAlarmNotifyDTO.class));
    }

    private ThreadPoolAlarmNotifyDTO buildAlarm(String threadPoolId) {
        return ThreadPoolAlarmNotifyDTO.builder()
                .threadPoolId(threadPoolId)
                .alarmType("Capacity")
                .interval(5)
                .build();
    }

    @SuppressWarnings("unchecked")
    private void injectMockNotifier(String platformKey, NotifierService mockNotifier) throws Exception {
        Field field = NotifierDispatcher.class.getDeclaredField("NOTIFIER_SERVICE_MAP");
        field.setAccessible(true);
        Map<String, NotifierService> map = (Map<String, NotifierService>) field.get(null);
        map.put(platformKey, mockNotifier);
    }
}
