package com.blogs.repository;

import com.blogs.entity.LikeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRecordRepository extends JpaRepository<LikeRecord, Long> {
    Optional<LikeRecord> findByUserIdAndTargetIdAndType(Long userId, Long targetId, String type);
    Page<LikeRecord> findByUserId(Long userId, Pageable pageable);
    void deleteByUserIdAndTargetIdAndType(Long userId, Long targetId, String type);
    
    /**
     * 统计指定文章/帖子的点赞数
     */
    long countByTargetIdAndType(Long targetId, String type);
}

