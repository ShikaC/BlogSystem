package com.blogs.repository;

import com.blogs.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 前台展示 - 已通过的评论
    List<Comment> findByArticleIdAndStatusOrderByCreatedAtAsc(Long articleId, Integer status);
    
    // 后台管理
    Page<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Comment> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);
    
    // 最新评论
    List<Comment> findTop10ByStatusOrderByCreatedAtDesc(Integer status);
    
    // 统计
    long countByStatus(Integer status);
    
    @Query("SELECT COUNT(c) FROM Comment c")
    long countAll();
    
    // 批量删除
    void deleteByArticleId(Long articleId);
}
