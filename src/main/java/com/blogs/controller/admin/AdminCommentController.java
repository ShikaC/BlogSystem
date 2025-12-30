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
    
    /**
     * 获取评论列表
     */
    @GetMapping
    public Result<PageResult<CommentVO>> getComments(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<CommentVO> result = commentService.getAdminComments(status, page, size);
        return Result.success(result);
    }
    
    /**
     * 博主回复评论
     */
    @PostMapping("/reply")
    public Result<CommentVO> reply(@RequestParam Long articleId,
                                   @RequestParam Long parentId,
                                   @RequestParam(required = false) Long replyToId,
                                   @RequestParam String content) {
        CommentVO comment = commentService.bloggerReply(articleId, parentId, replyToId, content);
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
}
