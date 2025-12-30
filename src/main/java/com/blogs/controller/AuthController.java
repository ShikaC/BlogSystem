package com.blogs.controller;

import com.blogs.common.Result;
import com.blogs.dto.LoginRequest;
import com.blogs.dto.LoginResponse;
import com.blogs.service.BloggerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private BloggerService bloggerService;
    
    /**
     * 博主登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = bloggerService.login(request);
        return Result.success(response);
    }
    
    /**
     * 初始化博主账号（仅首次使用）
     */
    @PostMapping("/init")
    public Result<Void> initBlogger(@RequestParam String username,
                                    @RequestParam String password,
                                    @RequestParam(required = false, defaultValue = "博主") String nickname) {
        bloggerService.initBlogger(username, password, nickname);
        return Result.success();
    }
}
