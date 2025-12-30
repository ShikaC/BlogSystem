package com.blogs.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台全站内容管理统一 VO（文章/帖子）
 */
@Data
public class ContentAdminVO {

    /**
     * ARTICLE / FORUM_POST
     */
    private String contentType;

    private Long id;
    private Long userId;
    private String title;
    private Integer status;
    private Boolean isTop;

    private Long viewCount;
    private Long likeCount;
    private Long collectCount;
    private Long commentCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ARTICLE 专属
    private Long categoryId;

    // FORUM_POST 专属
    private Long sectionId;
    private Boolean isEssence;
}


