package com.blogs.service;

import com.blogs.dto.StatisticsDTO;
import com.blogs.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

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
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

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

        Long articleViews = articleRepository.sumViewCount();
        Long postViews = postRepository.sumViewCount();
        dto.setTotalViews((articleViews != null ? articleViews : 0L) + (postViews != null ? postViews : 0L));

        Long articleLikes = articleRepository.sumLikeCount();
        Long postLikes = postRepository.sumLikeCount();
        dto.setTotalLikes((articleLikes != null ? articleLikes : 0L) + (postLikes != null ? postLikes : 0L));

        // 统一评论/回帖：comment 表既包含文章评论也包含帖子回帖
        dto.setTotalComments(commentRepository.countAll());
        dto.setTotalPostComments(commentRepository.countByTargetType("FORUM_POST"));
        dto.setPendingComments(commentRepository.countByStatus(0));
        dto.setTotalCategories(categoryRepository.count());
        dto.setTotalTags(tagRepository.count());

        // 收藏数
        dto.setTotalCollects(favoriteRepository.count());

        // 今日新增
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        dto.setTodayNewUsers(countUsersCreatedAfter(todayStart));
        dto.setTodayNewArticles(countArticlesCreatedAfter(todayStart));
        dto.setTodayNewPosts(countPostsCreatedAfter(todayStart));
        dto.setTodayNewComments(countCommentsCreatedAfter(todayStart));

        // 活跃用户（近7天创建的用户，简化处理）
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        dto.setActiveUsers(countUsersCreatedAfter(sevenDaysAgo));

        // 趋势数据（简化实现，返回空列表，后续可扩展）
        dto.setUserTrend(new ArrayList<>());
        dto.setContentTrend(new ArrayList<>());
        dto.setInteractionTrend(new ArrayList<>());

        return dto;
    }

    private long countUsersCreatedAfter(LocalDateTime dateTime) {
        return userRepository.countByCreatedAtAfter(dateTime);
    }

    private long countArticlesCreatedAfter(LocalDateTime dateTime) {
        return articleRepository.countByCreatedAtAfter(dateTime);
    }

    private long countPostsCreatedAfter(LocalDateTime dateTime) {
        return postRepository.countByCreatedAtAfter(dateTime);
    }

    private long countCommentsCreatedAfter(LocalDateTime dateTime) {
        return commentRepository.countByCreatedAtAfter(dateTime);
    }
}
