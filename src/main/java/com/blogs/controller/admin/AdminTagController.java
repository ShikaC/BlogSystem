package com.blogs.controller.admin;

import com.blogs.common.Result;
import com.blogs.entity.Tag;
import com.blogs.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台标签管理控制器
 */
@RestController
@RequestMapping("/admin/tags")
public class AdminTagController {
    
    @Autowired
    private TagService tagService;
    
    /**
     * 获取所有标签
     */
    @GetMapping
    public Result<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return Result.success(tags);
    }
    
    /**
     * 获取标签详情
     */
    @GetMapping("/{id}")
    public Result<Tag> getTag(@PathVariable Long id) {
        Tag tag = tagService.getTag(id);
        return Result.success(tag);
    }
    
    /**
     * 创建标签
     */
    @PostMapping
    public Result<Tag> createTag(@RequestParam String name) {
        Tag tag = tagService.createTag(name);
        return Result.success(tag);
    }
    
    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    public Result<Tag> updateTag(@PathVariable Long id, @RequestParam String name) {
        Tag tag = tagService.updateTag(id, name);
        return Result.success(tag);
    }
    
    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
