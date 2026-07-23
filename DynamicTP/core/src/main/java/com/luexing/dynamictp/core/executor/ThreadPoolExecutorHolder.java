/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.core.executor;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池执行器持有者对象
 * <p>
 */
@Data
@AllArgsConstructor
public class ThreadPoolExecutorHolder {

    /**
     * 线程池唯一标识
     */
    private String threadPoolId;

    /**
     * 线程池
     */
    private ThreadPoolExecutor executor;

    /**
     * 线程池属性参数
     */
    private ThreadPoolExecutorProperties executorProperties;
}
