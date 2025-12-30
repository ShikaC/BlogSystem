package com.blogs.repository;

import com.blogs.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIdIsNullOrderBySortOrderAsc();
    List<Category> findByParentIdOrderBySortOrderAsc(Long parentId);
    boolean existsByName(String name);
    
    @Modifying
    @Query("UPDATE Category c SET c.articleCount = c.articleCount + :delta WHERE c.id = :id")
    void updateArticleCount(Long id, int delta);
}
