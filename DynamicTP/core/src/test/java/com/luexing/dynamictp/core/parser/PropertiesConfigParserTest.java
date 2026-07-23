/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PropertiesConfigParser 单元测试
 */
class PropertiesConfigParserTest {

    private PropertiesConfigParser parser;

    @BeforeEach
    void setUp() {
        parser = new PropertiesConfigParser();
    }

    @Test
    void parsesKeyValuePairs() throws Exception {
        String content = """
                dynamictp.enable=true
                dynamictp.monitor.collectInterval=30
                """;

        Map<Object, Object> result = parser.doParse(content);

        assertThat(result).containsEntry("dynamictp.enable", "true");
        assertThat(result).containsEntry("dynamictp.monitor.collectInterval", "30");
    }

    @Test
    void supportsPropertiesType() {
        assertThat(parser.supports(ConfigFileTypeEnum.PROPERTIES)).isTrue();
        assertThat(parser.supports(ConfigFileTypeEnum.YAML)).isFalse();
        assertThat(parser.getConfigFileTypes()).containsExactly(ConfigFileTypeEnum.PROPERTIES);
    }
}
