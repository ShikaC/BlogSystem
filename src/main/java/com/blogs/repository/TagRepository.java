package com.blogs.repository;

import com.blogs.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);
    List<Tag> findByIdIn(Set<Long> ids);
    List<Tag> findAllByOrderByArticleCountDesc();
    
    @Modifying
    @Query("UPDATE Tag t SET t.articleCount = t.articleCount + :delta WHERE t.id = :id")
    void updateArticleCount(Long id, int delta);
}
