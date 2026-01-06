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

    /**
     * 驳回理由（审核不通过时填写）
     */
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    public ForumPost() {
        // 新帖子默认状态为待审核（0）
        this.setStatus(0);
    }
}

