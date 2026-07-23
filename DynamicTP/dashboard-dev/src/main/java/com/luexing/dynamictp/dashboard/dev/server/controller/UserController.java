/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.controller;

import com.luexing.dynamictp.dashboard.dev.server.common.Result;
import com.luexing.dynamictp.dashboard.dev.server.common.Results;
import com.luexing.dynamictp.dashboard.dev.server.dto.UserDetailRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.UserLoginReqDTO;
import com.luexing.dynamictp.dashboard.dev.server.dto.UserLoginRespDTO;
import com.luexing.dynamictp.dashboard.dev.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户业务控制层
 * <p>
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/api/luexing-dashboard/auth/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam) {
        return Results.success(userService.login(requestParam));
    }

    /**
     * 查询用户信息
     */
    @GetMapping("/api/luexing-dashboard/user")
    public Result<UserDetailRespDTO> getUser() {
        return Results.success(userService.getUser());
    }
}
