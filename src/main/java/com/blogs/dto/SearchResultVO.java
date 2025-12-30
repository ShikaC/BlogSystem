package com.blogs.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 全站搜索结果项（文章/帖子统一）
 */
@Data
public class SearchResultVO {
    private String contentType; // ARTICLE / FORUM_POST
    private Long id;
    private String title;
    private String excerpt;
    private String username;
    private String nickname;
    private Long viewCount;
    private LocalDateTime createdAt;
}


