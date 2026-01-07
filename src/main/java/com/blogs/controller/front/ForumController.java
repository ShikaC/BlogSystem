package com.blogs.controller.front;

import com.blogs.common.PageResult;
import com.blogs.common.Result;
import com.blogs.dto.ForumPostVO;
import com.blogs.entity.ForumPostComment;
import com.blogs.entity.ForumSection;
import com.blogs.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台论坛展示控制器
 */
@RestController
@RequestMapping("/front/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    /**
     * 获取所有板块
     */
    @GetMapping("/sections")
    public Result<List<ForumSection>> getSections() {
        return Result.success(forumService.getAllSections());
    }

    /**
     * 获取板块下的帖子列表
     */
    @GetMapping("/posts")
    public Result<PageResult<ForumPostVO>> getPosts(
            @RequestParam Long sectionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(forumService.getPostList(sectionId, page, size));
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/posts/{id}")
    public Result<ForumPostVO> getPost(@PathVariable Long id) {
        return Result.success(forumService.getPost(id));
    }

    /**
     * 获取帖子评论列表
     */
    @GetMapping("/posts/{id}/comments")
    public Result<PageResult<ForumPostComment>> getPostComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "100") Integer size) {
        return Result.success(forumService.getComments(id, page, size));
    }
}
