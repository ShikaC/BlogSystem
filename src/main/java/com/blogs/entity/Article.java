package com.blogs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 文章实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "article")
public class Article extends BaseContent {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private User user;

    @Column(length = 500)
    private String summary;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

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
    @Column(length = 100)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "article_tag", joinColumns = @JoinColumn(name = "article_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * 兼容字段：直接拿到分类ID（不改变原有 Category 关联用法）
     */
    @Transient
    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }
}
