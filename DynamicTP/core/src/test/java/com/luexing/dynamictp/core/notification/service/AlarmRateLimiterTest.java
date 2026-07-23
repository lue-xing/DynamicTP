/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.notification.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AlarmRateLimiter 单元测试
 */
class AlarmRateLimiterTest {

    @Test
    void firstAllowAlarmReturnsTrue() {
        String threadPoolId = UUID.randomUUID().toString();

        assertThat(AlarmRateLimiter.allowAlarm(threadPoolId, "Capacity", 5)).isTrue();
    }

    @Test
    void secondCallWithinIntervalReturnsFalse() throws InterruptedException {
        String threadPoolId = UUID.randomUUID().toString();
        String alarmType = "Reject";

        assertThat(AlarmRateLimiter.allowAlarm(threadPoolId, alarmType, 5)).isTrue();
        Thread.sleep(1);
        assertThat(AlarmRateLimiter.allowAlarm(threadPoolId, alarmType, 5)).isFalse();
    }

    @Test
    void differentAlarmTypesAreIndependent() throws InterruptedException {
        String threadPoolId = UUID.randomUUID().toString();

        assertThat(AlarmRateLimiter.allowAlarm(threadPoolId, "Capacity", 5)).isTrue();
        assertThat(AlarmRateLimiter.allowAlarm(threadPoolId, "Activity", 5)).isTrue();
        assertThat(AlarmRateLimiter.allowAlarm(threadPoolId, "Reject", 5)).isTrue();

        Thread.sleep(1);
        assertThat(AlarmRateLimiter.allowAlarm(threadPoolId, "Capacity", 5)).isFalse();
        assertThat(AlarmRateLimiter.allowAlarm(threadPoolId, "Activity", 5)).isFalse();
        assertThat(AlarmRateLimiter.allowAlarm(threadPoolId, "Reject", 5)).isFalse();
    }
}
