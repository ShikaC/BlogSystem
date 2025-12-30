package com.blogs.dto;

import lombok.Data;

/**
 * 用户信息DTO
 */
@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
    private String email;
    private String role;
    private Integer status;
    private String github;
    private String zhihu;
    private String weixin;
}
