package com.blogs.controller.admin;

import com.blogs.common.PageResult;
import com.blogs.common.Result;
import com.blogs.dto.ArticleRequest;
import com.blogs.dto.ArticleVO;
import com.blogs.entity.Article;
import com.blogs.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台文章管理控制器
 */
@RestController
@RequestMapping("/admin/articles")
public class AdminArticleController {
    
    @Autowired
    private ArticleService articleService;
    
    /**
     * 获取文章列表
     */
    @GetMapping
    public Result<PageResult<ArticleVO>> getArticleList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ArticleVO> result = articleService.getAdminArticleList(status, page, size);
        return Result.success(result);
    }
    
    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticle(@PathVariable Long id) {
        ArticleVO article = articleService.getArticle(id);
        return Result.success(article);
    }
    
    /**
     * 保存文章（新增/编辑）
     */
    @PostMapping
    public Result<ArticleVO> saveArticle(@Valid @RequestBody ArticleRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Article article = articleService.saveArticle(request, username);
        return Result.success(ArticleVO.fromEntity(article));
    }
    
    /**
     * 移到回收站
     */
    @DeleteMapping("/{id}")
    public Result<Void> moveToTrash(@PathVariable Long id) {
        articleService.moveToTrash(id);
        return Result.success();
    }
    
    /**
     * 批量移到回收站
     */
    @DeleteMapping("/batch")
    public Result<Void> batchMoveToTrash(@RequestBody List<Long> ids) {
        articleService.batchMoveToTrash(ids);
        return Result.success();
    }
    
    /**
     * 恢复文章
     */
    @PostMapping("/{id}/restore")
    public Result<Void> restoreFromTrash(@PathVariable Long id) {
        articleService.restoreFromTrash(id);
        return Result.success();
    }
    
    /**
     * 彻底删除
     */
    @DeleteMapping("/{id}/permanent")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }
    
    /**
     * 置顶/取消置顶
     */
    @PostMapping("/{id}/toggle-top")
    public Result<Void> toggleTop(@PathVariable Long id) {
        articleService.toggleTop(id);
        return Result.success();
    }
    
    /**
     * 批量置顶
     */
    @PostMapping("/batch-top")
    public Result<Void> batchTop(@RequestBody List<Long> ids, @RequestParam Boolean isTop) {
        articleService.batchTop(ids, isTop);
        return Result.success();
    }
}
