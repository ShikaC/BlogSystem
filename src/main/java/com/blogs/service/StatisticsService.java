package com.blogs.service;

import com.blogs.dto.StatisticsDTO;
import com.blogs.repository.ArticleRepository;
import com.blogs.repository.CategoryRepository;
import com.blogs.repository.CommentRepository;
import com.blogs.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 统计服务
 */
@Service
public class StatisticsService {
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    /**
     * 获取统计数据
     */
    public StatisticsDTO getStatistics() {
        StatisticsDTO dto = new StatisticsDTO();
        
        dto.setTotalArticles(articleRepository.count());
        dto.setPublishedArticles(articleRepository.countByStatus(1));
        dto.setDraftArticles(articleRepository.countByStatus(0));
        
        Long totalViews = articleRepository.sumViewCount();
        dto.setTotalViews(totalViews != null ? totalViews : 0L);
        
        Long totalLikes = articleRepository.sumLikeCount();
        dto.setTotalLikes(totalLikes != null ? totalLikes : 0L);
        
        dto.setTotalComments(commentRepository.countAll());
        dto.setPendingComments(commentRepository.countByStatus(0));
        dto.setTotalCategories(categoryRepository.count());
        dto.setTotalTags(tagRepository.count());
        
        return dto;
    }
}
