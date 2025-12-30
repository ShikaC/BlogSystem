package com.blogs.repository;

import com.blogs.entity.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    Page<Media> findByFileTypeContainingOrderByCreatedAtDesc(String fileType, Pageable pageable);
    Page<Media> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);
    Page<Media> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Media> findByOriginalNameContainingOrderByCreatedAtDesc(String keyword, Pageable pageable);
}
