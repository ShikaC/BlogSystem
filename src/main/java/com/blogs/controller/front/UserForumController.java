package com.blogs.controller.front;

import com.blogs.common.Result;
import com.blogs.entity.ForumPost;
import com.blogs.entity.ForumPostComment;
import com.blogs.service.ForumService;
import com.blogs.service.UserService;
import com.blogs.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 前台用户论坛互动控制器
 */
@RestController
@RequestMapping("/front/user/forum")
public class UserForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

    /**
     * 发布/更新帖子
     */
    @PostMapping("/posts")
    public Result<ForumPost> savePost(@RequestBody ForumPost post) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);
        post.setUserId(user.getId());
        return Result.success(forumService.savePost(post));
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);
        forumService.deletePost(id, user.getId());
        return Result.success();
    }

    /**
     * 回帖
     */
    @PostMapping("/comments")
    public Result<ForumPostComment> createComment(@RequestBody ForumPostComment comment) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);
        comment.setUserId(user.getId());
        return Result.success(forumService.saveComment(comment));
    }

    /**
     * 删除自己的回帖
     */
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);
        forumService.deleteComment(id, user.getId());
        return Result.success();
    }

    /**
     * 点赞帖子
     */
    @PostMapping("/posts/{id}/like")
    public Result<Void> likePost(@PathVariable Long id) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);
        forumService.likePost(user.getId(), id);
        return Result.success();
    }

    /**
     * 收藏帖子
     */
    @PostMapping("/posts/{id}/collect")
    public Result<Void> collectPost(@PathVariable Long id) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);
        forumService.collectPost(user.getId(), id);
        return Result.success();
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
