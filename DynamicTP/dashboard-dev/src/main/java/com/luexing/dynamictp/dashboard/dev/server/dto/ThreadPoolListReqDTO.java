/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.dto;

import lombok.Data;

/**
 * 线程池控制台查询请求实体
 * <p>
 */
@Data
public class ThreadPoolListReqDTO {

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 服务名
     */
    private String serviceName;
}
