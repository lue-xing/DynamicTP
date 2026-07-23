/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.parser;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ConfigParserHandler 单元测试
 */
class ConfigParserHandlerTest {

    @Test
    void routesYamlAndProperties() throws Exception {
        ConfigParserHandler handler = ConfigParserHandler.getInstance();

        Map<Object, Object> yamlResult = handler.parseConfig(
                "dynamictp:\n  enable: true\n",
                ConfigFileTypeEnum.YAML
        );
        Map<Object, Object> ymlResult = handler.parseConfig(
                "dynamictp:\n  enable: false\n",
                ConfigFileTypeEnum.YML
        );
        Map<Object, Object> propsResult = handler.parseConfig(
                "dynamictp.enable=true\n",
                ConfigFileTypeEnum.PROPERTIES
        );

        assertThat(yamlResult).containsEntry("dynamictp.enable", "true");
        assertThat(ymlResult).containsEntry("dynamictp.enable", "false");
        assertThat(propsResult).containsEntry("dynamictp.enable", "true");
    }

    @Test
    void getInstanceReturnsSingleton() {
        ConfigParserHandler first = ConfigParserHandler.getInstance();
        ConfigParserHandler second = ConfigParserHandler.getInstance();

        assertThat(first).isSameAs(second);
    }
}
