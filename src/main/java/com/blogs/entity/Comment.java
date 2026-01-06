package com.blogs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 评论实体类
 *
 * 统一设计：
 * - targetId + targetType 绑定任意内容（ARTICLE / FORUM_POST）
 * - 保留 article_id 作为历史兼容字段（数据库字段不删除，旧数据可读）
 */
@Data
@Entity
@Table(name = "comment")
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 历史兼容字段：旧版本只支持文章评论（article_id）
     * - 新版本统一使用 targetId/targetType
     * - 读取旧数据时，如果 targetType 为空，则默认视为 ARTICLE
     */
    @Deprecated
    @Column(name = "article_id")
    private Long legacyArticleId;

    /**
     * 统一目标ID：文章ID / 帖子ID
     */
    @Column(name = "target_id")
    private Long targetId;

    /**
     * 统一目标类型：ARTICLE / FORUM_POST
     */
    @Column(name = "target_type", length = 20)
    private String targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(length = 50)
    private String nickname;
    
    @Column(length = 100)
    private String email;
    
    @Column(length = 200)
    private String website;
    
    @Column(length = 500)
    private String avatar;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "reply_to_id")
    private Long replyToId;
    
    @Column(name = "reply_to_nickname", length = 50)
    private String replyToNickname;
    
    /**
     * 是否为博主回复
     */
    @Column(name = "is_blogger")
    private Boolean isBlogger = false;
    
    /**
     * 审核状态：0-待审核 1-已通过 2-已拒绝
     */
    @Column(nullable = false)
    private Integer status = 1;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "like_count")
    private Long likeCount = 0L;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 兼容旧字段：前端/VO 仍可能使用 articleId
     */
    @Transient
    public Long getArticleId() {
        if ("ARTICLE".equalsIgnoreCase(targetType)) {
            return targetId;
        }
        return legacyArticleId;
    }
}
