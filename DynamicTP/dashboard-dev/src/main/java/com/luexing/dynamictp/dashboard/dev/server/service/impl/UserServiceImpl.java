/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.luexing.dynamictp.dashboard.dev.server.config.DynamicTPProperties;
import com.luexing.dynamictp.dashboard.dev.server.dto.UserDetailRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.UserLoginReqDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.UserLoginRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户业务接口实现层
 * <p>
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final DynamicTPProperties dynamicTPProperties;

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        String actualAuth = requestParam.getUsername() + "," + requestParam.getPassword();
        if (!dynamicTPProperties.getUsers().contains(actualAuth)) {
            throw new RuntimeException("Invalid username or password");
        }

        StpUtil.login(requestParam.getUsername());
        SaSession session = StpUtil.getSession();
        session.set(
                "user",
                JSON.toJSONString(
                        UserDetailRespDTO.builder()
                                .userId("1")
                                .homePath("/")
                                .realName("管理员")
                                .username(requestParam.getUsername())
                                .desc("")
                                .avatar("/luexing-avatar.jpg")
                                .build()
                )
        );

        return UserLoginRespDTO.builder()
                .id("1") // 因为用户模块非主要，这里固定写
                .realName("管理员")
                .username(requestParam.getUsername())
                .password(requestParam.getPassword()) // 前端用的 https://github.com/vbenjs/vue-vben-admin，为了兼容框架返回。正常登录不需要返回密码
                .accessToken(StpUtil.getTokenValue())
                .build();
    }

    @Override
    public UserDetailRespDTO getUser() {
        return JSON.parseObject(StpUtil.getSession().get("user").toString(), UserDetailRespDTO.class);
    }
}
