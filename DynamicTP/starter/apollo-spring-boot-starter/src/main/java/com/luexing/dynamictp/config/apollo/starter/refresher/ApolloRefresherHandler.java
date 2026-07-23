/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.config.apollo.starter.refresher;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.luexing.dynamictp.config.common.starter.refresher.AbstractDynamicThreadPoolRefresher;
import com.luexing.dynamictp.core.notification.service.NotifierDispatcher;
import com.luexing.dynamictp.core.config.BootstrapConfigProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Apollo 配置中心刷新处理器
 * <p>
 */
@Slf4j(topic = "DynamicTPConfigRefresher")
public class ApolloRefresherHandler extends AbstractDynamicThreadPoolRefresher {

    public ApolloRefresherHandler(BootstrapConfigProperties properties) {
        super(properties);
    }

    @SneakyThrows
    public void registerListener() {
        BootstrapConfigProperties.ApolloConfig apolloConfig = properties.getApollo();
        String[] apolloNamespaces = apolloConfig.getNamespace().split(",");

        String namespace = apolloNamespaces[0];
        String configFileType = properties.getConfigFileType().getValue();
        Config config = ConfigService.getConfig(String.format("%s.%s", namespace, properties.getConfigFileType().getValue()));

        ConfigChangeListener configChangeListener = createConfigChangeListener(namespace, configFileType);
        config.addChangeListener(configChangeListener);

        log.info("Dynamic thread pool refresher, add apollo listener success. namespace: {}", namespace);
    }

    private ConfigChangeListener createConfigChangeListener(String namespace, String configFileType) {
        return configChangeEvent -> {
            String namespaceItem = namespace.replace("." + configFileType, "");
            ConfigFileFormat configFileFormat = ConfigFileFormat.fromString(configFileType);
            ConfigFile configFile = ConfigService.getConfigFile(namespaceItem, configFileFormat);
            refreshThreadPoolProperties(configFile.getContent());
        };
    }
}
