package com.blogs.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情DTO - 包含用户完整信息及发布记录
 */
@Data
public class UserDetailDTO {
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
    private Boolean likesPublic;
    private Boolean favoritesPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 统计信息
    private Long articleCount;
    private Long postCount;
    private Long commentCount;

    // 发布的文章列表（简要信息）
    private List<ArticleSummary> articles;

    // 发布的帖子列表（简要信息）
    private List<PostSummary> posts;

    @Data
    public static class ArticleSummary {
        private Long id;
        private String title;
        private Integer status;
        private Integer viewCount;
        private Integer likeCount;
        private LocalDateTime createdAt;
    }

    @Data
    public static class PostSummary {
        private Long id;
        private String title;
        private Integer status;
        private Integer viewCount;
        private Integer likeCount;
        private LocalDateTime createdAt;
    }
}
