-- =============================================
-- 博客 + 论坛一体化综合系统 数据库初始化脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE blog_db;

-- =============================================
-- 1. 用户表 (两级角色：ADMIN, USER)
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(100) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像URL',
    bio TEXT COMMENT '个人简介',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN-超级管理员, USER-注册用户',
    status INT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    github VARCHAR(200) COMMENT 'GitHub地址',
    zhihu VARCHAR(200) COMMENT '知乎地址',
    weixin VARCHAR(100) COMMENT '微信',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- =============================================
-- 2. 文章分类表 (博客)
-- =============================================
CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(200) COMMENT '分类描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    parent_id BIGINT DEFAULT NULL COMMENT '父分类ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类表';

-- =============================================
-- 3. 文章标签表 (博客)
-- =============================================
CREATE TABLE IF NOT EXISTS tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    color VARCHAR(20) DEFAULT '#409eff' COMMENT '标签颜色',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签表';

-- =============================================
-- 4. 文章表 (博客)
-- =============================================
CREATE TABLE IF NOT EXISTS article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    content LONGTEXT COMMENT '文章内容(HTML)',
    summary VARCHAR(500) COMMENT '文章摘要',
    cover_image VARCHAR(500) COMMENT '封面图URL',
    status INT DEFAULT 0 COMMENT '状态: 0-草稿 1-已发布 2-私密 3-回收站 4-待审核',
    is_top BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    password VARCHAR(100) COMMENT '访问密码(可选)',
    view_count BIGINT DEFAULT 0 COMMENT '阅读量',
    like_count BIGINT DEFAULT 0 COMMENT '点赞数',
    collect_count BIGINT DEFAULT 0 COMMENT '收藏数',
    comment_count BIGINT DEFAULT 0 COMMENT '评论数',
    word_count INT DEFAULT 0 COMMENT '字数',
    category_id BIGINT COMMENT '分类ID',
    seo_title VARCHAR(200) COMMENT 'SEO标题',
    seo_keywords VARCHAR(200) COMMENT 'SEO关键词',
    seo_description VARCHAR(500) COMMENT 'SEO描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    published_at DATETIME COMMENT '发布时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_is_top (is_top),
    INDEX idx_category_id (category_id),
    INDEX idx_created_at (created_at),
    FULLTEXT INDEX ft_title_content (title, content) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- =============================================
-- 5. 文章-标签关联表
-- =============================================
CREATE TABLE IF NOT EXISTS article_tag (
    article_id BIGINT NOT NULL COMMENT '文章ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (article_id, tag_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- =============================================
-- 6. 文章评论表
-- =============================================
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    user_id BIGINT COMMENT '评论者用户ID(匿名则为NULL)',
    content TEXT NOT NULL COMMENT '评论内容',
    nickname VARCHAR(50) COMMENT '评论者昵称(冗余)',
    avatar VARCHAR(500) COMMENT '头像URL(冗余)',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    status INT DEFAULT 1 COMMENT '状态: 0-待审核 1-已通过 2-已拒绝',
    parent_id BIGINT COMMENT '父评论ID(回复)',
    reply_to_id BIGINT COMMENT '回复对象用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_article_id (article_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章评论表';

-- =============================================
-- 7. 论坛板块表
-- =============================================
CREATE TABLE IF NOT EXISTS forum_section (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '板块名称',
    description VARCHAR(200) COMMENT '板块描述',
    icon VARCHAR(200) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    parent_id BIGINT DEFAULT NULL COMMENT '父板块ID',
    status INT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛板块表';

-- =============================================
-- 8. 论坛帖子表
-- =============================================
CREATE TABLE IF NOT EXISTS forum_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    section_id BIGINT NOT NULL COMMENT '所属板块ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    title VARCHAR(200) NOT NULL COMMENT '帖子标题',
    content LONGTEXT COMMENT '帖子内容',
    status INT DEFAULT 1 COMMENT '状态: 0-草稿 1-已发布 2-私密 3-回收站 4-待审核',
    is_top BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    is_essence BOOLEAN DEFAULT FALSE COMMENT '是否精华',
    view_count BIGINT DEFAULT 0 COMMENT '阅读量',
    like_count BIGINT DEFAULT 0 COMMENT '点赞数',
    collect_count BIGINT DEFAULT 0 COMMENT '收藏数',
    comment_count BIGINT DEFAULT 0 COMMENT '回帖数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_section_id (section_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_is_top (is_top),
    INDEX idx_created_at (created_at),
    FULLTEXT INDEX ft_title_content (title, content) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛帖子表';

-- =============================================
-- 9. 论坛回帖表
-- =============================================
CREATE TABLE IF NOT EXISTS forum_post_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    post_id BIGINT NOT NULL COMMENT '所属帖子ID',
    user_id BIGINT NOT NULL COMMENT '回帖者ID',
    content TEXT NOT NULL COMMENT '回帖内容',
    parent_id BIGINT COMMENT '父级回帖ID(针对回帖的回复)',
    reply_to_id BIGINT COMMENT '回复对象用户ID',
    status INT DEFAULT 1 COMMENT '状态: 0-待审核 1-已通过 2-已拒绝',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛回帖表';

-- =============================================
-- 10. 收藏表 (统一收藏 文章 + 帖子)
-- =============================================
CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_id BIGINT NOT NULL COMMENT '目标ID (文章或帖子ID)',
    type VARCHAR(20) NOT NULL COMMENT '类型: ARTICLE, POST',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_target_type (user_id, target_id, type),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- =============================================
-- 11. 点赞表 (统一点赞 文章 + 帖子)
-- =============================================
CREATE TABLE IF NOT EXISTS like_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_id BIGINT NOT NULL COMMENT '目标ID (文章或帖子ID)',
    type VARCHAR(20) NOT NULL COMMENT '类型: ARTICLE, POST',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_target_type (user_id, target_id, type),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户点赞表';

-- =============================================
-- 12. 消息通知表
-- =============================================
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    sender_id BIGINT COMMENT '发送者ID(系统通知则为NULL)',
    type VARCHAR(50) NOT NULL COMMENT '类型: COMMENT, REPLY, LIKE, COLLECT, SYSTEM, AUDIT',
    title VARCHAR(200) COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容',
    target_id BIGINT COMMENT '关联目标ID (文章、帖子或评论ID)',
    target_type VARCHAR(20) COMMENT '目标类型: ARTICLE, POST, COMMENT',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- =============================================
-- 其他原表保留并适配
-- =============================================
CREATE TABLE IF NOT EXISTS friend_link (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    logo VARCHAR(500),
    description VARCHAR(200),
    sort_order INT DEFAULT 0,
    is_visible BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS media (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL,
    filepath VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    category VARCHAR(50),
    url VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS site_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(200),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 初始化数据
-- =============================================
-- 默认管理员: admin / 123456 (BCrypt加密)
INSERT INTO users (username, password, nickname, role, status) VALUES 
('admin', '$2b$12$AyzmbsXTz6URch/o5XcMoOFvrQUHJAgf5MXxW/9tjyj78FyhI/qBi', '超级管理员', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE username = username;

INSERT INTO site_config (config_key, config_value, description) VALUES
('site_name', '综合系统', '站点名称'),
('enable_audit', 'false', '是否开启内容审核'),
('enable_comment', 'true', '是否开启评论'),
('articles_per_page', '10', '每页文章数')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

SELECT '数据库重构脚本执行完成！' AS message;
