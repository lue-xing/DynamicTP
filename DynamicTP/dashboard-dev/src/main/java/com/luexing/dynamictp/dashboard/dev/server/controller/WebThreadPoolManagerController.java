/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.controller;

import com.luexing.dynamictp.dashboard.dev.server.common.Result;
import com.luexing.dynamictp.dashboard.dev.server.common.Results;
import com.luexing.dynamictp.dashboard.dev.server.dto.WebThreadPoolDetailRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.WebThreadPoolListReqDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.WebThreadPoolUpdateReqDTO;
import com.luexing.dynamictp.dashboard.dev.server.service.WebThreadPoolManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Web 线程池管理控制器层
 * <p>
 */
@RestController
@RequiredArgsConstructor
public class WebThreadPoolManagerController {

    private final WebThreadPoolManagerService webThreadPoolManagerService;

    /**
     * 查询线程池集合
     */
    @GetMapping("/api/luexing-dashboard/web/thread-pools")
    public Result<List<WebThreadPoolDetailRespDTO>> listThreadPool(WebThreadPoolListReqDTO requestParam) {
        return Results.success(webThreadPoolManagerService.listThreadPool(requestParam));
    }

    /**
     * 更新线程池
     */
    @PutMapping("/api/luexing-dashboard/web/thread-pool")
    public Result<Void> updateGlobalThreadPool(@RequestBody @Valid WebThreadPoolUpdateReqDTO requestParam) {
        webThreadPoolManagerService.updateGlobalThreadPool(requestParam);
        return Results.success();
    }
}
