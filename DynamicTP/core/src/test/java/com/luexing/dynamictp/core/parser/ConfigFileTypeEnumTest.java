/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ConfigFileTypeEnum 单元测试
 */
class ConfigFileTypeEnumTest {

    @Test
    void ofKnownValues() {
        assertThat(ConfigFileTypeEnum.of("properties")).isEqualTo(ConfigFileTypeEnum.PROPERTIES);
        assertThat(ConfigFileTypeEnum.of("yml")).isEqualTo(ConfigFileTypeEnum.YML);
        assertThat(ConfigFileTypeEnum.of("yaml")).isEqualTo(ConfigFileTypeEnum.YAML);
    }

    @Test
    void unknownValueFallsBackToProperties() {
        assertThat(ConfigFileTypeEnum.of("unknown")).isEqualTo(ConfigFileTypeEnum.PROPERTIES);
        assertThat(ConfigFileTypeEnum.of("json")).isEqualTo(ConfigFileTypeEnum.PROPERTIES);
    }
}
