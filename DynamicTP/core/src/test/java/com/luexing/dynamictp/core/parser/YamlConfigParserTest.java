/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * YamlConfigParser 单元测试
 */
class YamlConfigParserTest {

    private YamlConfigParser parser;

    @BeforeEach
    void setUp() {
        parser = new YamlConfigParser();
    }

    @Test
    void nestedYamlFlattensWithDots() throws Exception {
        String yaml = """
                dynamictp:
                  enable: true
                  monitor:
                    collectType: micrometer
                """;

        Map<Object, Object> result = parser.doParse(yaml);

        assertThat(result).containsEntry("dynamictp.enable", "true");
        assertThat(result).containsEntry("dynamictp.monitor.collectType", "micrometer");
    }

    @Test
    void listItemsUseIndexedKeys() throws Exception {
        String yaml = """
                dynamictp:
                  executors:
                    - threadPoolId: pool-a
                      corePoolSize: 2
                    - threadPoolId: pool-b
                      corePoolSize: 4
                """;

        Map<Object, Object> result = parser.doParse(yaml);

        assertThat(result).containsEntry("dynamictp.executors[0].threadPoolId", "pool-a");
        assertThat(result).containsEntry("dynamictp.executors[0].corePoolSize", "2");
        assertThat(result).containsEntry("dynamictp.executors[1].threadPoolId", "pool-b");
        assertThat(result).containsEntry("dynamictp.executors[1].corePoolSize", "4");
    }

    @Test
    void emptyOrNullReturnsEmptyMap() throws Exception {
        assertThat(parser.doParse(null)).isEmpty();
        assertThat(parser.doParse("")).isEmpty();
        assertThat(parser.doParse("   ")).isEmpty();
    }

    @Test
    void supportsYamlAndYml() {
        assertThat(parser.supports(ConfigFileTypeEnum.YAML)).isTrue();
        assertThat(parser.supports(ConfigFileTypeEnum.YML)).isTrue();
        assertThat(parser.supports(ConfigFileTypeEnum.PROPERTIES)).isFalse();
    }
}
