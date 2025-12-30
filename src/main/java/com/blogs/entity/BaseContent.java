package com.blogs.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

/**
 * 全站统一内容抽象父类（博客文章 / 论坛帖子同源设计）
 *
 * 说明：
 * - 使用 @MappedSuperclass 将共性字段上收，子类只保留各自特有字段；
 * - 数据库层面不删除任何原字段，依赖 spring.jpa.hibernate.ddl-auto=update 增量对齐字段。
 */
@Data
@MappedSuperclass
public abstract class BaseContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 发布者ID（统一字段）
     * - Article 仍保留 User 关联用于展示/查询，但以 userId 作为落库主字段
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 内容状态（统一字段）
     * - Article: 0-草稿 1-已发布 2-私密 3-回收站
     * - ForumPost: 0-草稿 1-已发布 2-私密 3-回收站 4-待审核
     */
    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "is_top")
    private Boolean isTop = false;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "like_count")
    private Long likeCount = 0L;

    /**
     * 收藏数（原系统文章/帖子均存在）
     */
    @Column(name = "collect_count")
    private Long collectCount = 0L;

    /**
     * 评论/回帖数（统一字段）
     */
    @Column(name = "comment_count")
    private Long commentCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}


