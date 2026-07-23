/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.notification.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ThreadPoolAlarmNotifyDTO 单元测试
 */
class ThreadPoolAlarmNotifyDTOTest {

    @Test
    void resolveWithSupplierReturnsSupplierResult() {
        ThreadPoolAlarmNotifyDTO supplied = ThreadPoolAlarmNotifyDTO.builder()
                .threadPoolId("pool-supplied")
                .alarmType("Capacity")
                .build();
        ThreadPoolAlarmNotifyDTO dto = ThreadPoolAlarmNotifyDTO.builder()
                .threadPoolId("pool-original")
                .supplier(() -> supplied)
                .build();

        ThreadPoolAlarmNotifyDTO resolved = dto.resolve();

        assertThat(resolved).isSameAs(supplied);
        assertThat(resolved.getThreadPoolId()).isEqualTo("pool-supplied");
    }

    @Test
    void resolveWithoutSupplierReturnsThis() {
        ThreadPoolAlarmNotifyDTO dto = ThreadPoolAlarmNotifyDTO.builder()
                .threadPoolId("pool-self")
                .alarmType("Reject")
                .build();

        assertThat(dto.resolve()).isSameAs(dto);
    }
}
