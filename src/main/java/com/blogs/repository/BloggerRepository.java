package com.blogs.repository;

import com.blogs.entity.Blogger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BloggerRepository extends JpaRepository<Blogger, Long> {
    Optional<Blogger> findByUsername(String username);
    boolean existsByUsername(String username);
}
