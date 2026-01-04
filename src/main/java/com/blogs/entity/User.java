package com.blogs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体类 - 支持多用户角色
 */
@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String nickname;

    @Column(length = 500)
    private String avatar;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 100)
    private String email;

    /**
     * 角色：ADMIN-超级管理员 USER-注册用户
     */
    @Column(nullable = false, length = 20)
    private String role;

    /**
     * 状态：0-禁用 1-启用
     */
    @Column(nullable = false)
    private Integer status = 1;

    @Column(length = 200)
    private String github;

    @Column(length = 200)
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
