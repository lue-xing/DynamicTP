/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.controller;

import com.luexing.dynamictp.dashboard.dev.server.common.Result;
import com.luexing.dynamictp.dashboard.dev.server.common.Results;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Grafana 控制器
 * <p>
 */
@RestController
public class GrafanaController {

    @Value("${dynamictp.grafana.url}")
    private String grafanaUrl;

    /**
     * 控制台获取 Grafana 预览地址
     */
    @GetMapping("/api/luexing-dashboard/grafana")
    public Result<String> getGrafanaUrl() {
        return Results.success(grafanaUrl);
    }
}
