package com.blogs.controller.admin;

import com.blogs.common.PageResult;
import com.blogs.common.Result;
import com.blogs.dto.CommentVO;
import com.blogs.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台评论管理控制器
 */
@RestController
@RequestMapping("/admin/comments")
public class AdminCommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private com.blogs.service.NotificationService notificationService;

    @Autowired
    private com.blogs.service.UserService userService;

    /**
     * 获取评论列表
     * 
     * @param targetType 评论目标类型：ARTICLE（文章）/ FORUM_POST（论坛帖子），为空时返回所有
     * @param status     评论状态
     * @param page       页码
     * @param size       每页数量
     */
    @GetMapping
    public Result<PageResult<CommentVO>> getComments(
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<CommentVO> commentPage = commentService.getAdminComments(targetType, targetId, status, page, size);
        return Result.success(commentPage);
    }

    /**
     * 博主回复评论
     */
    @PostMapping("/reply")
    public Result<CommentVO> reply(@RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam Long parentId,
            @RequestParam(required = false) Long replyToId,
            @RequestParam String content) {
        CommentVO comment = commentService.bloggerReply(targetType, targetId, parentId, replyToId, content);
        return Result.success(comment);
    }

    /**
     * 审核评论
     */
    @PostMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        commentService.updateCommentStatus(id, status);
        return Result.success();
    }

    /**
     * 批量审核
     */
    @PostMapping("/batch-status")
    public Result<Void> batchUpdateStatus(@RequestBody List<Long> ids, @RequestParam Integer status) {
        commentService.batchUpdateStatus(ids, status);
        return Result.success();
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        commentService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 警告用户（发送系统消息）
     */
    @PostMapping("/{id}/warn")
    public Result<Void> warnUser(@PathVariable Long id, @RequestBody WarnRequest request) {
        com.blogs.entity.Comment comment = commentService.getCommentById(id)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        if (comment.getUser() != null && comment.getUser().getId() != null) {
            notificationService.sendNotification(
                comment.getUser().getId(),
                null, // 系统发送
                com.blogs.common.NotificationTypes.SYSTEM_WARNING,
                "内容违规警告",
                request.getMessage() != null ? request.getMessage() : "您发布的评论存在违规行为，请遵守社区规范。",
                comment.getId(),
                "COMMENT"
            );
        }
        
        return Result.success();
    }

    /**
     * 禁用用户账号
     */
    @PostMapping("/{id}/disable-user")
    public Result<Void> disableUser(@PathVariable Long id) {
        com.blogs.entity.Comment comment = commentService.getCommentById(id)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        if (comment.getUser() != null && comment.getUser().getId() != null) {
            userService.updateUserStatus(comment.getUser().getId(), 0); // 0=禁用
            
            // 发送通知
            notificationService.sendNotification(
                comment.getUser().getId(),
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
}
