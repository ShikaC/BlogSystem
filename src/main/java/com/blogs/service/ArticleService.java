package com.blogs.service;

import com.blogs.common.PageResult;
import com.blogs.dto.*;
import com.blogs.entity.Article;
import com.blogs.entity.Category;
import com.blogs.entity.Tag;
import com.blogs.entity.User;
import com.blogs.exception.BusinessException;
import com.blogs.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章服务
 */
@Service
@Transactional
public class ArticleService {
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 保存文章（新增/编辑）
     */
    public Article saveArticle(ArticleRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        Article article;
        boolean isNew = request.getId() == null;
        
        if (isNew) {
            article = new Article();
            article.setUser(user);
        } else {
            article = articleRepository.findById(request.getId())
                    .orElseThrow(() -> new BusinessException("文章不存在"));
            // 权限校验：管理员可以修改任何文章，普通用户只能修改自己的
            if (!"ADMIN".equals(user.getRole()) && !article.getUser().getId().equals(user.getId())) {
                throw new BusinessException("无权修改他人文章");
            }
        }
        
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setStatus(request.getStatus());
        article.setIsTop(request.getIsTop());
        article.setSeoTitle(request.getSeoTitle());
        article.setSeoKeywords(request.getSeoKeywords());
        article.setSeoDescription(request.getSeoDescription());
        article.setPassword(request.getPassword());
        
        // 计算字数
        if (StringUtils.hasText(request.getContent())) {
            String text = request.getContent().replaceAll("<[^>]*>", "").replaceAll("\\s+", "");
            article.setWordCount(text.length());
        }
        
        // 处理分类
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException("分类不存在"));
            
