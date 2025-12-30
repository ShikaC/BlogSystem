package com.blogs.dto;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {
    private String token;
    private String nickname;
    private String avatar;
    
    public static LoginResponse of(String token, String nickname, String avatar) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setNickname(nickname);
        response.setAvatar(avatar);
        return response;
    }
}
