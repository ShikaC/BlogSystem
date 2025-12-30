package com.blogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * 文章请求DTO
 */
@Data
public class ArticleRequest {
    
    private Long id;
    
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "标题不能超过200字")
    private String title;
    
    private String content;
    
    @Size(max = 500, message = "摘要不能超过500字")
    private String summary;
    
    private String coverImage;
    
    /**
     * 0-草稿 1-已发布 2-私密
     */
    private Integer status = 0;
    
    private Boolean isTop = false;
    
    private Long categoryId;
    
    private Set<Long> tagIds;
    
    // SEO字段
    private String seoTitle;
    private String seoKeywords;
    private String seoDescription;
    
    // 访问密码
    private String password;
}
