/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.service;

import com.luexing.dynamictp.dashboard.dev.server.dto.ProjectInfoRespDTO;

import java.util.List;

/**
 * 项目接口层
 * <p>
 */
public interface ProjectService {

    /**
     * 查询项目集合
     *
     * @return 项目集合数据
     */
    List<ProjectInfoRespDTO> listProject();
}
