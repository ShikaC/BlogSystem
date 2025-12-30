package com.blogs.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人中心：我的收藏/点赞 统一展示项
 */
@Data
public class UserActionItemVO {

    /**
     * ARTICLE / FORUM_POST
     */
    private String contentType;

    private Long targetId;
    private String title;
    private LocalDateTime createdAt;
}


