package com.blogs.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 统计数据DTO
 */
@Data
public class StatisticsDTO {
    // 基础统计
    private Long totalArticles;
    private Long totalPosts;
    private Long totalUsers;
    private Long totalComments;
    private Long totalPostComments;
    private Long totalViews;
    private Long totalLikes;
    private Long totalCollects;
    private Long publishedArticles;
    private Long draftArticles;
    private Long pendingComments;
    private Long totalCategories;
    private Long totalTags;
    
    // 今日新增
    private Long todayNewUsers;
    private Long todayNewArticles;
    private Long todayNewPosts;
    private Long todayNewComments;
    
    // 活跃用户（近7天登录）
    private Long activeUsers;
    
    // 趋势数据（用于图表）
    private List<Map<String, Object>> userTrend; // 用户增长趋势
    private List<Map<String, Object>> contentTrend; // 内容增长趋势
    private List<Map<String, Object>> interactionTrend; // 互动趋势
}
