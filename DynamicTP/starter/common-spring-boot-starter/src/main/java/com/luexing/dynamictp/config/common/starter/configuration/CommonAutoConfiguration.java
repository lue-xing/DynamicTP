/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.config.common.starter.configuration;

import com.luexing.dynamictp.config.common.starter.refresher.DynamicThreadPoolRefreshListener;
import com.luexing.dynamictp.core.config.BootstrapConfigProperties;
import com.luexing.dynamictp.core.notification.service.NotifierDispatcher;
import com.luexing.dynamictp.spring.base.configuration.DynamicTPBaseConfiguration;
import com.luexing.dynamictp.spring.base.enable.MarkerConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * 基于配置中心的公共自动装配配置
 * <p>
 */
@ConditionalOnBean(MarkerConfiguration.Marker.class)
@Import(DynamicTPBaseConfiguration.class)
@AutoConfigureAfter(DynamicTPBaseConfiguration.class)
@ConditionalOnProperty(prefix = BootstrapConfigProperties.PREFIX, value = "enable", matchIfMissing = true, havingValue = "true")
public class CommonAutoConfiguration {

    @Bean
    public BootstrapConfigProperties bootstrapConfigProperties(Environment environment) {
        Binder binder = Binder.get(environment);
        Bindable<BootstrapConfigProperties> bindable = Bindable.of(BootstrapConfigProperties.class);
        BootstrapConfigProperties properties = binder.bind(BootstrapConfigProperties.PREFIX, bindable)
                .orElseThrow(() -> new IllegalStateException(
                        "未找到 dynamicTP 配置：请在 Nacos 使用 data-id=dynamictp-nacos-cloud-example.yaml 并配置 dynamictp 根节点，"
                                + "或使用本地 application-dev.yaml / classpath:nacos-config.yaml"));
        BootstrapConfigProperties.setInstance(properties);
        return properties;
    }

    @Bean
    public DynamicThreadPoolRefreshListener dynamicThreadPoolRefreshListener(NotifierDispatcher notifierDispatcher) {
        return new DynamicThreadPoolRefreshListener(notifierDispatcher);
    }

    @Bean
    public DynamicTPBannerHandler dynamicTPBannerHandler(ObjectProvider<BuildProperties> buildProperties) {
        return new DynamicTPBannerHandler(buildProperties.getIfAvailable());
    }
}
