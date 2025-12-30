package com.blogs.service;

import com.blogs.dto.StatisticsDTO;
import com.blogs.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 统计服务 - 聚合全站数据
 */
@Service
public class StatisticsService {
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private ForumPostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ForumPostCommentRepository postCommentRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    /**
     * 获取全站可视化统计数据
     */
    public StatisticsDTO getStatistics() {
        StatisticsDTO dto = new StatisticsDTO();
        
        dto.setTotalArticles(articleRepository.count());
        dto.setTotalPosts(postRepository.count());
        dto.setTotalUsers(userRepository.count());
        
        dto.setPublishedArticles(articleRepository.countByStatus(1));
        dto.setDraftArticles(articleRepository.countByStatus(0));
        
        Long totalViews = articleRepository.sumViewCount();
        dto.setTotalViews(totalViews != null ? totalViews : 0L);
        
        Long totalLikes = articleRepository.sumLikeCount();
        dto.setTotalLikes(totalLikes != null ? totalLikes : 0L);
        
        dto.setTotalComments(commentRepository.countAll());
        dto.setTotalPostComments(postCommentRepository.count());
        dto.setPendingComments(commentRepository.countByStatus(0));
        dto.setTotalCategories(categoryRepository.count());
        dto.setTotalTags(tagRepository.count());
        
        return dto;
    }
}
