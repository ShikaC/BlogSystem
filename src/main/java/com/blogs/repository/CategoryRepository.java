package com.blogs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.blogs.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIdIsNullOrderBySortOrderAsc();

    List<Category> findByParentIdOrderBySortOrderAsc(Long parentId);

    boolean existsByName(String name);

    List<Category> findByTypeOrderBySortOrderAsc(String type);

    @Modifying
    @Query("UPDATE Category c SET c.articleCount = c.articleCount + :delta WHERE c.id = :id")
    void updateArticleCount(Long id, int delta);

    @Query("SELECT COUNT(a) FROM Article a WHERE a.category.id = :categoryId")
    long countArticlesByCategoryId(Long categoryId);
}
