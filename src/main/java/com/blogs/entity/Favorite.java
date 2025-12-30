package com.blogs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 收藏实体类
 */
@Data
@Entity
@Table(name = "favorite", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "target_id", "type"})})
public class Favorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "target_id", nullable = false)
    private Long targetId;
    
    /**
     * 类型: ARTICLE, POST
     */
    @Column(nullable = false, length = 20)
    private String type;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

