package com.blogs.dto;

import lombok.Data;

/**
 * 统计数据DTO
 */
@Data
public class StatisticsDTO {
    private Long totalArticles;
    private Long publishedArticles;
    private Long draftArticles;
    private Long totalViews;
    private Long totalLikes;
    private Long totalComments;
    private Long pendingComments;
    private Long totalCategories;
    private Long totalTags;
}
