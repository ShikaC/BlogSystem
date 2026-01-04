package com.blogs.dto;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {
    private String token;
    private Long id;  // 添加用户ID
    private String nickname;
    private String avatar;
    private String role;
    
    public static LoginResponse of(String token, Long userId, String nickname, String avatar, String role) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setId(userId);  // 设置用户ID
        response.setNickname(nickname);
        response.setAvatar(avatar);
        response.setRole(role);
        return response;
    }
}