package com.blogs.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.blogs.entity.Article;
import com.blogs.entity.Category;

import lombok.Data;

/**
 * 文章响应DTO
 */
@Data
public class ArticleVO {
    private Long id;
    private Long userId;
    private String authorNickname;
    private String authorAvatar;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private Integer status;
    private Boolean isTop;
    private Long viewCount;
    private Long likeCount;
    private Long collectCount;
    private Long commentCount;
    private Integer wordCount;
    private String seoTitle;
    private String seoKeywords;
    private String seoDescription;
    private Boolean hasPassword;
    private Long categoryId;
    private String categoryName;
    private List<TagVO> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    
    @Data
    public static class TagVO {
        private Long id;
        private String name;
    }
    
    public static ArticleVO fromEntity(Article article) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        if (article.getUser() != null) {
            vo.setUserId(article.getUser().getId());
            vo.setAuthorNickname(article.getUser().getNickname());
            vo.setAuthorAvatar(article.getUser().getAvatar());
        }
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setStatus(article.getStatus());
        vo.setIsTop(article.getIsTop());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setCollectCount(article.getCollectCount());
        vo.setCommentCount(article.getCommentCount());
        vo.setWordCount(article.getWordCount());
        vo.setSeoTitle(article.getSeoTitle());
        vo.setSeoKeywords(article.getSeoKeywords());
        vo.setSeoDescription(article.getSeoDescription());
        vo.setHasPassword(article.getPassword() != null && !article.getPassword().isEmpty());
        vo.setCreatedAt(article.getCreatedAt());
        vo.setUpdatedAt(article.getUpdatedAt());
        vo.setPublishedAt(article.getPublishedAt());
        
        Category category = article.getCategory();
        if (category != null) {
            vo.setCategoryId(category.getId());
            vo.setCategoryName(category.getName());
        }
        
        if (article.getTags() != null) {
            vo.setTags(article.getTags().stream().map(tag -> {
                TagVO tagVO = new TagVO();
                tagVO.setId(tag.getId());
                tagVO.setName(tag.getName());
                return tagVO;
            }).collect(Collectors.toList()));
        }
        
        return vo;
    }
    
    public static ArticleVO fromEntityWithoutContent(Article article) {
        ArticleVO vo = fromEntity(article);
        vo.setContent(null);
        return vo;
    }
}
