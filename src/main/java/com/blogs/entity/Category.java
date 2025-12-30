package com.blogs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 分类实体类
 */
@Data
@Entity
@Table(name = "category")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 分类类型：
     * - CATEGORY：博客分类
     * - SECTION：论坛版块
     *
     * 说明：为保证增量兼容，历史数据可能为 null，业务层应默认视为 CATEGORY。
     */
    @Column(length = 20)
    private String type = "CATEGORY";
    
    @Column(length = 200)
    private String description;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "article_count")
    private Integer articleCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
