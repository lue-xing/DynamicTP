/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.spring.base.configuration;

import com.luexing.dynamictp.core.alarm.ThreadPoolAlarmChecker;
import com.luexing.dynamictp.core.config.BootstrapConfigProperties;
import com.luexing.dynamictp.core.monitor.ThreadPoolMonitor;
import com.luexing.dynamictp.core.notification.service.NotifierDispatcher;
import com.luexing.dynamictp.spring.base.support.ApplicationContextHolder;
import com.luexing.dynamictp.spring.base.support.DynamicTPBeanPostProcessor;
import com.luexing.dynamictp.spring.base.support.SpringPropertiesLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 动态线程池基础 Spring 配置类
 * <p>
 */
@Configuration
public class DynamicTPBaseConfiguration {

    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    @DependsOn("applicationContextHolder")
    public DynamicTPBeanPostProcessor dynamicTPBeanPostProcessor(BootstrapConfigProperties properties) {
        return new DynamicTPBeanPostProcessor(properties);
    }

    @Bean
    public NotifierDispatcher notifierDispatcher() {
        return new NotifierDispatcher();
    }

    @Bean
    public SpringPropertiesLoader springPropertiesLoader() {
        return new SpringPropertiesLoader();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ThreadPoolAlarmChecker threadPoolAlarmChecker(NotifierDispatcher notifierDispatcher) {
        return new ThreadPoolAlarmChecker(notifierDispatcher);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ThreadPoolMonitor threadPoolMonitor() {
        return new ThreadPoolMonitor();
    }
}
