package com.blogs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 博主实体类 - 单博主模式
 */
@Data
@Entity
@Table(name = "blogger")
public class Blogger {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(length = 50)
    private String nickname;
    
    @Column(length = 500)
    private String avatar;
    
    @Column(length = 1000)
    private String bio;
    
    @Column(length = 100)
    private String email;
    
    @Column(length = 100)
    private String github;
    
    @Column(length = 100)
    private String zhihu;
    
    @Column(length = 100)
    private String weixin;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
