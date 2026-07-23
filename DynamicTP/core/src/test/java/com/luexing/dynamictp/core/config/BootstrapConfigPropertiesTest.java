/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BootstrapConfigProperties 单元测试
 */
class BootstrapConfigPropertiesTest {

    private BootstrapConfigProperties originalInstance;

    @AfterEach
    void restoreInstance() {
        if (originalInstance != null) {
            BootstrapConfigProperties.setInstance(originalInstance);
        }
    }

    @Test
    void monitorDefaults() {
        BootstrapConfigProperties properties = new BootstrapConfigProperties();

        assertThat(properties.getMonitor().getEnable()).isTrue();
        assertThat(properties.getMonitor().getCollectType()).isEqualTo("micrometer");
        assertThat(properties.getMonitor().getCollectInterval()).isEqualTo(10L);
    }

    @Test
    void setInstanceAndGetInstance() {
        originalInstance = BootstrapConfigProperties.getInstance();

        BootstrapConfigProperties custom = new BootstrapConfigProperties();
        custom.setEnable(false);
        custom.getMonitor().setCollectInterval(30L);

        BootstrapConfigProperties.setInstance(custom);

        assertThat(BootstrapConfigProperties.getInstance()).isSameAs(custom);
        assertThat(BootstrapConfigProperties.getInstance().getEnable()).isFalse();
        assertThat(BootstrapConfigProperties.getInstance().getMonitor().getCollectInterval()).isEqualTo(30L);
    }
}
