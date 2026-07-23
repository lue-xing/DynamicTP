/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * dynamicTP DashBoard 配置文件
 * <p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "dynamictp")
public class DynamicTPProperties {

    /**
     * 用户集合
     */
    private List<String> users;

    /**
     * Nacos 命名空间
     */
    private List<String> namespaces;
}