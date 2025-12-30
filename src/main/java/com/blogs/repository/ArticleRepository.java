package com.blogs.repository;

import com.blogs.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    // 前台查询 - 只查已发布的
    Page<Article> findByStatusOrderByIsTopDescCreatedAtDesc(Integer status, Pageable pageable);
    
    Page<Article> findByCategoryIdAndStatusOrderByIsTopDescCreatedAtDesc(Long categoryId, Integer status, Pageable pageable);
    
    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t.id = :tagId AND a.status = :status ORDER BY a.isTop DESC, a.createdAt DESC")
    Page<Article> findByTagIdAndStatus(Long tagId, Integer status, Pageable pageable);
    
    // 后台查询 - 根据状态筛选
    Page<Article> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);
    
    // 全状态查询（后台管理）
    Page<Article> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 根据用户查询
    Page<Article> findByUser_UsernameOrderByCreatedAtDesc(String username, Pageable pageable);
    Page<Article> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 搜索
    @Query("""
            SELECT DISTINCT a FROM Article a
            LEFT JOIN a.tags t
            LEFT JOIN a.user u
            WHERE a.status = 1 AND (
                   a.title LIKE %:keyword%
                OR a.content LIKE %:keyword%
                OR a.summary LIKE %:keyword%
                OR t.name LIKE %:keyword%
                OR u.username LIKE %:keyword%
                OR u.nickname LIKE %:keyword%
            )
            """)
    Page<Article> searchByKeyword(String keyword, Pageable pageable);
    
    // 统计
    @Query("SELECT COUNT(a) FROM Article a WHERE a.status = :status")
    long countByStatus(Integer status);
    
    @Query("SELECT SUM(a.viewCount) FROM Article a")
    Long sumViewCount();
    
    @Query("SELECT SUM(a.likeCount) FROM Article a")
    Long sumLikeCount();
    
    // 热门文章
    Page<Article> findByStatusOrderByViewCountDesc(Integer status, Pageable pageable);
    
    // 相关推荐
    @Query("SELECT a FROM Article a WHERE a.status = 1 AND a.category.id = :categoryId AND a.id != :articleId ORDER BY a.createdAt DESC")
    List<Article> findRelatedArticles(Long categoryId, Long articleId, Pageable pageable);
    
    // 时间归档
    @Query("SELECT YEAR(a.createdAt) as year, MONTH(a.createdAt) as month, COUNT(a) as count FROM Article a WHERE a.status = 1 GROUP BY YEAR(a.createdAt), MONTH(a.createdAt) ORDER BY year DESC, month DESC")
    List<Object[]> getArchiveStats();
    
    @Query("SELECT a FROM Article a WHERE a.status = 1 AND YEAR(a.createdAt) = :year AND MONTH(a.createdAt) = :month ORDER BY a.createdAt DESC")
    Page<Article> findByYearAndMonth(Integer year, Integer month, Pageable pageable);
    
    // 更新计数
    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(Long id);
    
    @Modifying
    @Query("UPDATE Article a SET a.likeCount = a.likeCount + :delta WHERE a.id = :id")
    void updateLikeCount(Long id, int delta);
    
    @Modifying
    @Query("UPDATE Article a SET a.collectCount = a.collectCount + :delta WHERE a.id = :id")
    void updateCollectCount(Long id, int delta);
    
    @Modifying
    @Query("UPDATE Article a SET a.commentCount = a.commentCount + :delta WHERE a.id = :id")
    void updateCommentCount(Long id, int delta);
}
