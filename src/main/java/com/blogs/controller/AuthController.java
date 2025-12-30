package com.blogs.controller;

import com.blogs.common.Result;
import com.blogs.dto.LoginRequest;
import com.blogs.dto.LoginResponse;
import com.blogs.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 - 处理全站用户登录与注册
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户登录 (超级管理员与注册用户通用)
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    /**
     * 用户注册 (全员创作者)
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam String nickname) {
        userService.register(username, password, nickname);
        return Result.success();
    }
    
    /**
     * 初始化管理员账号（仅首次使用）
     */
    @PostMapping("/init")
    public Result<Void> initAdmin(@RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam(required = false, defaultValue = "超级管理员") String nickname) {
        userService.initAdmin(username, password, nickname);
        return Result.success();
    }
}
