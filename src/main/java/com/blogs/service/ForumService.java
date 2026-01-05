package com.blogs.service;

import com.blogs.common.PageResult;
import com.blogs.common.NotificationTypes;
import com.blogs.dto.ForumPostVO;
import com.blogs.entity.*;
import com.blogs.exception.BusinessException;
import com.blogs.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private LikeRecordRepository likeRecordRepository;

    @Autowired
    private NotificationService notificationService;

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

    public ForumPostVO getPost(Long id) {
        if (id == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        post.setViewCount(post.getViewCount() + 1);
        post = postRepository.save(post);

        // 查询用户信息
        User user = null;
        if (post.getUserId() != null) {
            user = userRepository.findById(post.getUserId()).orElse(null);
        }

        return ForumPostVO.fromEntity(post, user);
    }

    /**
     * 获取帖子（不增加阅读量）
     * 用途：个人中心/后台列表等“只读展示标题”等场景，避免误增 viewCount。
     */
    public ForumPost getPostWithoutIncrement(Long id) {
        if (id == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        return postRepository.findById(id)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
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

        // 统一评论表：将“回帖”写入 Comment（targetType=FORUM_POST）
        User user = userRepository.findById(comment.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Comment unified = new Comment();
        unified.setTargetType("FORUM_POST");
        unified.setTargetId(postId);
        unified.setUser(user);
        unified.setContent(comment.getContent());
        unified.setParentId(comment.getParentId());
        unified.setReplyToId(comment.getReplyToId());
        unified.setIpAddress(comment.getIpAddress());
        unified.setStatus(comment.getStatus() != null ? comment.getStatus() : 1);
        unified.setNickname(user.getNickname());
        unified.setAvatar(user.getAvatar());

        if (comment.getReplyToId() != null) {
            commentRepository.findById(comment.getReplyToId())
                    .ifPresent(replyTo -> unified.setReplyToNickname(replyTo.getNickname()));
        }

        Comment saved = commentRepository.save(unified);

        // 通知：回帖/回复
        sendPostCommentNotifications(saved, user, post);

        // 返回结构兼容旧前端：ForumPostComment 仍作为响应体
        ForumPostComment resp = new ForumPostComment();
        resp.setId(saved.getId());
        resp.setPostId(postId);
        resp.setUserId(user.getId());
        resp.setContent(saved.getContent());
        resp.setParentId(saved.getParentId());
        resp.setReplyToId(saved.getReplyToId());
        resp.setStatus(saved.getStatus());
        resp.setIpAddress(saved.getIpAddress());
        resp.setCreatedAt(saved.getCreatedAt());
        return resp;
    }

    public PageResult<ForumPostComment> getComments(Long postId, Integer page, Integer size) {
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByTargetTypeAndTargetIdAndStatus("FORUM_POST", postId, 1,
                pageable);

        List<ForumPostComment> list = commentPage.getContent().stream().map(c -> {
            ForumPostComment fc = new ForumPostComment();
            fc.setId(c.getId());
            fc.setPostId(postId);
            fc.setUserId(c.getUser() != null ? c.getUser().getId() : null);
            fc.setContent(c.getContent());
            fc.setParentId(c.getParentId());
            fc.setReplyToId(c.getReplyToId());
            fc.setStatus(c.getStatus());
            fc.setIpAddress(c.getIpAddress());
            fc.setCreatedAt(c.getCreatedAt());

            // 填充用户信息
            fc.setNickname(c.getNickname());
            fc.setAvatar(c.getAvatar());
            fc.setReplyToNickname(c.getReplyToNickname());

            // 如果 Comment 中的 nickname 为空（可能是旧数据），则尝试从 User 获取
            if (fc.getNickname() == null && c.getUser() != null) {
                fc.setNickname(c.getUser().getNickname());
                fc.setAvatar(c.getUser().getAvatar());
            }

            return fc;
        }).toList();

        return PageResult.of(list, commentPage.getTotalElements(), page, size);
    }

    /**
     * 删除回帖（仅允许删除自己的回帖）
     */
    public void deleteComment(Long commentId, Long userId) {
        if (commentId == null) {
            throw new BusinessException("评论ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        // 检查是否为评论作者
        Long commentUserId = comment.getUser() != null ? comment.getUser().getId() : null;
        if (!userId.equals(commentUserId)) {
            throw new BusinessException("无权删除他人评论");
        }

        // 更新帖子评论数
        if ("FORUM_POST".equals(comment.getTargetType()) && comment.getTargetId() != null) {
            postRepository.findById(comment.getTargetId()).ifPresent(post -> {
                post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
                postRepository.save(post);
            });
        }

        // 软删除评论
        comment.setStatus(0);
        commentRepository.save(comment);
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
            throw new BusinessException("您已点赞过该帖子");
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

        // 通知：帖子被点赞
        if (post.getUserId() != null && !post.getUserId().equals(userId)) {
            notificationService.sendNotification(
                    post.getUserId(),
                    userId,
                    NotificationTypes.FORUM_POST_LIKE,
                    "你的帖子被点赞",
                    "有人点赞了你的帖子",
                    postId,
                    "FORUM_POST");
        }
    }

    public void collectPost(Long userId, Long postId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        if (favoriteRepository.findByUserIdAndTargetIdAndType(userId, postId, "POST").isPresent()) {
            throw new BusinessException("您已收藏过该帖子");
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

    private void sendPostCommentNotifications(Comment saved, User sender, ForumPost post) {
        if (saved == null || sender == null || post == null) {
            return;
        }

        // 回复：通知被回复者
        if (saved.getReplyToId() != null) {
            commentRepository.findById(saved.getReplyToId()).ifPresent(replyTo -> {
                Long receiverId = replyTo.getUser() != null ? replyTo.getUser().getId() : null;
                if (receiverId != null && !receiverId.equals(sender.getId())) {
                    notificationService.sendNotification(
                            receiverId,
                            sender.getId(),
                            NotificationTypes.FORUM_POST_COMMENT_REPLY,
                            "有人回复了你",
                            preview(saved.getContent()),
                            saved.getId(),
                            "COMMENT");
                }
            });
        }

        // 回帖：通知帖子作者
        if (post.getUserId() != null && !post.getUserId().equals(sender.getId())) {
            notificationService.sendNotification(
                    post.getUserId(),
                    sender.getId(),
                    NotificationTypes.FORUM_POST_COMMENT,
                    "有人回帖了你",
                    preview(saved.getContent()),
                    post.getId(),
                    "FORUM_POST");
        }
    }

    private String preview(String content) {
        if (content == null) {
            return "";
        }
        String c = content.trim();
        return c.length() > 120 ? c.substring(0, 120) + "..." : c;
    }

    /**
     * 修复帖子评论数（重新计算）
     */
    public void fixPostCommentCounts() {
        List<ForumPost> posts = postRepository.findAll();
        for (ForumPost post : posts) {
            long count = commentRepository.countByTargetTypeAndTargetIdAndStatus("FORUM_POST", post.getId(), 1);
            if (!Objects.equals(post.getCommentCount(), count)) {
                post.setCommentCount(count);
                postRepository.save(post);
            }
        }
    }
}
