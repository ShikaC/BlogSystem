package com.blogs.repository;

import com.blogs.entity.LikeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRecordRepository extends JpaRepository<LikeRecord, Long> {
    Optional<LikeRecord> findByUserIdAndTargetIdAndType(Long userId, Long targetId, String type);
    void deleteByUserIdAndTargetIdAndType(Long userId, Long targetId, String type);
}

