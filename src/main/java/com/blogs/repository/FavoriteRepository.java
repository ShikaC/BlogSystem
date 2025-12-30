package com.blogs.repository;

import com.blogs.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndTargetIdAndType(Long userId, Long targetId, String type);
    Page<Favorite> findByUserIdAndType(Long userId, String type, Pageable pageable);
    void deleteByUserIdAndTargetIdAndType(Long userId, Long targetId, String type);
}

