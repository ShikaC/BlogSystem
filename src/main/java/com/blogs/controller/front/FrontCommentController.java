package com.blogs.controller.front;

import com.blogs.common.Result;
import com.blogs.dto.CommentRequest;
import com.blogs.dto.CommentVO;
import com.blogs.exception.BusinessException;

import com.blogs.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 前台评论控制器
 */
@RestController
@RequestMapping("/front/comments")
public class FrontCommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private com.blogs.service.UserService userService;

    // @Autowired
    // private CaptchaService captchaService;

    /**
     * 发表评论
     */
    @PostMapping
    public Result<CommentVO> createComment(@Valid @RequestBody CommentRequest request,
            HttpServletRequest httpRequest) {
        // 获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(username)) {
            throw new BusinessException("请先登录再发表评论");
        }

        // 移除验证码验证，用户登录后即可发表评论
        String ipAddress = getClientIp(httpRequest);
        CommentVO comment = commentService.createComment(request, username, ipAddress);
        return Result.success(comment);
    }

    /**
     * 删除评论（仅限用户删除自己的评论）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        // 获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(username)) {
            throw new BusinessException("请先登录");
        }

        commentService.deleteUserComment(id, username);
        return Result.success();
    }

    /**
     * 点赞评论
     */
    @PostMapping("/{id}/like")
    public Result<Void> likeComment(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(username)) {
            throw new BusinessException("请先登录");
        }
        com.blogs.dto.UserDTO user = userService.getUserInfo(username);
        commentService.likeComment(user.getId(), id);
        return Result.success();
    }

    /**
     * 取消点赞评论
     */
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeComment(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(username)) {
            throw new BusinessException("请先登录");
        }
        com.blogs.dto.UserDTO user = userService.getUserInfo(username);
        commentService.unlikeComment(user.getId(), id);
        return Result.success();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}