            // 更新文章数
            if (isNew || !request.getCategoryId().equals(article.getCategory() != null ? article.getCategory().getId() : null)) {
                if (article.getCategory() != null) {
                    categoryRepository.updateArticleCount(article.getCategory().getId(), -1);
                }
                categoryRepository.updateArticleCount(category.getId(), 1);
            }
            article.setCategory(category);
        }
        
        // 处理标签
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Long> oldTagIds = article.getTags().stream().map(Tag::getId).collect(Collectors.toSet());
            List<Tag> newTags = tagRepository.findByIdIn(request.getTagIds());
            
            // 更新标签文章数
            for (Tag tag : newTags) {
                if (!oldTagIds.contains(tag.getId())) {
                    tagRepository.updateArticleCount(tag.getId(), 1);
                }
            }
            for (Long oldTagId : oldTagIds) {
                if (!request.getTagIds().contains(oldTagId)) {
                    tagRepository.updateArticleCount(oldTagId, -1);
                }
            }
            
            article.setTags(new HashSet<>(newTags));
        } else {
            // 清空标签时更新计数
            for (Tag tag : article.getTags()) {
                tagRepository.updateArticleCount(tag.getId(), -1);
            }
            article.setTags(new HashSet<>());
        }
        
        // 发布时间
        if (request.getStatus() == 1 && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        
        return articleRepository.save(article);
    }
    
    /**
     * 获取文章详情
     */
    public ArticleVO getArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        return ArticleVO.fromEntity(article);
    }
    
    /**
     * 删除文章（移到回收站）
     */
    public void moveToTrash(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        article.setStatus(3);
        articleRepository.save(article);
    }
    
    /**
     * 批量删除文章
     */
    public void batchMoveToTrash(List<Long> ids) {
        ids.forEach(this::moveToTrash);
    }
    
    /**
     * 恢复文章
     */
    public void restoreFromTrash(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        article.setStatus(0);
        articleRepository.save(article);
    }
    
    /**
     * 彻底删除文章
     */
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        
        // 更新分类文章数
        if (article.getCategory() != null) {
            categoryRepository.updateArticleCount(article.getCategory().getId(), -1);
        }
        
        // 更新标签文章数
        for (Tag tag : article.getTags()) {
            tagRepository.updateArticleCount(tag.getId(), -1);
        }
        
        // 删除相关评论
        commentRepository.deleteByArticleId(id);
        
        articleRepository.delete(article);
    }
    
    /**
     * 置顶/取消置顶
     */
    public void toggleTop(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        article.setIsTop(!article.getIsTop());
        articleRepository.save(article);
    }
    
    /**
     * 批量置顶
     */
    public void batchTop(List<Long> ids, Boolean isTop) {
        for (Long id : ids) {
            Article article = articleRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("文章不存在"));
            article.setIsTop(isTop);
            articleRepository.save(article);
        }
    }
    
    /**
     * 后台文章列表
     */
    public PageResult<ArticleVO> getAdminArticleList(Integer status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Article> articlePage;
        
        if (status == null) {
            articlePage = articleRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            articlePage = articleRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
        
        List<ArticleVO> list = articlePage.getContent().stream()
                .map(ArticleVO::fromEntityWithoutContent)
                .collect(Collectors.toList());
        
        return PageResult.of(list, articlePage.getTotalElements(), page, size);
    }
    
    /**
     * 前台文章列表
     */
    public PageResult<ArticleVO> getPublishedArticleList(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Article> articlePage = articleRepository.findByStatusOrderByIsTopDescCreatedAtDesc(1, pageable);
        
        List<ArticleVO> list = articlePage.getContent().stream()
                .map(ArticleVO::fromEntityWithoutContent)
                .collect(Collectors.toList());
        
        return PageResult.of(list, articlePage.getTotalElements(), page, size);
    }
    
    /**
     * 按分类获取文章
     */
    public PageResult<ArticleVO> getArticlesByCategory(Long categoryId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Article> articlePage = articleRepository.findByCategoryIdAndStatusOrderByIsTopDescCreatedAtDesc(
                categoryId, 1, pageable);
        
        List<ArticleVO> list = articlePage.getContent().stream()
                .map(ArticleVO::fromEntityWithoutContent)
                .collect(Collectors.toList());
        
        return PageResult.of(list, articlePage.getTotalElements(), page, size);
    }
    
    /**
     * 按标签获取文章
     */
    public PageResult<ArticleVO> getArticlesByTag(Long tagId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Article> articlePage = articleRepository.findByTagIdAndStatus(tagId, 1, pageable);
        
        List<ArticleVO> list = articlePage.getContent().stream()
                .map(ArticleVO::fromEntityWithoutContent)
                .collect(Collectors.toList());
        
        return PageResult.of(list, articlePage.getTotalElements(), page, size);
    }
    
    /**
     * 搜索文章
     */
    public PageResult<ArticleVO> searchArticles(String keyword, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Article> articlePage = articleRepository.searchByKeyword(keyword, pageable);
        
        List<ArticleVO> list = articlePage.getContent().stream()
                .map(ArticleVO::fromEntityWithoutContent)
                .collect(Collectors.toList());
        
        return PageResult.of(list, articlePage.getTotalElements(), page, size);
    }
    
    /**
     * 热门文章
     */
    public List<ArticleVO> getHotArticles(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Article> articlePage = articleRepository.findByStatusOrderByViewCountDesc(1, pageable);
        
        return articlePage.getContent().stream()
                .map(ArticleVO::fromEntityWithoutContent)
                .collect(Collectors.toList());
    }
    
    /**
     * 相关推荐
     */
    public List<ArticleVO> getRelatedArticles(Long articleId, Integer limit) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        
        if (article.getCategory() == null) {
            return List.of();
        }
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Article> relatedArticles = articleRepository.findRelatedArticles(
                article.getCategory().getId(), articleId, pageable);
        
        return relatedArticles.stream()
                .map(ArticleVO::fromEntityWithoutContent)
                .collect(Collectors.toList());
    }
    
    /**
     * 时间归档
     */
    public List<ArchiveDTO> getArchives() {
        List<Object[]> stats = articleRepository.getArchiveStats();
        return stats.stream()
                .map(row -> ArchiveDTO.of(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue(),
                        ((Number) row[2]).longValue()))
                .collect(Collectors.toList());
    }
    
    /**
     * 按年月获取文章
     */
    public PageResult<ArticleVO> getArticlesByYearMonth(Integer year, Integer month, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Article> articlePage = articleRepository.findByYearAndMonth(year, month, pageable);
        
        List<ArticleVO> list = articlePage.getContent().stream()
                .map(ArticleVO::fromEntityWithoutContent)
                .collect(Collectors.toList());
        
        return PageResult.of(list, articlePage.getTotalElements(), page, size);
    }
    
    /**
     * 获取指定用户的文章列表 (个人中心)
     */
    public PageResult<ArticleVO> getUserArticleList(String username, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Article> articlePage = articleRepository.findByUser_UsernameOrderByCreatedAtDesc(username, pageable);
        
        List<ArticleVO> list = articlePage.getContent().stream()
                .map(ArticleVO::fromEntityWithoutContent)
                .collect(Collectors.toList());
        
        return PageResult.of(list, articlePage.getTotalElements(), page, size);
    }

    /**
     * 增加阅读量
     */
    public void incrementViewCount(Long id) {
        articleRepository.incrementViewCount(id);
    }
    
    /**
     * 点赞
     */
    public void likeArticle(Long id) {
        articleRepository.updateLikeCount(id, 1);
    }
    
    /**
     * 取消点赞
     */
    public void unlikeArticle(Long id) {
        articleRepository.updateLikeCount(id, -1);
    }
    
    /**
     * 收藏
     */
    public void collectArticle(Long id) {
        articleRepository.updateCollectCount(id, 1);
    }
    
    /**
     * 取消收藏
     */
    public void uncollectArticle(Long id) {
        articleRepository.updateCollectCount(id, -1);
    }
    
    /**
     * 验证文章密码
     */
    public boolean verifyPassword(Long id, String password) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        return password.equals(article.getPassword());
    }
}
