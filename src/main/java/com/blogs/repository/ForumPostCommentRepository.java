package com.blogs.repository;

import com.blogs.entity.ForumPostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostCommentRepository extends JpaRepository<ForumPostComment, Long> {
    Page<ForumPostComment> findByPostIdAndStatus(Long postId, Integer status, Pageable pageable);
    List<ForumPostComment> findByParentIdOrderByCreatedAtAsc(Long parentId);
    Page<ForumPostComment> findByUserId(Long userId, Pageable pageable);
}

