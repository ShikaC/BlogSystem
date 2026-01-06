package com.blogs.repository;

import com.blogs.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /**
     * 分页查询所有用户（按创建时间倒序）
     */
    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 按用户名模糊查询
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.nickname LIKE %:keyword% ORDER BY u.createdAt DESC")
    Page<User> findByKeyword(String keyword, Pageable pageable);

    /**
     * 按角色查询
     */
    Page<User> findByRoleOrderByCreatedAtDesc(String role, Pageable pageable);

    /**
     * 按状态查询
     */
    Page<User> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    /**
     * 按角色和状态查询
     */
    Page<User> findByRoleAndStatusOrderByCreatedAtDesc(String role, Integer status, Pageable pageable);

    /**
     * 按关键词和角色查询
     */
    @Query("SELECT u FROM User u WHERE (u.username LIKE %:keyword% OR u.nickname LIKE %:keyword%) AND u.role = :role ORDER BY u.createdAt DESC")
    Page<User> findByKeywordAndRole(String keyword, String role, Pageable pageable);

    /**
     * 按关键词和状态查询
     */
    @Query("SELECT u FROM User u WHERE (u.username LIKE %:keyword% OR u.nickname LIKE %:keyword%) AND u.status = :status ORDER BY u.createdAt DESC")
    Page<User> findByKeywordAndStatus(String keyword, Integer status, Pageable pageable);

    /**
     * 按关键词、角色和状态查询
     */
    @Query("SELECT u FROM User u WHERE (u.username LIKE %:keyword% OR u.nickname LIKE %:keyword%) AND u.role = :role AND u.status = :status ORDER BY u.createdAt DESC")
    Page<User> findByKeywordAndRoleAndStatus(String keyword, String role, Integer status, Pageable pageable);

    /**
     * 统计用户数（不同状态）
     */
    long countByStatus(Integer status);

    /**
     * 统计用户数（不同角色）
     */
    long countByRole(String role);

    /**
     * 统计指定时间之后创建的用户数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :dateTime")
    long countByCreatedAtAfter(java.time.LocalDateTime dateTime);
}
