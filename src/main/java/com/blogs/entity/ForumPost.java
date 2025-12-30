package com.blogs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 论坛帖子实体类
 */
@Data
@Entity
@Table(name = "forum_post")
public class ForumPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "section_id", nullable = false)
    private Long sectionId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    
    /**
     * 状态: 0-草稿 1-已发布 2-私密 3-回收站 4-待审核
     */
    @Column(nullable = false)
    private Integer status = 1;
    
    @Column(name = "is_top")
    private Boolean isTop = false;
    
    @Column(name = "is_essence")
    private Boolean isEssence = false;
    
    @Column(name = "view_count")
    private Long viewCount = 0L;
    
    @Column(name = "like_count")
    private Long likeCount = 0L;
    
    @Column(name = "collect_count")
    private Long collectCount = 0L;
    
    @Column(name = "comment_count")
    private Long commentCount = 0L;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

