package com.blogs.dto;

import lombok.Data;

/**
 * 用户信息更新请求
 */
@Data
public class UserUpdateRequest {
    private String nickname;
    private String avatar;
    private String bio;
    private String email;
    private String github;
    private String zhihu;
    private String weixin;
}
