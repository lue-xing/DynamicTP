/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.service;

import com.luexing.dynamictp.dashboard.dev.server.dto.UserDetailRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.UserLoginReqDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.UserLoginRespDTO;

/**
 * 用户业务接口层
 * <p>
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param requestParam 用户名、密码
     * @return 用户登录返回信息
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 获取用户明细信息
     *
     * @return 用户明细信息
     */
    UserDetailRespDTO getUser();
}
