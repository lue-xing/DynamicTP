/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApplicationProperties 单元测试
 */
class ApplicationPropertiesTest {

    @AfterEach
    void tearDown() {
        ApplicationProperties.setApplicationName(null);
        ApplicationProperties.setActiveProfile(null);
    }

    @Test
    void gettersAndSetters() {
        ApplicationProperties.setApplicationName("demo-app");
        ApplicationProperties.setActiveProfile("dev");

        assertThat(ApplicationProperties.getApplicationName()).isEqualTo("demo-app");
        assertThat(ApplicationProperties.getActiveProfile()).isEqualTo("dev");
    }
}
