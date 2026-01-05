package com.blogs.service;

import com.blogs.common.PageResult;
import com.blogs.common.NotificationTypes;
import com.blogs.dto.CommentRequest;
import com.blogs.dto.CommentVO;
import com.blogs.entity.Comment;
import com.blogs.entity.User;
import com.blogs.exception.BusinessException;
import com.blogs.repository.ArticleRepository;
import com.blogs.repository.CommentRepository;
import com.blogs.repository.ForumPostRepository;
import com.blogs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * 发表评论 (需要登录)
     */
    @SuppressWarnings("deprecation")
    public CommentVO createComment(CommentRequest request, String username, String ipAddress) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 兼容旧参数：articleId -> (ARTICLE, targetId)
        Long targetId = request.getTargetId() != null ? request.getTargetId() : request.getArticleId();
        String targetType = request.getTargetType();
        if (targetType == null || targetType.isBlank()) {
            targetType = request.getTargetId() != null ? null : "ARTICLE";
        }
        if (targetId == null) {
            throw new BusinessException("评论目标ID不能为空");
        }
        if (targetType == null || targetType.isBlank()) {
            throw new BusinessException("评论目标类型不能为空");
        }
        targetType = targetType.trim().toUpperCase(Locale.ROOT);
        if (!"ARTICLE".equals(targetType) && !"FORUM_POST".equals(targetType)) {
            throw new BusinessException("评论目标类型不合法");
        }

        Comment comment = new Comment();
        comment.setTargetId(targetId);
        comment.setTargetType(targetType);
        // 兼容旧数据：文章评论同步写入 legacyArticleId，确保不改动既有依赖时也能读到
        if ("ARTICLE".equals(targetType)) {
            comment.setLegacyArticleId(targetId);
        }
        comment.setUser(user);
        comment.setContent(request.getContent());
        comment.setIpAddress(ipAddress);
        comment.setIsBlogger("ADMIN".equals(user.getRole()));
        comment.setStatus(1); // 默认通过，实际可根据配置开启审核

        // 兼容原有的昵称和头像 (从用户信息中获取)
        comment.setNickname(user.getNickname());
        comment.setAvatar(user.getAvatar());

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

        // 更新目标内容评论/回帖数
        updateTargetCommentCount(targetType, targetId, 1);

        // 发送通知（评论/回帖 + 回复）
        sendCommentNotifications(comment, user);

        return CommentVO.fromEntity(comment);
    }

    /**
     * 用户删除自己的评论
     */
    public void deleteUserComment(Long id, String username) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查评论是否属于当前用户
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权删除此评论");
        }

        String targetType = normalizeTargetTypeForExisting(comment);
        Long targetId = resolveTargetIdForExisting(comment);
        if (comment.getStatus() == 1 && targetType != null && targetId != null) {
            updateTargetCommentCount(targetType, targetId, -1);
        }

        commentRepository.delete(comment);
    }

    /**
     * 博主回复评论
     */
    @SuppressWarnings("deprecation")
    public CommentVO bloggerReply(String targetType, Long targetId, Long parentId, Long replyToId, String content) {
        if (targetType == null || targetId == null) {
            throw new BusinessException("回复目标不能为空");
        }

        Comment comment = new Comment();
        comment.setTargetId(targetId);
        comment.setTargetType(targetType);
        if ("ARTICLE".equalsIgnoreCase(targetType)) {
            comment.setLegacyArticleId(targetId);
        }
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

        // 更新评论数
        updateTargetCommentCount(targetType, targetId, 1);

        // 发送通知
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        if (adminUser != null) {
            sendCommentNotifications(comment, adminUser);
        }

        return CommentVO.fromEntity(comment);
    }

    /**
     * 获取文章评论（树形结构）
     */
    public List<CommentVO> getArticleComments(Long articleId) {
        List<Comment> comments = commentRepository.findArticleCommentsCompatible(articleId, 1);
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
     * 
     * @param targetType 评论目标类型：ARTICLE（文章）/ FORUM_POST（论坛帖子），为null时返回所有
     * @param targetId   目标ID（文章ID或帖子ID），选填
     * @param status     评论状态
     * @param page       页码
     * @param size       每页数量
     */
    public PageResult<CommentVO> getAdminComments(String targetType, Long targetId, Integer status, Integer page,
            Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Comment> commentPage;

        if (targetType != null && !targetType.isBlank()) {
            String normalizedType = targetType.trim().toUpperCase(java.util.Locale.ROOT);
            if (targetId != null) {
                // 有类型且有ID
                if (status == null) {
                    commentPage = commentRepository.findByTargetTypeAndTargetIdCompatible(normalizedType, targetId,
                            pageable);
                } else {
                    commentPage = commentRepository.findByTargetTypeAndTargetIdAndStatusCompatible(normalizedType,
                            targetId, status, pageable);
                }
            } else {
                // 只有类型
                if (status == null) {
                    commentPage = commentRepository.findByTargetTypeCompatible(normalizedType, pageable);
                } else {
                    commentPage = commentRepository.findByTargetTypeAndStatusCompatible(normalizedType, status,
                            pageable);
                }
            }
        } else {
            // 没有类型 (targetId 在这里暂不生效，通常按类型筛选是基础)
            if (status == null) {
                commentPage = commentRepository.findAllByOrderByCreatedAtDesc(pageable);
            } else {
                commentPage = commentRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
            }
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

        // 更新目标内容评论/回帖数
        String targetType = normalizeTargetTypeForExisting(comment);
        Long targetId = resolveTargetIdForExisting(comment);
        if (targetType != null && targetId != null) {
            if (oldStatus == 1 && status != 1) {
                updateTargetCommentCount(targetType, targetId, -1);
            } else if (oldStatus != 1 && status == 1) {
                updateTargetCommentCount(targetType, targetId, 1);
            }
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
     * 删除评论（管理员）
     */
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        String targetType = normalizeTargetTypeForExisting(comment);
        Long targetId = resolveTargetIdForExisting(comment);
        if (comment.getStatus() == 1 && targetType != null && targetId != null) {
            updateTargetCommentCount(targetType, targetId, -1);
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

    private void updateTargetCommentCount(String targetType, Long targetId, int delta) {
        if ("ARTICLE".equals(targetType)) {
            articleRepository.updateCommentCount(targetId, delta);
        } else if ("FORUM_POST".equals(targetType)) {
            forumPostRepository.updateCommentCount(targetId, delta);
        }
    }

    private void sendCommentNotifications(Comment comment, User sender) {
        String targetType = normalizeTargetTypeForExisting(comment);
        Long targetId = resolveTargetIdForExisting(comment);
        if (targetType == null || targetId == null || sender == null) {
            return;
        }

        // 回复通知优先：通知被回复者
        if (comment.getReplyToId() != null) {
            commentRepository.findById(comment.getReplyToId()).ifPresent(replyTo -> {
                Long receiverId = replyTo.getUser() != null ? replyTo.getUser().getId() : null;
                if (receiverId != null && !receiverId.equals(sender.getId())) {
                    String type = "ARTICLE".equals(targetType)
                            ? NotificationTypes.ARTICLE_COMMENT_REPLY
                            : NotificationTypes.FORUM_POST_COMMENT_REPLY;
                    notificationService.sendNotification(
                            receiverId,
                            sender.getId(),
                            type,
                            "有人回复了你",
                            safePreview(comment.getContent()),
                            comment.getId(),
                            "COMMENT");
                }
            });
        }

        // 评论/回帖通知：通知内容发布者
        Long ownerId = null;
        if ("ARTICLE".equals(targetType)) {
            ownerId = articleRepository.findById(targetId)
                    .map(a -> a.getUserId() != null ? a.getUserId()
                            : (a.getUser() != null ? a.getUser().getId() : null))
                    .orElse(null);
        } else if ("FORUM_POST".equals(targetType)) {
            ownerId = forumPostRepository.findById(targetId)
                    .map(p -> p.getUserId())
                    .orElse(null);
        }

        if (ownerId != null && !ownerId.equals(sender.getId())) {
            String type = "ARTICLE".equals(targetType)
                    ? NotificationTypes.ARTICLE_COMMENT
                    : NotificationTypes.FORUM_POST_COMMENT;
            notificationService.sendNotification(
                    ownerId,
                    sender.getId(),
                    type,
                    "有人评论了你",
                    safePreview(comment.getContent()),
                    targetId,
                    targetType);
        }
    }

    private String safePreview(String content) {
        if (content == null) {
            return "";
        }
        String c = content.trim();
        return c.length() > 120 ? c.substring(0, 120) + "..." : c;
    }

    /**
     * 兼容旧数据：targetType 为空时视为 ARTICLE
     */
    private String normalizeTargetTypeForExisting(Comment comment) {
        if (comment.getTargetType() == null || comment.getTargetType().isBlank()) {
            return "ARTICLE";
        }
        return comment.getTargetType().trim().toUpperCase(Locale.ROOT);
    }

    @SuppressWarnings("deprecation")
    private Long resolveTargetIdForExisting(Comment comment) {
        if (comment.getTargetId() != null) {
            return comment.getTargetId();
        }
        return comment.getLegacyArticleId();
    }
}