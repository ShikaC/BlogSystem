package com.blogs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 消息通知实体类
 */
@Data
@Entity
@Table(name = "notification")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;
    
    @Column(name = "sender_id")
    private Long senderId;
    
    /**
     * 类型: COMMENT, REPLY, LIKE, COLLECT, SYSTEM, AUDIT
     */
    @Column(nullable = false, length = 50)
    private String type;
    
    @Column(length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "target_id")
    private Long targetId;
    
    /**
     * 目标类型: ARTICLE, POST, COMMENT
     */
    @Column(name = "target_type", length = 20)
    private String targetType;
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

