/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.config.common.starter.refresher;

import com.luexing.dynamictp.core.config.BootstrapConfigProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 配置中心刷新线程池参数变更事件
 * <p>
 */
public class ThreadPoolConfigUpdateEvent extends ApplicationEvent {

    @Getter
    @Setter
    private BootstrapConfigProperties bootstrapConfigProperties;

    public ThreadPoolConfigUpdateEvent(Object source, BootstrapConfigProperties bootstrapConfigProperties) {
        super(source);
        this.bootstrapConfigProperties = bootstrapConfigProperties;
    }
}
