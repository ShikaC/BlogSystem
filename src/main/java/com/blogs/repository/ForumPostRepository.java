package com.blogs.repository;

import com.blogs.entity.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    Page<ForumPost> findBySectionIdAndStatus(Long sectionId, Integer status, Pageable pageable);
    Page<ForumPost> findByUserId(Long userId, Pageable pageable);
    Page<ForumPost> findByStatus(Integer status, Pageable pageable);
}

