package com.blogs.dto;

import com.blogs.entity.ForumPost;
import com.blogs.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论坛帖子响应DTO
 */
@Data
public class ForumPostVO {
    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Long sectionId;
    private String title;
    private String content;
    private Integer status;
    private Boolean isTop;
    private Boolean isEssence;
    private Long viewCount;
    private Long likeCount;
    private Long collectCount;
    private Long commentCount;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ForumPostVO fromEntity(ForumPost post, User user) {
        ForumPostVO vo = new ForumPostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }
        vo.setSectionId(post.getSectionId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setStatus(post.getStatus());
        vo.setIsTop(post.getIsTop());
        vo.setIsEssence(post.getIsEssence());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCollectCount(post.getCollectCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setRejectReason(post.getRejectReason());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());
        return vo;
    }
}
