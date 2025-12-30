-- =============================================
-- 博客系统数据库初始化脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE blog_db;

-- =============================================
-- 1. 博主表
-- =============================================
CREATE TABLE IF NOT EXISTS blogger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(100) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像URL',
    bio TEXT COMMENT '个人简介',
    email VARCHAR(100) COMMENT '邮箱',
    github VARCHAR(200) COMMENT 'GitHub地址',
    weibo VARCHAR(200) COMMENT '微博地址',
    wechat VARCHAR(100) COMMENT '微信公众号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博主信息表';

-- =============================================
-- 2. 分类表
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
-- 3. 标签表
-- =============================================
CREATE TABLE IF NOT EXISTS tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    color VARCHAR(20) DEFAULT '#409eff' COMMENT '标签颜色',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签表';

-- =============================================
-- 4. 文章表
-- =============================================
CREATE TABLE IF NOT EXISTS article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    content LONGTEXT COMMENT '文章内容(HTML)',
    summary VARCHAR(500) COMMENT '文章摘要',
    cover_image VARCHAR(500) COMMENT '封面图URL',
    status INT DEFAULT 1 COMMENT '状态: 0-草稿 1-已发布 2-私密 3-回收站',
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
    INDEX idx_status (status),
    INDEX idx_is_top (is_top),
    INDEX idx_category_id (category_id),
    INDEX idx_created_at (created_at),
    INDEX idx_published_at (published_at),
    INDEX idx_view_count (view_count),
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
-- 6. 评论表
-- =============================================
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    content TEXT NOT NULL COMMENT '评论内容',
    nickname VARCHAR(50) COMMENT '评论者昵称',
    email VARCHAR(100) COMMENT '评论者邮箱',
    website VARCHAR(200) COMMENT '评论者网站',
    avatar VARCHAR(500) COMMENT '头像URL',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    status INT DEFAULT 1 COMMENT '状态: 0-待审核 1-已通过 2-已拒绝',
    is_blogger BOOLEAN DEFAULT FALSE COMMENT '是否博主回复',
    parent_id BIGINT COMMENT '父评论ID(回复)',
    reply_to_id BIGINT COMMENT '回复对象ID',
    reply_to_nickname VARCHAR(50) COMMENT '回复对象昵称',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_article_id (article_id),
    INDEX idx_status (status),
    INDEX idx_parent_id (parent_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- =============================================
-- 7. 友情链接表
-- =============================================
CREATE TABLE IF NOT EXISTS friend_link (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '链接名称',
    url VARCHAR(500) NOT NULL COMMENT '链接地址',
    logo VARCHAR(500) COMMENT 'Logo图片',
    description VARCHAR(200) COMMENT '链接描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_visible BOOLEAN DEFAULT TRUE COMMENT '是否显示',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_sort_order (sort_order),
    INDEX idx_is_visible (is_visible)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='友情链接表';

-- =============================================
-- 8. 媒体文件表
-- =============================================
CREATE TABLE IF NOT EXISTS media (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    filename VARCHAR(255) NOT NULL COMMENT '原始文件名',
    filepath VARCHAR(500) NOT NULL COMMENT '存储路径',
    file_type VARCHAR(50) COMMENT '文件类型',
    file_size BIGINT COMMENT '文件大小(字节)',
    category VARCHAR(50) COMMENT '分类',
    url VARCHAR(500) COMMENT '访问URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_category (category),
    INDEX idx_file_type (file_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='媒体文件表';

-- =============================================
-- 9. 站点配置表
-- =============================================
CREATE TABLE IF NOT EXISTS site_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(200) COMMENT '配置说明',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站点配置表';

-- =============================================
-- 初始化站点配置
-- =============================================
INSERT INTO site_config (config_key, config_value, description) VALUES
('site_name', '我的博客', '站点名称'),
('site_subtitle', '记录生活，分享技术', '站点副标题'),
('site_logo', '', '站点Logo'),
('site_favicon', '', '站点图标'),
('site_footer', '© 2025 My Blog. All rights reserved.', '页脚版权信息'),
('site_icp', '', 'ICP备案号'),
('site_keywords', '博客,技术,分享', '站点SEO关键词'),
('site_description', '一个简洁的个人博客', '站点SEO描述'),
('articles_per_page', '10', '每页文章数'),
('summary_length', '200', '摘要截取长度'),
('enable_comment', 'true', '是否开启评论'),
('show_view_count', 'true', '是否显示阅读量'),
('show_like_count', 'true', '是否显示点赞数'),
('about_content', '欢迎来到我的博客！', '关于页面内容')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- =============================================
-- 创建上传文件目录的提示
-- =============================================
-- 注意：请手动在项目根目录创建 uploads 文件夹用于存储上传的文件

SELECT '数据库初始化完成！' AS message;
