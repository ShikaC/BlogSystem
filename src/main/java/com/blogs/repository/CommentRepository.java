package com.blogs.repository;

import com.blogs.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * 前台展示 - 文章评论（兼容旧数据：targetType 为空时按 legacyArticleId 匹配）
     */
    @Query("""
            SELECT c FROM Comment c
            WHERE c.status = :status
              AND (
                   (c.targetType = 'ARTICLE' AND c.targetId = :articleId)
                OR (c.targetType IS NULL AND c.legacyArticleId = :articleId)
              )
            ORDER BY c.createdAt ASC
            """)
    List<Comment> findArticleCommentsCompatible(Long articleId, Integer status);

    /**
     * 统一查询 - 按目标类型 + 目标ID + 状态分页
     */
    Page<Comment> findByTargetTypeAndTargetIdAndStatus(String targetType, Long targetId, Integer status, Pageable pageable);

    Page<Comment> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 后台管理
    Page<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Comment> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);
    
    // 最新评论
    List<Comment> findTop10ByStatusOrderByCreatedAtDesc(Integer status);
    
    // 统计
    long countByStatus(Integer status);
    
    @Query("SELECT COUNT(c) FROM Comment c")
    long countAll();

    long countByTargetType(String targetType);
    
    // 批量删除
    @Modifying
    @Query("""
            DELETE FROM Comment c
            WHERE (c.targetType = 'ARTICLE' AND c.targetId = :articleId)
               OR (c.targetType IS NULL AND c.legacyArticleId = :articleId)
            """)
    void deleteByArticleIdCompatible(Long articleId);
}
