package com.blogs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 论坛帖子实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "forum_post")
public class ForumPost extends BaseContent {
    
    @Column(name = "section_id", nullable = false)
    private Long sectionId;
    
    @Column(name = "is_essence")
    private Boolean isEssence = false;

    public ForumPost() {
        // 兼容原有默认值：论坛帖子默认已发布
        this.setStatus(1);
    }
}

