package com.blogs.repository;

import com.blogs.entity.ForumPost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    Page<ForumPost> findBySectionIdAndStatus(Long sectionId, Integer status, Pageable pageable);
    Page<ForumPost> findByUserId(Long userId, Pageable pageable);
    Page<ForumPost> findByStatus(Integer status, Pageable pageable);

    // 更新计数（与 ArticleRepository 保持一致的增量接口，便于评论/回帖系统统一）
    @Modifying
    @Query("UPDATE ForumPost p SET p.commentCount = p.commentCount + :delta WHERE p.id = :id")
    void updateCommentCount(Long id, int delta);

    /**
     * 搜索帖子（标题/正文/用户名）
     */
    @Query("""
            SELECT p FROM ForumPost p
            WHERE p.status = 1 AND (
                   p.title LIKE %:keyword%
                OR p.content LIKE %:keyword%
                OR EXISTS (SELECT 1 FROM User u WHERE u.id = p.userId AND (u.username LIKE %:keyword% OR u.nickname LIKE %:keyword%))
            )
            """)
    Page<ForumPost> searchByKeyword(String keyword, Pageable pageable);
}

