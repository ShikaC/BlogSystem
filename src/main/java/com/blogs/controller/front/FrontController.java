package com.blogs.controller.front;

import com.blogs.common.PageResult;
import com.blogs.common.Result;
import com.blogs.dto.ArchiveDTO;
import com.blogs.dto.ArticleVO;
import com.blogs.dto.UserDTO;
import com.blogs.dto.CommentVO;
import com.blogs.entity.Category;
import com.blogs.entity.FriendLink;
import com.blogs.entity.Tag;
import com.blogs.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 前台展示控制器
 */
@RestController
@RequestMapping("/front")
public class FrontController {
    
    @Autowired
    private ArticleService articleService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private FriendLinkService friendLinkService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SiteConfigService siteConfigService;
    
    // ==================== 文章相关 ====================
    
    /**
     * 首页文章列表
     */
    @GetMapping("/articles")
    public Result<PageResult<ArticleVO>> getArticleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ArticleVO> result = articleService.getPublishedArticleList(page, size);
        return Result.success(result);
    }
    
    /**
     * 文章详情
     */
    @GetMapping("/articles/{id}")
    public Result<ArticleVO> getArticle(@PathVariable Long id) {
        ArticleVO article = articleService.getArticle(id);
        // 增加阅读量
        articleService.incrementViewCount(id);
        return Result.success(article);
    }
    
    /**
     * 验证文章密码
     */
    @PostMapping("/articles/{id}/verify-password")
    public Result<Boolean> verifyPassword(@PathVariable Long id, @RequestParam String password) {
        boolean valid = articleService.verifyPassword(id, password);
        return Result.success(valid);
    }
    
    /**
     * 按分类获取文章
     */
    @GetMapping("/articles/category/{categoryId}")
    public Result<PageResult<ArticleVO>> getArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ArticleVO> result = articleService.getArticlesByCategory(categoryId, page, size);
        return Result.success(result);
    }
    
    /**
     * 按标签获取文章
     */
    @GetMapping("/articles/tag/{tagId}")
    public Result<PageResult<ArticleVO>> getArticlesByTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ArticleVO> result = articleService.getArticlesByTag(tagId, page, size);
        return Result.success(result);
    }
    
    /**
     * 搜索文章
     */
    @GetMapping("/articles/search")
    public Result<PageResult<ArticleVO>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ArticleVO> result = articleService.searchArticles(keyword, page, size);
        return Result.success(result);
    }
    
    /**
     * 热门文章
     */
    @GetMapping("/articles/hot")
    public Result<List<ArticleVO>> getHotArticles(@RequestParam(defaultValue = "10") Integer limit) {
        List<ArticleVO> articles = articleService.getHotArticles(limit);
        return Result.success(articles);
    }
    
    /**
     * 相关推荐
     */
    @GetMapping("/articles/{id}/related")
    public Result<List<ArticleVO>> getRelatedArticles(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") Integer limit) {
        List<ArticleVO> articles = articleService.getRelatedArticles(id, limit);
        return Result.success(articles);
    }
    
    /**
     * 时间归档列表
     */
    @GetMapping("/archives")
    public Result<List<ArchiveDTO>> getArchives() {
        List<ArchiveDTO> archives = articleService.getArchives();
        return Result.success(archives);
    }
    
    /**
     * 按年月获取文章
     */
    @GetMapping("/archives/{year}/{month}")
    public Result<PageResult<ArticleVO>> getArticlesByYearMonth(
            @PathVariable Integer year,
            @PathVariable Integer month,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ArticleVO> result = articleService.getArticlesByYearMonth(year, month, page, size);
        return Result.success(result);
    }
    
    // ==================== 互动相关 ====================
    
    /**
     * 点赞文章
     */
    @PostMapping("/articles/{id}/like")
    public Result<Void> likeArticle(@PathVariable Long id) {
        articleService.likeArticle(id);
        return Result.success();
    }
    
    /**
     * 取消点赞
     */
    @DeleteMapping("/articles/{id}/like")
    public Result<Void> unlikeArticle(@PathVariable Long id) {
        articleService.unlikeArticle(id);
        return Result.success();
    }
    
    /**
     * 收藏文章
     */
    @PostMapping("/articles/{id}/collect")
    public Result<Void> collectArticle(@PathVariable Long id) {
        articleService.collectArticle(id);
        return Result.success();
    }
    
    /**
     * 取消收藏
     */
    @DeleteMapping("/articles/{id}/collect")
    public Result<Void> uncollectArticle(@PathVariable Long id) {
        articleService.uncollectArticle(id);
        return Result.success();
    }
    
    // ==================== 分类标签 ====================
    
    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public Result<List<Category>> getCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }
    
    /**
     * 获取分类详情
     */
    @GetMapping("/categories/{id}")
    public Result<Category> getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategory(id);
        return Result.success(category);
    }
    
    /**
     * 获取所有标签
     */
    @GetMapping("/tags")
    public Result<List<Tag>> getTags() {
        List<Tag> tags = tagService.getTagsByArticleCount();
        return Result.success(tags);
    }
    
    /**
     * 获取标签详情
     */
    @GetMapping("/tags/{id}")
    public Result<Tag> getTag(@PathVariable Long id) {
        Tag tag = tagService.getTag(id);
        return Result.success(tag);
    }
    
    // ==================== 评论相关 ====================
    
    /**
     * 获取文章评论
     */
    @GetMapping("/articles/{articleId}/comments")
    public Result<List<CommentVO>> getArticleComments(@PathVariable Long articleId) {
        List<CommentVO> comments = commentService.getArticleComments(articleId);
        return Result.success(comments);
    }
    
    /**
     * 最新评论
     */
    @GetMapping("/comments/latest")
    public Result<List<CommentVO>> getLatestComments() {
        List<CommentVO> comments = commentService.getLatestComments();
        return Result.success(comments);
    }
    
    // ==================== 其他 ====================
    
    /**
     * 获取管理员信息 (原博主信息)
     */
    @GetMapping("/admin-info")
    public Result<UserDTO> getAdminInfo() {
        // 假设第一个管理员是站点所有者
        List<UserDTO> users = userService.getAllUsers();
        UserDTO admin = users.stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .findFirst()
                .orElse(null);
        return Result.success(admin);
    }
    
    /**
     * 获取友情链接
     */
    @GetMapping("/friend-links")
    public Result<List<FriendLink>> getFriendLinks() {
        List<FriendLink> links = friendLinkService.getVisibleFriendLinks();
        return Result.success(links);
    }
    
    /**
     * 获取站点配置
     */
    @GetMapping("/config")
    public Result<Map<String, String>> getSiteConfig() {
        Map<String, String> configs = siteConfigService.getAllConfigs();
        return Result.success(configs);
    }
}
