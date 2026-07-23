/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 通用返回结果封装类
 * <p>
 */
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = -4408341719434417427L;

    /**
     * 成功状态码，通常使用 "0" 表示成功
     */
    public static final Integer SUCCESS_CODE = 0;

    /**
     * 状态码，"0" 表示成功，其他为失败或异常
     */
    private Integer code;

    /**
     * 提示信息或异常描述
     */
    private String message;

    /**
     * 返回数据主体
     */
    private T data;

    /**
     * 判断是否为成功返回
     */
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }
}
