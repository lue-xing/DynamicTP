/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.executor.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RejectedPolicyTypeEnum 单元测试
 */
class RejectedPolicyTypeEnumTest {

    @ParameterizedTest
    @EnumSource(RejectedPolicyTypeEnum.class)
    void createPolicyForEachKnownName(RejectedPolicyTypeEnum type) {
        RejectedExecutionHandler handler = RejectedPolicyTypeEnum.createPolicy(type.getName());

        assertThat(handler).isNotNull();
        switch (type) {
            case CALLER_RUNS_POLICY -> assertThat(handler).isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);
            case ABORT_POLICY -> assertThat(handler).isInstanceOf(ThreadPoolExecutor.AbortPolicy.class);
            case DISCARD_POLICY -> assertThat(handler).isInstanceOf(ThreadPoolExecutor.DiscardPolicy.class);
            case DISCARD_OLDEST_POLICY -> assertThat(handler).isInstanceOf(ThreadPoolExecutor.DiscardOldestPolicy.class);
        }
    }

    @Test
    void invalidNameThrowsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> RejectedPolicyTypeEnum.createPolicy("UnknownPolicy"));
    }
}
