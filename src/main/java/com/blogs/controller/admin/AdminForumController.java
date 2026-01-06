package com.blogs.controller.admin;

import com.blogs.common.PageResult;
import com.blogs.common.Result;
import com.blogs.entity.ForumPost;
import com.blogs.entity.ForumSection;
import com.blogs.repository.ForumPostRepository;

import com.blogs.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台论坛管理控制器
 */
@RestController
@RequestMapping("/admin/forum")
public class AdminForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private com.blogs.service.NotificationService notificationService;

    @Autowired
    private com.blogs.service.UserService userService;

    // ==================== 板块管理 ====================

    /**
     * 获取所有板块
     */
    @GetMapping("/sections")
    public Result<List<ForumSection>> getSections() {
        return Result.success(forumService.getAdminSections());
    }

    /**
     * 创建/更新板块
     */
    @PostMapping("/sections")
    public Result<ForumSection> saveSection(@RequestBody ForumSection section) {
        return Result.success(forumService.saveSection(section));
    }

    /**
     * 删除板块
     */
    @DeleteMapping("/sections/{id}")
    public Result<Void> deleteSection(@PathVariable Long id) {
        forumService.deleteSection(id);
        return Result.success();
    }

    // ==================== 帖子管理 ====================

    /**
     * 获取帖子列表（管理员可查看所有状态）
     */
    @GetMapping("/posts")
    public Result<PageResult<ForumPost>> getPosts(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ForumPost> postPage;

        if (sectionId != null && status != null) {
            postPage = forumPostRepository.findBySectionIdAndStatus(sectionId, status, pageable);
        } else if (sectionId != null) {
            postPage = forumPostRepository.findBySectionId(sectionId, pageable);
        } else if (status != null) {
            postPage = forumPostRepository.findByStatus(status, pageable);
        } else {
            postPage = forumPostRepository.findAll(pageable);
        }

        return Result.success(PageResult.of(postPage.getContent(), postPage.getTotalElements(), page, size));
    }

    /**
     * 更新帖子状态
     */
    @PostMapping("/posts/{id}/status")
    public Result<Void> updatePostStatus(@PathVariable Long id, @RequestParam Integer status) {
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        post.setStatus(status);
        forumPostRepository.save(post);
        return Result.success();
    }

    /**
     * 审核帖子 - 通过
     */
    @PostMapping("/posts/{id}/approve")
    public Result<Void> approvePost(@PathVariable Long id) {
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        post.setStatus(1); // 已发布
        post.setRejectReason(null); // 清除驳回理由
        forumPostRepository.save(post);
        return Result.success();
    }

    /**
     * 审核帖子 - 驳回
     */
    @PostMapping("/posts/{id}/reject")
    public Result<Void> rejectPost(@PathVariable Long id, @RequestBody RejectRequest request) {
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        post.setStatus(2); // 已驳回
        post.setRejectReason(request.getReason());
        forumPostRepository.save(post);
        return Result.success();
    }

    /**
     * 获取待审核帖子列表（按发布时间排序）
     */
    @GetMapping("/posts/pending")
    public Result<PageResult<ForumPost>> getPendingPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, 
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "createdAt"));
        Page<ForumPost> postPage = forumPostRepository.findByStatus(0, pageable);
        return Result.success(PageResult.of(postPage.getContent(), postPage.getTotalElements(), page, size));
    }

    /**
     * 驳回请求DTO
     */
    public static class RejectRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /**
     * 警告用户（发送系统消息）
     */
    @PostMapping("/posts/{id}/warn")
    public Result<Void> warnUser(@PathVariable Long id, @RequestBody WarnRequest request) {
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        
        if (post.getUserId() != null) {
            notificationService.sendNotification(
                post.getUserId(),
                null, // 系统发送
                com.blogs.common.NotificationTypes.SYSTEM_WARNING,
                "内容违规警告",
                request.getMessage() != null ? request.getMessage() : "您发布的内容存在违规行为，请遵守社区规范。",
                post.getId(),
                "FORUM_POST"
            );
        }
        
        return Result.success();
    }

    /**
     * 禁用用户账号
     */
    @PostMapping("/posts/{id}/disable-user")
    public Result<Void> disableUser(@PathVariable Long id) {
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        
        if (post.getUserId() != null) {
            userService.updateUserStatus(post.getUserId(), 0); // 0=禁用
            
            // 发送通知
            notificationService.sendNotification(
                post.getUserId(),
                null,
                com.blogs.common.NotificationTypes.SYSTEM_NOTICE,
                "账号已被禁用",
                "由于发布违规内容，您的账号已被管理员禁用。如有疑问，请联系管理员。",
                null,
                null
            );
        }
        
        return Result.success();
    }

    /**
     * 警告请求DTO
     */
    public static class WarnRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * 删除帖子（移到回收站）
     */
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        post.setStatus(3); // 回收站
        forumPostRepository.save(post);
        return Result.success();
    }

    /**
     * 彻底删除帖子
     */
    @DeleteMapping("/posts/{id}/permanent")
    public Result<Void> permanentDeletePost(@PathVariable Long id) {
        forumPostRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 恢复帖子
     */
    @PostMapping("/posts/{id}/restore")
    public Result<Void> restorePost(@PathVariable Long id) {
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        post.setStatus(1); // 正常
        forumPostRepository.save(post);
        return Result.success();
    }

    /**
     * 批量删除帖子
     */
    @DeleteMapping("/posts/batch")
    public Result<Void> batchDeletePosts(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            ForumPost post = forumPostRepository.findById(id).orElse(null);
            if (post != null) {
                post.setStatus(3);
                forumPostRepository.save(post);
            }
        }
        return Result.success();
    }

    /**
     * 修复评论数（当数据不一致时使用）
     */
    @PostMapping("/fix-counts")
    public Result<Void> fixCounts() {
        forumService.fixPostCommentCounts();
        return Result.success();
    }
}
