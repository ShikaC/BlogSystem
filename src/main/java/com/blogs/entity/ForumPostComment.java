package com.blogs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 论坛回帖实体类
 */
@Data
@Entity
@Table(name = "forum_post_comment")
public class ForumPostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "reply_to_id")
    private Long replyToId;

    /**
     * 状态: 0-待审核 1-已通过 2-已拒绝
     */
    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private String nickname;

    @Transient
    private String avatar;

    @Transient
    private String replyToNickname;
}
