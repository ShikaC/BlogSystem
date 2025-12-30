package com.blogs.controller.front;

import com.blogs.common.PageResult;
import com.blogs.common.Result;
import com.blogs.dto.ArticleVO;
import com.blogs.dto.UserDTO;
import com.blogs.entity.ForumPost;
import com.blogs.entity.Notification;
import com.blogs.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 前台个人中心控制器 - 所有注册用户通用
 */
@RestController
@RequestMapping("/front/user")
public class UserCenterController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取个人信息
     */
    @GetMapping("/profile")
    public Result<UserDTO> getProfile() {
        String username = getCurrentUsername();
        return Result.success(userService.getUserInfo(username));
    }

    /**
     * 获取我的文章
     */
    @GetMapping("/articles")
    public Result<PageResult<ArticleVO>> getMyArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String username = getCurrentUsername();
        return Result.success(articleService.getUserArticleList(username, page, size));
    }

    /**
     * 获取我的帖子
     */
    @GetMapping("/posts")
    public Result<PageResult<ForumPost>> getMyPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);
        return Result.success(forumService.getMyPosts(user.getId(), page, size));
    }

    /**
     * 获取我的消息
     */
    @GetMapping("/notifications")
    public Result<PageResult<Notification>> getMyNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);
        return Result.success(notificationService.getNotifications(user.getId(), page, size));
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

