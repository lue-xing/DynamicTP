/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.config.apollo.starter.configuration;

import com.luexing.dynamictp.config.apollo.starter.refresher.ApolloRefresherHandler;
import com.luexing.dynamictp.core.config.BootstrapConfigProperties;
import com.luexing.dynamictp.spring.base.enable.MarkerConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Apollo 配置中心自动装配
 * <p>
 */
@AutoConfigureOrder(Integer.MIN_VALUE)
@ConditionalOnBean(MarkerConfiguration.Marker.class)
@ConditionalOnProperty(prefix = BootstrapConfigProperties.PREFIX, value = "enable", matchIfMissing = true, havingValue = "true")
public class ApolloAutoConfiguration {

    @Bean
    public ApolloRefresherHandler apolloRefresherHandler(BootstrapConfigProperties properties) {
        return new ApolloRefresherHandler(properties);
    }
}
