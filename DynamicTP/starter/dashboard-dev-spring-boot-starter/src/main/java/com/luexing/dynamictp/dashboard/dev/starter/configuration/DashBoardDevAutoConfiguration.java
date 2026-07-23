/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.starter.configuration;

import com.luexing.dynamictp.dashboard.dev.starter.controller.DynamicThreadPoolController;
import com.luexing.dynamictp.dashboard.dev.starter.controller.WebThreadPoolController;
import com.luexing.dynamictp.dashboard.dev.starter.service.DynamicThreadPoolService;
import com.luexing.dynamictp.dashboard.dev.starter.service.WebThreadPoolService;
import org.springframework.context.annotation.Bean;

/**
 * 基于配置中心的公共自动装配配置
 * <p>
 */
public class DashBoardDevAutoConfiguration {

    @Bean
    public DynamicThreadPoolService dynamicThreadPoolService() {
        return new DynamicThreadPoolService();
    }

    @Bean
    public DynamicThreadPoolController dynamicThreadPoolController(DynamicThreadPoolService dynamicThreadPoolService) {
        return new DynamicThreadPoolController(dynamicThreadPoolService);
    }

    @Bean
    public WebThreadPoolService webThreadPoolService(com.luexing.dynamictp.web.starter.core.executor.WebThreadPoolService webThreadPoolService) {
        return new WebThreadPoolService(webThreadPoolService);
    }

    @Bean
    public WebThreadPoolController webThreadPoolController(WebThreadPoolService webThreadPoolService) {
        return new WebThreadPoolController(webThreadPoolService);
    }
}
