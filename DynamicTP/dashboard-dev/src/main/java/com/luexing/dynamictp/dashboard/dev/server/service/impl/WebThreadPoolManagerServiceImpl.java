/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.luexing.dynamictp.dashboard.dev.server.common.Result;
import com.luexing.dynamictp.dashboard.dev.server.config.DashBoardConfigProperties;
import com.luexing.dynamictp.dashboard.dev.server.config.DynamicTPProperties;
import com.luexing.dynamictp.dashboard.dev.server.dto.WebThreadPoolDetailRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.WebThreadPoolListReqDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.WebThreadPoolStateRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.WebThreadPoolUpdateReqDTO;
import com.luexing.dynamictp.dashboard.dev.server.remote.client.NacosProxyClient;
import com.luexing.dynamictp.dashboard.dev.server.remote.dto.NacosConfigDetailRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.remote.dto.NacosConfigRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.remote.dto.NacosServiceRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.service.WebThreadPoolManagerService;
import com.luexing.dynamictp.dashboard.dev.server.service.handler.YamlConfigParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Web 线程池管理接口实现层
 * <p>
 */
@Service
@RequiredArgsConstructor
public class WebThreadPoolManagerServiceImpl implements WebThreadPoolManagerService {

    private final DynamicTPProperties dynamicTPProperties;
    private final NacosProxyClient nacosProxyClient;
    private final YamlConfigParser yamlConfigParser;

    @Override
    public List<WebThreadPoolDetailRespDTO> listThreadPool(WebThreadPoolListReqDTO requestParam) {
        List<WebThreadPoolDetailRespDTO> threadPools = new ArrayList<>();

        List<String> namespaces = new ArrayList<>(dynamicTPProperties.getNamespaces());
        String requestedNamespace = requestParam.getNamespace();
        String requestedServiceName = requestParam.getServiceName();
        if (StrUtil.isNotBlank(requestedNamespace) && namespaces.contains(requestedNamespace)) {
            // 只保留匹配的 namespace
            namespaces.clear();
            namespaces.add(requestedNamespace);
        }

        namespaces.forEach(namespace -> {
            List<NacosConfigRespDTO> nacosConfigResponse = nacosProxyClient.listConfig(namespace);
            if (CollUtil.isNotEmpty(nacosConfigResponse)) {
                nacosConfigResponse
                        .stream()
                        .filter(each -> {
                            if (StrUtil.isBlank(each.getAppName())) {
                                return false;
                            }
                            return StrUtil.isBlank(requestedServiceName) || Objects.equals(each.getAppName(), requestedServiceName);
                        })
                        .forEach(config -> {
                            // 此处应根据配置文件的类型进行判断，比如 YAML 或者 Properties，为了简化非核心流程，默认处理 YAML
                            Map<Object, Object> configInfoMap = yamlConfigParser.doParse(config.getContent());

                            // 将 Map 值绑定到 DashBoardConfigProperties 类属性
                            ConfigurationPropertySource sources = new MapConfigurationPropertySource(configInfoMap);
                            Binder binder = new Binder(sources);

                            DashBoardConfigProperties refresherProperties;
                            try {
                                refresherProperties = binder
                                        .bind("dynamictp", Bindable.of(DashBoardConfigProperties.class))
                                        .orElseThrow(() -> new IllegalArgumentException("dynamictp config binding failed"));
                            } catch (Exception e) {
                                return;
                            }

                            NacosServiceListRespDTO service = nacosProxyClient.getService(namespace, config.getAppName());
                            DashBoardConfigProperties.WebThreadPoolExecutorConfig webThreadPoolConfig = refresherProperties.getWeb();
                            if (service == null || CollUtil.isEmpty(service.getServiceList()) || webThreadPoolConfig == null) {
                                return;
                            }

                            NacosServiceRespDTO nacosService = service.getServiceList().get(0);
                            String networkAddress = nacosService.getIp() + ":" + nacosService.getPort();

                            Result<WebThreadPoolStateRespDTO> result;
                            try {
                                String resultStr = HttpUtil.get("http://" + networkAddress + "/web/thread-pool", 1000);
                                result = JSON.parseObject(resultStr, new TypeReference<>() {
                                });
                            } catch (Exception e) {
                                return;
                            }
                            String webContainerName = result.getData().getWebContainerName();

                            WebThreadPoolDetailRespDTO webThreadPool = WebThreadPoolDetailRespDTO.builder()
                                    .webContainerName(webContainerName)
                                    .namespace(namespace)
                                    .serviceName(config.getAppName())
                                    .dataId(config.getDataId())
                                    .group(config.getGroup())
                                    .instanceCount(service.getCount())
                                    .corePoolSize(webThreadPoolConfig.getCorePoolSize())
                                    .maximumPoolSize(webThreadPoolConfig.getMaximumPoolSize())
                                    .keepAliveTime(webThreadPoolConfig.getKeepAliveTime())
                                    .notify(BeanUtil.toBean(webThreadPoolConfig.getNotify(), WebThreadPoolDetailRespDTO.NotifyConfig.class))
                                    .build();
                            threadPools.add(webThreadPool);
                        });
            }
        });

        return threadPools;
    }

    @SneakyThrows
    @Override
    public void updateGlobalThreadPool(WebThreadPoolUpdateReqDTO requestParam) {
        NacosConfigDetailRespDTO configDetail = nacosProxyClient.getConfig(requestParam.getNamespace(), requestParam.getDataId(), requestParam.getGroup());
        String originalContent = configDetail.getContent();

        Map<Object, Object> configInfoMap = yamlConfigParser.doParse(originalContent);
        ConfigurationPropertySource source = new MapConfigurationPropertySource(configInfoMap);

        Binder binder = new Binder(source);
        DashBoardConfigProperties dynamictp = binder.bind("dynamictp", Bindable.of(DashBoardConfigProperties.class))
                .orElseThrow(() -> new RuntimeException("binding failed"));

        dynamictp.setWeb(BeanUtil.toBean(requestParam, DashBoardConfigProperties.WebThreadPoolExecutorConfig.class));

        Map<String, Object> updatedMap = new LinkedHashMap<>();
        updatedMap.put("dynamictp", dynamictp);

        YAMLFactory factory = new YAMLFactory();
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER); // 去除 Yaml 字符串开头 ---
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);

        ObjectMapper objectMapper = new ObjectMapper(factory);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 去除 null 字段
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE); // 驼峰命名转 -，比如 corePooSize 转 core-pool-size

        String yamlStr = objectMapper.writeValueAsString(Collections.singletonMap("dynamictp", dynamictp));
        nacosProxyClient.publishConfig(requestParam.getNamespace(), requestParam.getDataId(), requestParam.getGroup(), configDetail.getAppName(), configDetail.getId(), configDetail.getMd5(), yamlStr, "yaml");
    }
}
