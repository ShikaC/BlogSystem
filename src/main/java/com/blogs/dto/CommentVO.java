package com.blogs.dto;

import com.blogs.entity.Comment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应DTO
 */
@Data
public class CommentVO {
    private Long id;
    private Long articleId;
    private Long targetId;
    private String targetType;
    private String content;
    private String nickname;
    private String email;
    private String website;
    private String avatar;
    private Long parentId;
    private Long replyToId;
    private String replyToNickname;
    private Boolean isBlogger;
    private Integer status;
    private LocalDateTime createdAt;
    private List<CommentVO> children;
    
    public static CommentVO fromEntity(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setTargetId(comment.getTargetId());
        vo.setTargetType(comment.getTargetType());
        // 兼容旧字段：文章评论仍可通过 articleId 获取
        vo.setArticleId(comment.getArticleId());
        vo.setContent(comment.getContent());
        vo.setNickname(comment.getNickname());
        vo.setEmail(comment.getEmail());
        vo.setWebsite(comment.getWebsite());
        vo.setAvatar(comment.getAvatar());
        vo.setParentId(comment.getParentId());
        vo.setReplyToId(comment.getReplyToId());
        vo.setReplyToNickname(comment.getReplyToNickname());
        vo.setIsBlogger(comment.getIsBlogger());
        vo.setStatus(comment.getStatus());
        vo.setCreatedAt(comment.getCreatedAt());
        return vo;
    }
}
