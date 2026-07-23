/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.remote.dto;

import lombok.Data;

/**
 * Nacos 配置明细响应实体
 * <p>
 */
@Data
public class NacosConfigRespDTO {

    /**
     * 配置的 Data ID
     */
    private String dataId;

    /**
     * 配置所属分组，例如：DEFAULT_GROUP
     */
    private String group;

    /**
     * 应用名
     */
    private String appName;

    /**
     * 配置中心内容
     */
    private String content;
}
