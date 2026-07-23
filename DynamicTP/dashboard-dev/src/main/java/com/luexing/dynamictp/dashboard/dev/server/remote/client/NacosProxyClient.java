/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.remote.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.luexing.dynamictp.dashboard.dev.server.remote.dto.NacosConfigDetailRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.remote.dto.NacosConfigListRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.remote.dto.NacosConfigRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Nacos 代理客户端
 * <p>
 */
@Slf4j
@Component
public class NacosProxyClient {

    @Value("${dynamictp.nacos.server-addr}")
    private String serverAddr;

    /**
     * 查询命名空间下配置文件集合
     *
     * @param namespace 命名空间
     * @return 配置文件集合
     */
    public List<NacosConfigRespDTO> listConfig(String namespace) {
        String url = serverAddr + "/nacos/v1/cs/configs";

        HttpResponse response = HttpRequest.get(url)
                .form("pageNo", "1")
                .form("pageSize", "100") // 默认单个 namespace 最大 100 条数据，如果超过写个 while 循环读取即可
                .form("tenant", Objects.equals(namespace, "public") ? "" : namespace)
                .form("search", "blur")
                .form("dataId", "")
                .form("group", "")
                .form("appName", "")
                .form("config_tags", "")
                .form("search", "blur")
                .execute();

        String result = response.body();
        if (!response.isOk()) {
            throw new RuntimeException("Nacos server returned error.");
        }

        NacosConfigListRespDTO nacosRemoteResult = JSON.parseObject(result, NacosConfigListRespDTO.class);
        return nacosRemoteResult.getPageItems();
    }

    /**
     * 查询配置明细信息
     *
     * @param namespace 命名空间
     * @param dataId    数据 ID
     * @param group     分组标识
     * @return 配置明细
     */
    public NacosConfigDetailRespDTO getConfig(String namespace, String dataId, String group) {
        String url = serverAddr + "/nacos/v1/cs/configs";

        // 构建请求并发送
        HttpResponse response = HttpRequest.get(url)
                .form("dataId", dataId)
                .form("group", group)
                .form("namespaceId", Objects.equals(namespace, "public") ? "" : namespace)
                .form("tenant", Objects.equals(namespace, "public") ? "" : namespace)
                .form("show", "all")
                .execute();

        String result = response.body();
        if (!response.isOk()) {
            throw new RuntimeException("Nacos server returned error.");
        }

        return JSON.parseObject(result, NacosConfigDetailRespDTO.class);
    }

    /**
     * 发布配置
     *
     * @param namespace   命名空间
     * @param dataId      数据 ID
     * @param group       分组标识
     * @param content     配置文件内容
     * @param contentType 配置文件内容文件格式
     */
    public void publishConfig(String namespace, String dataId, String group, String appName, String id, String md5, String content, String contentType) {
        String url = serverAddr + "/nacos/v1/cs/configs";

        Map<String, Object> form = new HashMap<>();
        form.put("tenant", Objects.equals(namespace, "public") ? "" : namespace);
        form.put("dataId", dataId);
        form.put("group", group);
        form.put("appName", appName);
        form.put("id", id);
        form.put("md5", md5);
        form.put("content", content);
        form.put("type", contentType);

        // 发起 POST 请求
        HttpResponse response = HttpRequest.post(url)
                .form(form)
                .execute();

        if (!response.isOk()) {
            throw new RuntimeException("Nacos server returned error.");
        }
    }

    /**
     * 查询命名空间下服务明细
     *
     * @param namespace   命名空间
     * @param serviceName 服务名
     * @return 服务明细响应
     */
    public NacosServiceListRespDTO getService(String namespace, String serviceName) {
        String url = serverAddr + "/nacos/v1/ns/catalog/instances";

        HttpResponse response = HttpRequest.get(url)
                .form("pageNo", "1")
                .form("pageSize", "100") // 默认单个 service 最大 100 条数据，如果超过写个 while 循环读取即可
                .form("clusterName", "DEFAULT")
                .form("groupName", "DEFAULT_GROUP")
                .form("serviceName", serviceName)
                .form("namespaceId", Objects.equals(namespace, "public") ? "" : namespace)
                .execute();

        String result = response.body();
        if (!response.isOk()) {
            log.warn(result);
            return NacosServiceListRespDTO.builder()
                    .count(0)
                    .build();
        }

        return JSON.parseObject(result, NacosServiceListRespDTO.class);
    }
}
