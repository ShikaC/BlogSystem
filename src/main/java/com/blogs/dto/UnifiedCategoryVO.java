package com.blogs.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 全站统一分类/版块 VO（导航与筛选统一使用）
 *
 * 说明：
 * - CATEGORY：来源于 category 表（博客分类）
 * - SECTION：来源于 forum_section 表（论坛版块）
 *
 * 目的：在不破坏既有表结构与业务的前提下，实现“导航/筛选的一体化输出”。
 */
@Data
public class UnifiedCategoryVO {

    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Integer sortOrder;

    /**
     * CATEGORY / SECTION
     */
    private String type;

    /**
     * 仅 SECTION 使用：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 仅 SECTION 使用：图标
     */
    private String icon;

    private LocalDateTime createdAt;
}


