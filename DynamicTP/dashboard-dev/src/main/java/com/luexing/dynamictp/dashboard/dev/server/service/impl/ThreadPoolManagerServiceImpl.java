/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.luexing.dynamictp.dashboard.dev.server.config.DashBoardConfigProperties;
import com.luexing.dynamictp.dashboard.dev.server.config.DynamicTPProperties;
import com.luexing.dynamictp.dashboard.dev.server.dto.ThreadPoolDetailRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.ThreadPoolListReqDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.ThreadPoolUpdateReqDTO;
import com.luexing.dynamictp.dashboard.dev.server.remote.client.NacosProxyClient;
import com.luexing.dynamictp.dashboard.dev.server.remote.dto.NacosConfigDetailRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.remote.dto.NacosConfigRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.service.ThreadPoolManagerService;
import com.luexing.dynamictp.dashboard.dev.server.service.handler.YamlConfigParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 线程池管理接口实现层
 * <p>
 */
@Service
@RequiredArgsConstructor
public class ThreadPoolManagerServiceImpl implements ThreadPoolManagerService {

    private final DynamicTPProperties dynamicTPProperties;
    private final NacosProxyClient nacosProxyClient;
    private final YamlConfigParser yamlConfigParser;

    @Override
    public List<ThreadPoolDetailRespDTO> listThreadPool(ThreadPoolListReqDTO requestParam) {
        // 处理 namespace 过滤
        List<String> namespaces = new ArrayList<>(dynamicTPProperties.getNamespaces());
        String requestedNamespace = requestParam.getNamespace();
        String requestedServiceName = requestParam.getServiceName();
        if (StrUtil.isNotBlank(requestedNamespace) && namespaces.contains(requestedNamespace)) {
            namespaces = Collections.singletonList(requestedNamespace);
        }

        // 并行拉取各 namespace 的配置，并生成 (namespace, config) 任务对
        List<Map.Entry<String, NacosConfigRespDTO>> tasks = namespaces
                .parallelStream()
                .flatMap(ns -> {
                    List<NacosConfigRespDTO> cfgs = nacosProxyClient.listConfig(ns);
                    if (CollUtil.isEmpty(cfgs)) {
                        return Stream.<Map.Entry<String, NacosConfigRespDTO>>empty();
                    }
                    return cfgs.stream()
                            .filter(each -> StrUtil.isNotBlank(each.getAppName()))
                            .filter(each -> StrUtil.isBlank(requestedServiceName) || Objects.equals(each.getAppName(), requestedServiceName))
                            .map(cfg -> new AbstractMap.SimpleEntry<>(ns, cfg));
                })
                .collect(Collectors.toList());

        // 并行处理任务：解析 YAML -> 绑定配置 -> 查询服务实例数 -> 拼装返回
        return tasks.parallelStream()
                .map(entry -> {
                    String namespace = entry.getKey();
                    NacosConfigRespDTO config = entry.getValue();

                    // 解析 YAML
                    Map<Object, Object> configInfoMap = yamlConfigParser.doParse(config.getContent());
                    ConfigurationPropertySource sources = new MapConfigurationPropertySource(configInfoMap);
                    Binder binder = new Binder(sources);

                    // 绑定 dynamictp.*
                    BindResult<DashBoardConfigProperties> bound =
                            binder.bind("dynamictp", Bindable.of(DashBoardConfigProperties.class));
                    if (!bound.isBound()) {
                        return Collections.<ThreadPoolDetailRespDTO>emptyList();
                    }

                    DashBoardConfigProperties refresherProperties = bound.get();

                    // 查询当前服务在 Nacos 的实例数
                    NacosServiceListRespDTO service = nacosProxyClient.getService(namespace, config.getAppName());

                    // 补齐返回字段
                    refresherProperties.getExecutors().forEach(each -> {
                        each.setNamespace(namespace);
                        each.setServiceName(config.getAppName());
                        each.setDataId(config.getDataId());
                        each.setGroup(config.getGroup());
                        each.setInstanceCount(service.getCount());
                    });

                    return refresherProperties.getExecutors();
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public void updateGlobalThreadPool(ThreadPoolUpdateReqDTO requestParam) {
        NacosConfigDetailRespDTO configDetail = nacosProxyClient.getConfig(requestParam.getNamespace(), requestParam.getDataId(), requestParam.getGroup());
        String originalContent = configDetail.getContent();

        Map<Object, Object> configInfoMap = yamlConfigParser.doParse(originalContent);
        ConfigurationPropertySource source = new MapConfigurationPropertySource(configInfoMap);

        Binder binder = new Binder(source);
        DashBoardConfigProperties dynamictp = binder.bind("dynamictp", Bindable.of(DashBoardConfigProperties.class))
                .orElseThrow(() -> new RuntimeException("binding failed"));

        dynamictp.getExecutors().stream()
                .filter(e -> e.getThreadPoolId().equals(requestParam.getThreadPoolId()))
                .findFirst()
                .ifPresent(e -> {
                    e.setCorePoolSize(requestParam.getCorePoolSize());
                    e.setMaximumPoolSize(requestParam.getMaximumPoolSize());
                    e.setKeepAliveTime(requestParam.getKeepAliveTime());
                    e.setQueueCapacity(requestParam.getQueueCapacity());
                    e.setWorkQueue(requestParam.getWorkQueue());
                    e.setRejectedHandler(requestParam.getRejectedHandler());
                    e.setAllowCoreThreadTimeOut(requestParam.getAllowCoreThreadTimeOut());
                    e.setNotify(BeanUtil.toBean(requestParam.getNotify(), ThreadPoolDetailRespDTO.NotifyConfig.class));
                    e.setAlarm(BeanUtil.toBean(requestParam.getAlarm(), ThreadPoolDetailRespDTO.AlarmConfig.class));
                });

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
