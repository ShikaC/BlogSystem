package com.blogs.service;

import com.blogs.common.PageResult;
import com.blogs.entity.*;
import com.blogs.exception.BusinessException;
import com.blogs.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.List;

/**
 * 论坛服务
 */
@Service
@Transactional
public class ForumService {
    
    @Autowired
    private ForumSectionRepository sectionRepository;
    
    @Autowired
    private ForumPostRepository postRepository;
    
    @Autowired
    private ForumPostCommentRepository commentRepository;
    
    @Autowired
    private FavoriteRepository favoriteRepository;
    
    @Autowired
    private LikeRecordRepository likeRecordRepository;

    // ==================== 板块相关 ====================

    public List<ForumSection> getAllSections() {
        return sectionRepository.findByStatusOrderBySortOrderAsc(1);
    }

    public List<ForumSection> getAdminSections() {
        return sectionRepository.findAllByOrderBySortOrderAsc();
    }

    public ForumSection saveSection(ForumSection section) {
        if (section == null) {
            throw new BusinessException("板块信息不能为空");
        }
        return sectionRepository.save(section);
    }

    public void deleteSection(Long id) {
        if (id == null) {
            throw new BusinessException("板块ID不能为空");
        }
        sectionRepository.deleteById(id);
    }

    // ==================== 帖子相关 ====================

    public ForumPost savePost(ForumPost post) {
        if (post == null) {
            throw new BusinessException("帖子信息不能为空");
        }
        if (post.getSectionId() == null) {
            throw new BusinessException("板块ID不能为空");
        }
        if (post.getUserId() == null) {
            throw new BusinessException("发布者ID不能为空");
        }
        if (post.getId() != null) {
            Long postId = Objects.requireNonNull(post.getId(), "帖子ID不能为空");
            ForumPost oldPost = postRepository.findById(postId)
                    .orElseThrow(() -> new BusinessException("帖子不存在"));
            if (!oldPost.getUserId().equals(post.getUserId())) {
                throw new BusinessException("无权修改他人帖子");
            }
        }
        return postRepository.save(post);
    }

    public PageResult<ForumPost> getPostList(Long sectionId, Integer page, Integer size) {
        if (sectionId == null) {
            throw new BusinessException("板块ID不能为空");
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ForumPost> postPage = postRepository.findBySectionIdAndStatus(sectionId, 1, pageable);
        return PageResult.of(postPage.getContent(), postPage.getTotalElements(), page, size);
    }

    public ForumPost getPost(Long id) {
        if (id == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        post.setViewCount(post.getViewCount() + 1);
        return postRepository.save(post);
    }

    public void deletePost(Long id, Long userId) {
        if (id == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人帖子");
        }
        post.setStatus(3); // 回收站
        postRepository.save(post);
    }

    // ==================== 回帖相关 ====================

    public ForumPostComment saveComment(ForumPostComment comment) {
        if (comment == null) {
            throw new BusinessException("回帖内容不能为空");
        }
        if (comment.getPostId() == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        if (comment.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        Long postId = Objects.requireNonNull(comment.getPostId(), "帖子ID不能为空");
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        return commentRepository.save(comment);
    }

    public PageResult<ForumPostComment> getComments(Long postId, Integer page, Integer size) {
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ForumPostComment> commentPage = commentRepository.findByPostIdAndStatus(postId, 1, pageable);
        return PageResult.of(commentPage.getContent(), commentPage.getTotalElements(), page, size);
    }

    // ==================== 点赞收藏 ====================

    public void likePost(Long userId, Long postId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        if (likeRecordRepository.findByUserIdAndTargetIdAndType(userId, postId, "POST").isPresent()) {
            return;
        }
        LikeRecord record = new LikeRecord();
        record.setUserId(userId);
        record.setTargetId(postId);
        record.setType("POST");
        likeRecordRepository.save(record);
        
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
    }

    public void collectPost(Long userId, Long postId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        if (favoriteRepository.findByUserIdAndTargetIdAndType(userId, postId, "POST").isPresent()) {
            return;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setTargetId(postId);
        favorite.setType("POST");
        favoriteRepository.save(favorite);
        
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        post.setCollectCount(post.getCollectCount() + 1);
        postRepository.save(post);
    }

    public PageResult<ForumPost> getMyPosts(Long userId, Integer page, Integer size) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ForumPost> postPage = postRepository.findByUserId(userId, pageable);
        return PageResult.of(postPage.getContent(), postPage.getTotalElements(), page, size);
    }
}

