package com.blogs.dto;

import lombok.Data;

/**
 * 博主信息DTO
 */
@Data
public class BloggerDTO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
    private String email;
    private String github;
    private String zhihu;
    private String weixin;
}
