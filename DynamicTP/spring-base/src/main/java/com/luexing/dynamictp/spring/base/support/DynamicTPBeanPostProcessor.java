/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.spring.base.support;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.luexing.dynamictp.core.executor.DynamicTPExecutor;
import com.luexing.dynamictp.core.executor.DynamicTPRegistry;
import com.luexing.dynamictp.core.executor.ThreadPoolExecutorProperties;
import com.luexing.dynamictp.core.executor.support.BlockingQueueTypeEnum;
import com.luexing.dynamictp.core.executor.support.RejectedPolicyTypeEnum;
import com.luexing.dynamictp.spring.base.DynamicThreadPool;
import com.luexing.dynamictp.core.config.BootstrapConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 动态线程池后置处理器，扫描 Bean 是否为动态线程池，如果是的话进行属性填充和注册
 * <p>
 */
@Slf4j
@RequiredArgsConstructor
public class DynamicTPBeanPostProcessor implements BeanPostProcessor {

    private final BootstrapConfigProperties properties;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DynamicTPExecutor) {
            DynamicThreadPool dynamicThreadPool;
            try {
                // 通过 IOC 容器扫描 Bean 是否存在动态线程池注解
                dynamicThreadPool = ApplicationContextHolder.findAnnotationOnBean(beanName, DynamicThreadPool.class);
                if (Objects.isNull(dynamicThreadPool)) {
                    return bean;
                }
            } catch (Exception ex) {
                log.error("Failed to create dynamic thread pool in annotation mode.", ex);
                return bean;
            }

            DynamicTPExecutor dynamicTPExecutor = (DynamicTPExecutor) bean;
            // 从配置中心读取动态线程池配置并对线程池进行赋值
            ThreadPoolExecutorProperties executorProperties = properties.getExecutors()
                    .stream()
                    .filter(each -> Objects.equals(dynamicTPExecutor.getThreadPoolId(), each.getThreadPoolId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("The thread pool id does not exist in the configuration."));

            overrideLocalThreadPoolConfig(executorProperties, dynamicTPExecutor);

            // 注册到动态线程池注册器，后续监控和报警从注册器获取线程池实例。同时，参数动态变更需要依赖 ThreadPoolExecutorProperties 比对是否有边跟
            DynamicTPRegistry.putHolder(dynamicTPExecutor.getThreadPoolId(), dynamicTPExecutor, executorProperties);
        }

        return bean;
    }

    private void overrideLocalThreadPoolConfig(ThreadPoolExecutorProperties executorProperties, DynamicTPExecutor dynamicTPExecutor) {
        Integer remoteCorePoolSize = executorProperties.getCorePoolSize();
        Integer remoteMaximumPoolSize = executorProperties.getMaximumPoolSize();
        Assert.isTrue(remoteCorePoolSize <= remoteMaximumPoolSize, "remoteCorePoolSize must be smaller than remoteMaximumPoolSize.");

        // 兼容配置中心刷新时的 Bean 初始化顺序
        int originalMaximumPoolSize = dynamicTPExecutor.getMaximumPoolSize();
        if (remoteCorePoolSize > originalMaximumPoolSize) {
            dynamicTPExecutor.setMaximumPoolSize(remoteMaximumPoolSize);
            dynamicTPExecutor.setCorePoolSize(remoteCorePoolSize);
        } else {
            dynamicTPExecutor.setCorePoolSize(remoteCorePoolSize);
            dynamicTPExecutor.setMaximumPoolSize(remoteMaximumPoolSize);
        }

        // 阻塞队列没有常规 set 方法，所以使用反射赋值
        BlockingQueue workQueue = BlockingQueueTypeEnum.createBlockingQueue(executorProperties.getWorkQueue(), executorProperties.getQueueCapacity());
        // Java 9+ 的模块系统（JPMS）默认禁止通过反射访问 JDK 内部 API 的私有字段，所以需要配置开放反射权限
        // 在启动命令中增加以下参数，显式开放 java.util.concurrent 包
        // IDE 中通过在 VM options 中添加参数：--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
        // 部署的时候，在启动脚本（如 java -jar 命令）中加入该参数：java -jar --add-opens=java.base/java.util.concurrent=ALL-UNNAMED your-app.jar
        ReflectUtil.setFieldValue(dynamicTPExecutor, "workQueue", workQueue);

        // 赋值动态线程池其他核心参数
        dynamicTPExecutor.setKeepAliveTime(executorProperties.getKeepAliveTime(), TimeUnit.SECONDS);
        dynamicTPExecutor.allowCoreThreadTimeOut(executorProperties.getAllowCoreThreadTimeOut());
        dynamicTPExecutor.setRejectedExecutionHandler(RejectedPolicyTypeEnum.createPolicy(executorProperties.getRejectedHandler()));
    }
}
