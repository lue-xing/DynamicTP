/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.starter.toolkit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ByteConvertUtil 单元测试
 */
class ByteConvertUtilTest {

    @Test
    void getPrintSize_returnsBytesWhenBelowKb() {
        assertThat(ByteConvertUtil.getPrintSize(100)).isEqualTo("100B");
        assertThat(ByteConvertUtil.getPrintSize(1023)).isEqualTo("1023B");
    }

    @Test
    void getPrintSize_returnsKbWhenBelowMb() {
        assertThat(ByteConvertUtil.getPrintSize(ByteConvertUtil.KB_SIZE)).endsWith("KB");
        assertThat(ByteConvertUtil.getPrintSize(2048)).isEqualTo("2.00KB");
    }

    @Test
    void getPrintSize_returnsMbWhenBelowGb() {
        assertThat(ByteConvertUtil.getPrintSize(ByteConvertUtil.MB_SIZE)).endsWith("MB");
        assertThat(ByteConvertUtil.getPrintSize(2L * ByteConvertUtil.MB_SIZE)).isEqualTo("2.00MB");
    }

    @Test
    void getPrintSize_returnsGbWhenAboveOrEqualGb() {
        assertThat(ByteConvertUtil.getPrintSize(ByteConvertUtil.GB_SIZE)).endsWith("GB");
    }
}
