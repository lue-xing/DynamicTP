/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.controller;

import com.luexing.dynamictp.dashboard.dev.server.common.Result;
import com.luexing.dynamictp.dashboard.dev.server.common.Results;
import com.luexing.dynamictp.dashboard.dev.server.dto.WebThreadPoolBaseMetricsRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.WebThreadPoolStateRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.service.WebThreadPoolInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Web 线程池实例控制层
 * <p>
 */
@RestController
@RequiredArgsConstructor
public class WebThreadPoolInstanceController {

    private final WebThreadPoolInstanceService webThreadPoolInstanceService;

    /**
     * 获取线程池列表
     */
    @GetMapping("/api/luexing-dashboard/web/thread-pools/{namespace}/{serviceName}/basic-metrics")
    public Result<List<WebThreadPoolBaseMetricsRespDTO>> listBasicMetrics(
            @PathVariable String namespace,
            @PathVariable String serviceName) {
        return Results.success(webThreadPoolInstanceService.listBasicMetrics(namespace, serviceName));
    }

    /**
     * 获取线程池的完整运行时状态
     */
    @GetMapping("/api/luexing-dashboard/web/thread-pool/{networkAddress}")
    public Result<WebThreadPoolStateRespDTO> getRuntimeState(@PathVariable String networkAddress) {
        return Results.success(webThreadPoolInstanceService.getRuntimeState(networkAddress));
    }
}
