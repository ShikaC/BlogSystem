package com.blogs.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户列表DTO - 用于后台分页展示
 */
@Data
public class UserListDTO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 统计信息
    private Long articleCount;
    private Long postCount;
}
