package com.blogs.repository;

import com.blogs.entity.ForumSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumSectionRepository extends JpaRepository<ForumSection, Long> {
    List<ForumSection> findAllByOrderBySortOrderAsc();
    List<ForumSection> findByStatusOrderBySortOrderAsc(Integer status);
}

