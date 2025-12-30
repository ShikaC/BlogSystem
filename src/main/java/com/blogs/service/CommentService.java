package com.blogs.service;

import com.blogs.common.PageResult;
import com.blogs.dto.CommentRequest;
import com.blogs.dto.CommentVO;
import com.blogs.entity.Comment;
import com.blogs.exception.BusinessException;
import com.blogs.repository.ArticleRepository;
import com.blogs.repository.CommentRepository;
import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务
 */
@Service
@Transactional
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private ArticleRepository articleRepository;
    
    /**
     * 访客发表评论
     */
    public CommentVO createComment(CommentRequest request, String ipAddress) {
        Comment comment = new Comment();
        comment.setArticleId(request.getArticleId());
        comment.setContent(request.getContent());
        comment.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : "匿名访客");
        comment.setEmail(request.getEmail());
        comment.setWebsite(request.getWebsite());
        comment.setIpAddress(ipAddress);
        comment.setIsBlogger(false);
        comment.setStatus(1); // 默认通过
        
        // Gravatar头像
        if (StringUtils.hasText(request.getEmail())) {
            String hash = DigestUtil.md5Hex(request.getEmail().toLowerCase().trim());
            comment.setAvatar("https://www.gravatar.com/avatar/" + hash + "?d=mp");
        }
        
        // 处理回复
        if (request.getParentId() != null) {
            comment.setParentId(request.getParentId());
        }
        if (request.getReplyToId() != null) {
            Comment replyTo = commentRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> new BusinessException("回复的评论不存在"));
            comment.setReplyToId(request.getReplyToId());
            comment.setReplyToNickname(replyTo.getNickname());
        }
        
        comment = commentRepository.save(comment);
        
        // 更新文章评论数
        articleRepository.updateCommentCount(request.getArticleId(), 1);
        
        return CommentVO.fromEntity(comment);
    }
    
    /**
     * 博主回复评论
     */
    public CommentVO bloggerReply(Long articleId, Long parentId, Long replyToId, String content) {
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setContent(content);
        comment.setNickname("博主");
        comment.setIsBlogger(true);
        comment.setStatus(1);
        comment.setParentId(parentId);
        
        if (replyToId != null) {
            Comment replyTo = commentRepository.findById(replyToId)
                    .orElseThrow(() -> new BusinessException("回复的评论不存在"));
            comment.setReplyToId(replyToId);
            comment.setReplyToNickname(replyTo.getNickname());
        }
        
        comment = commentRepository.save(comment);
        
        // 更新文章评论数
        articleRepository.updateCommentCount(articleId, 1);
        
        return CommentVO.fromEntity(comment);
    }
    
    /**
     * 获取文章评论（树形结构）
     */
    public List<CommentVO> getArticleComments(Long articleId) {
        List<Comment> comments = commentRepository.findByArticleIdAndStatusOrderByCreatedAtAsc(articleId, 1);
        List<CommentVO> commentVOs = comments.stream()
                .map(CommentVO::fromEntity)
                .collect(Collectors.toList());
        
        // 构建树形结构
        Map<Long, CommentVO> commentMap = new HashMap<>();
        List<CommentVO> rootComments = new ArrayList<>();
        
        for (CommentVO vo : commentVOs) {
            commentMap.put(vo.getId(), vo);
            vo.setChildren(new ArrayList<>());
        }
        
        for (CommentVO vo : commentVOs) {
            if (vo.getParentId() == null) {
                rootComments.add(vo);
            } else {
                CommentVO parent = commentMap.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    rootComments.add(vo);
                }
            }
        }
        
        return rootComments;
    }
    
    /**
     * 后台评论列表
     */
    public PageResult<CommentVO> getAdminComments(Integer status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Comment> commentPage;
        
        if (status == null) {
            commentPage = commentRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            commentPage = commentRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
        
        List<CommentVO> list = commentPage.getContent().stream()
                .map(CommentVO::fromEntity)
                .collect(Collectors.toList());
        
        return PageResult.of(list, commentPage.getTotalElements(), page, size);
    }
    
    /**
     * 审核评论
     */
    public void updateCommentStatus(Long id, Integer status) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("评论不存在"));
        
        int oldStatus = comment.getStatus();
        comment.setStatus(status);
        commentRepository.save(comment);
        
        // 更新文章评论数
        if (oldStatus == 1 && status != 1) {
            articleRepository.updateCommentCount(comment.getArticleId(), -1);
        } else if (oldStatus != 1 && status == 1) {
            articleRepository.updateCommentCount(comment.getArticleId(), 1);
        }
    }
    
    /**
     * 批量审核
     */
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        for (Long id : ids) {
            updateCommentStatus(id, status);
        }
    }
    
    /**
     * 删除评论
     */
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("评论不存在"));
        
        if (comment.getStatus() == 1) {
            articleRepository.updateCommentCount(comment.getArticleId(), -1);
        }
        
        commentRepository.delete(comment);
    }
    
    /**
     * 批量删除
     */
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            deleteComment(id);
        }
    }
    
    /**
     * 最新评论
     */
    public List<CommentVO> getLatestComments() {
        List<Comment> comments = commentRepository.findTop10ByStatusOrderByCreatedAtDesc(1);
        return comments.stream()
                .map(CommentVO::fromEntity)
                .collect(Collectors.toList());
    }
}
