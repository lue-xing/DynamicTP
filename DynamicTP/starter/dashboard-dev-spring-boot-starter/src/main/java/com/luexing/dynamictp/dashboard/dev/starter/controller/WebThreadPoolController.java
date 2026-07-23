/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.starter.controller;

import com.luexing.dynamictp.dashboard.dev.starter.core.Result;
import com.luexing.dynamictp.dashboard.dev.starter.core.Results;
import com.luexing.dynamictp.dashboard.dev.starter.dto.WebThreadPoolDashBoardDevRespDTO;
import com.luexing.dynamictp.dashboard.dev.starter.service.WebThreadPoolService;
import com.luexing.dynamictp.web.starter.core.WebThreadPoolBaseMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Web 线程池控制器
 * <p>
 */
@RestController
@RequiredArgsConstructor
public class WebThreadPoolController {

    private final WebThreadPoolService webThreadPoolService;

    /**
     * 获取 Web 线程池的轻量级运行指标
     */
    @GetMapping("/web/thread-pool/basic-metrics")
    public Result<WebThreadPoolBaseMetrics> getBasicMetrics() {
        return Results.success(webThreadPoolService.getBasicMetrics());
    }

    /**
     * 获取 Web 线程池的完整运行时状态
     */
    @GetMapping("/web/thread-pool")
    public Result<WebThreadPoolDashBoardDevRespDTO> getRuntimeInfo() {
        return Results.success(webThreadPoolService.getRuntimeInfo());
    }
}
