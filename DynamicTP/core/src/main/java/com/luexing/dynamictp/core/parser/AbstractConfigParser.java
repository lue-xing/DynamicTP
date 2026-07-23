/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.parser;

/**
 * 配置解析器抽象类
 * <p>
 */
public abstract class AbstractConfigParser implements ConfigParser {

    @Override
    public boolean supports(ConfigFileTypeEnum type) {
        return getConfigFileTypes().contains(type);
    }
}
