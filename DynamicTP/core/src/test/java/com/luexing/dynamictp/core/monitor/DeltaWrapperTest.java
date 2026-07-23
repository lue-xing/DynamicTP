/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.monitor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DeltaWrapper 单元测试
 */
class DeltaWrapperTest {

    @Test
    void getDeltaIsZeroBeforeUpdate() {
        DeltaWrapper wrapper = new DeltaWrapper();

        assertThat(wrapper.getDelta()).isZero();
    }

    @Test
    void firstUpdateThenGetDeltaIsZero() {
        DeltaWrapper wrapper = new DeltaWrapper();

        wrapper.update(100L);

        assertThat(wrapper.getDelta()).isZero();
    }

    @Test
    void secondUpdateGetDeltaIsDifference() {
        DeltaWrapper wrapper = new DeltaWrapper();

        wrapper.update(100L);
        wrapper.update(150L);

        assertThat(wrapper.getDelta()).isEqualTo(50L);

        wrapper.update(120L);

        assertThat(wrapper.getDelta()).isEqualTo(-30L);
    }
}
