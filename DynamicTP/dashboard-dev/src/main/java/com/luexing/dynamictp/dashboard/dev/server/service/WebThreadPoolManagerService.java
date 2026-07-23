/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.service;

import com.luexing.dynamictp.dashboard.dev.server.dto.WebThreadPoolDetailRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.WebThreadPoolListReqDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.WebThreadPoolUpdateReqDTO;

import java.util.List;

/**
 * Web 线程池管理接口层
 * <p>
 */
public interface WebThreadPoolManagerService {

    /**
     * 查询线程池集合
     *
     * @param requestParam 请求参数
     * @return 线程池集合
     */
    List<WebThreadPoolDetailRespDTO> listThreadPool(WebThreadPoolListReqDTO requestParam);

    /**
     * 全局修改线程池参数
     *
     * @param requestParam 请求参数
     */
    void updateGlobalThreadPool(WebThreadPoolUpdateReqDTO requestParam);
}
