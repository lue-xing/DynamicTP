/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.remote.dto;

import lombok.Data;

/**
 * Nacos 服务明细实体
 * <p>
 */
@Data
public class NacosServiceRespDTO {

    /**
     * IP
     */
    private String ip;

    /**
     * Port
     */
    private Integer port;
}
