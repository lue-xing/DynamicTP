/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.parser;

import lombok.Getter;

/**
 * 配置解析器接口抽象
 * <p>
 */
@Getter
public enum ConfigFileTypeEnum {

    /**
     * PROPERTIES
     */
    PROPERTIES("properties"),

    /**
     * YML
     */
    YML("yml"),

    /**
     * YAML
     */
    YAML("yaml");

    private final String value;

    ConfigFileTypeEnum(String value) {
        this.value = value;
    }

    public static ConfigFileTypeEnum of(String value) {
        for (ConfigFileTypeEnum typeEnum : ConfigFileTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return PROPERTIES;
    }
}
