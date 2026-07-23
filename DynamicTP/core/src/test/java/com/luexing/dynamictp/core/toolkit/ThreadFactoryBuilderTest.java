/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.toolkit;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ThreadFactoryBuilder 单元测试
 */
class ThreadFactoryBuilderTest {

    @Test
    void namePrefixWithIncrement() {
        ThreadFactory factory = ThreadFactoryBuilder.builder()
                .namePrefix("test-pool-")
                .build();

        Thread first = factory.newThread(() -> { });
        Thread second = factory.newThread(() -> { });

        assertThat(first.getName()).isEqualTo("test-pool-0");
        assertThat(second.getName()).isEqualTo("test-pool-1");
    }

    @Test
    void daemonAndPriorityAreApplied() {
        ThreadFactory factory = ThreadFactoryBuilder.builder()
                .namePrefix("daemon-pool-")
                .daemon(true)
                .priority(Thread.MAX_PRIORITY)
                .build();

        Thread thread = factory.newThread(() -> { });

        assertThat(thread.isDaemon()).isTrue();
        assertThat(thread.getPriority()).isEqualTo(Thread.MAX_PRIORITY);
    }

    @Test
    void emptyNamePrefixThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                ThreadFactoryBuilder.builder()
                        .namePrefix("")
                        .build()
                        .newThread(() -> { })
        );
    }

    @Test
    void invalidPriorityThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                ThreadFactoryBuilder.builder()
                        .namePrefix("pool-")
                        .priority(0)
                        .build()
        );
        assertThrows(IllegalArgumentException.class, () ->
                ThreadFactoryBuilder.builder()
                        .namePrefix("pool-")
                        .priority(11)
                        .build()
        );
    }
}
