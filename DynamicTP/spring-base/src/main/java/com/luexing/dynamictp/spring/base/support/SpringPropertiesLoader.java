/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.spring.base.support;

import com.luexing.dynamictp.core.config.ApplicationProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

/**
 * 动态线程池 Spring 配置加载
 * <p>
 */
public class SpringPropertiesLoader implements InitializingBean {

    @Value("${spring.application.name:UNKNOWN}")
    private String applicationName;

    @Value("${spring.profiles.active:UNKNOWN}")
    private String activeProfile;

    @Override
    public void afterPropertiesSet() throws Exception {
        ApplicationProperties.setApplicationName(applicationName);
        ApplicationProperties.setActiveProfile(activeProfile);
    }
}
