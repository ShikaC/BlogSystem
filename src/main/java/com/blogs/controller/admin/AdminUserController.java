package com.blogs.controller.admin;

import com.blogs.common.Result;
import com.blogs.dto.UserDTO;
import com.blogs.dto.UserDetailDTO;
import com.blogs.dto.UserUpdateRequest;
import com.blogs.dto.PasswordUpdateRequest;
import com.blogs.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台用户管理控制器 - 仅超级管理员可访问
 */
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有用户列表（旧接口，保持兼容）
     */
    @GetMapping
    public Result<List<UserDTO>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    /**
     * 分页获取用户列表（支持筛选）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(userService.getUserList(keyword, role, status, page, size));
    }

    /**
     * 获取用户详情（含文章、帖子记录）
     */
    @GetMapping("/{id}/detail")
    public Result<UserDetailDTO> getUserDetail(@PathVariable Long id) {
        return Result.success(userService.getUserDetail(id));
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Long>> getUserStatistics() {
        return Result.success(userService.getUserStatistics());
    }

    /**
     * 更新用户状态 (禁用/解封)
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/current")
    public Result<UserDTO> getCurrentAdmin() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return Result.success(userService.getUserInfo(username));
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.updateUserInfo(username, request);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.updatePassword(username, request);
        return Result.success();
    }
}
