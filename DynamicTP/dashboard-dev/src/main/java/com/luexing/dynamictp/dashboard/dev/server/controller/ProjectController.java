/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.controller;

import com.luexing.dynamictp.dashboard.dev.server.common.Result;
import com.luexing.dynamictp.dashboard.dev.server.common.Results;
import com.luexing.dynamictp.dashboard.dev.server.dto.ProjectInfoRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目控制器
 * <p>
 */
@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 查看包含动态线程池项目列表
     */
    @GetMapping("/api/luexing-dashboard/projects")
    public Result<List<ProjectInfoRespDTO>> listProjects() {
        return Results.success(projectService.listProject());
    }
}
