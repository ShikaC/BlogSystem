package com.blogs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 文章实体类
 */
@Data
@Entity
@Table(name = "article")
public class Article {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    
    @Column(length = 500)
    private String summary;
    
    @Column(name = "cover_image", length = 500)
    private String coverImage;
    
    /**
     * 文章状态：0-草稿 1-已发布 2-私密 3-回收站
     */
    @Column(nullable = false)
    private Integer status = 0;
    
    @Column(name = "is_top")
    private Boolean isTop = false;
    
    @Column(name = "view_count")
    private Long viewCount = 0L;
    
    @Column(name = "like_count")
    private Long likeCount = 0L;
    
    @Column(name = "collect_count")
    private Long collectCount = 0L;
    
    @Column(name = "comment_count")
    private Long commentCount = 0L;
    
    @Column(name = "word_count")
    private Integer wordCount = 0;
    
    // SEO字段
    @Column(name = "seo_title", length = 200)
    private String seoTitle;
    
    @Column(name = "seo_keywords", length = 200)
    private String seoKeywords;
    
    @Column(name = "seo_description", length = 500)
    private String seoDescription;
    
    // 访问密码（私密文章可设置）
    @Column(length = 50)
    private String password;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "article_tag",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
