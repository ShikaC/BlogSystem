package com.blogs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
@Entity
@Table(name = "comment")
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "article_id", nullable = false)
    private Long articleId;
    
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
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
