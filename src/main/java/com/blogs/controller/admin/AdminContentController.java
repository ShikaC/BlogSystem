package com.blogs.controller.admin;

import com.blogs.common.PageResult;
import com.blogs.common.Result;
import com.blogs.dto.ContentAdminVO;
import com.blogs.service.ContentManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 后台全站内容管理控制器（文章 + 帖子统一入口）
 *
 * 说明：不删除原有 AdminArticleController，只新增统一入口，便于一体化管理。
 */
@RestController
@RequestMapping("/admin/content")
public class AdminContentController {

    @Autowired
    private ContentManageService contentManageService;

    /**
     * 内容列表（支持 contentType=ARTICLE/FORUM_POST 筛选）
     */
    @GetMapping
    public Result<PageResult<ContentAdminVO>> list(
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(contentManageService.getContentList(contentType, status, page, size));
    }

    /**
     * 审核/状态更新（复用同一接口）
     */
    @PostMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam String contentType,
            @RequestParam Integer status) {
        contentManageService.updateStatus(contentType, id, status);
        return Result.success();
    }

    /**
     * 置顶/取消置顶（文章/帖子通用）
     */
    @PostMapping("/{id}/top")
    public Result<Void> updateTop(
            @PathVariable Long id,
            @RequestParam String contentType,
            @RequestParam Boolean isTop) {
        contentManageService.updateTop(contentType, id, isTop);
        return Result.success();
    }

    /**
     * 加精/取消加精（仅帖子）
     */
    @PostMapping("/posts/{id}/essence")
    public Result<Void> updateEssence(@PathVariable Long id, @RequestParam Boolean isEssence) {
        contentManageService.updateEssence(id, isEssence);
        return Result.success();
    }
}


