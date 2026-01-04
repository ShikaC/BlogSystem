-- =============================================
-- 数据库升级脚本：统一评论系统支持
-- 用于已有数据库的表结构升级
-- =============================================

USE blog_db;

-- 为 comment 表添加统一评论所需的字段（如果不存在）
-- 添加 target_id 字段
SET
    @exist := (
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE
            table_schema = DATABASE()
            AND table_name = 'comment'
            AND column_name = 'target_id'
    );

SET
    @sql := IF(
        @exist = 0,
        'ALTER TABLE comment ADD COLUMN target_id BIGINT COMMENT ''统一目标ID：文章ID / 帖子ID''',
        'SELECT ''target_id 已存在'''
    );

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- 添加 target_type 字段
SET
    @exist := (
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE
            table_schema = DATABASE()
            AND table_name = 'comment'
            AND column_name = 'target_type'
    );

SET
    @sql := IF(
        @exist = 0,
        'ALTER TABLE comment ADD COLUMN target_type VARCHAR(20) COMMENT ''统一目标类型：ARTICLE / FORUM_POST''',
        'SELECT ''target_type 已存在'''
    );

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- 添加 reply_to_nickname 字段
SET
    @exist := (
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE
            table_schema = DATABASE()
            AND table_name = 'comment'
            AND column_name = 'reply_to_nickname'
    );

SET
    @sql := IF(
        @exist = 0,
        'ALTER TABLE comment ADD COLUMN reply_to_nickname VARCHAR(50) COMMENT ''回复对象昵称''',
        'SELECT ''reply_to_nickname 已存在'''
    );

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- 添加 is_blogger 字段
SET
    @exist := (
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE
            table_schema = DATABASE()
            AND table_name = 'comment'
            AND column_name = 'is_blogger'
    );

SET
    @sql := IF(
        @exist = 0,
        'ALTER TABLE comment ADD COLUMN is_blogger BOOLEAN DEFAULT FALSE COMMENT ''是否为博主回复''',
        'SELECT ''is_blogger 已存在'''
    );

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- 添加 email 字段
SET
    @exist := (
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE
            table_schema = DATABASE()
            AND table_name = 'comment'
            AND column_name = 'email'
    );

SET
    @sql := IF(
        @exist = 0,
        'ALTER TABLE comment ADD COLUMN email VARCHAR(100) COMMENT ''邮箱''',
        'SELECT ''email 已存在'''
    );

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- 添加 website 字段
SET
    @exist := (
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE
            table_schema = DATABASE()
            AND table_name = 'comment'
            AND column_name = 'website'
    );

SET
    @sql := IF(
        @exist = 0,
        'ALTER TABLE comment ADD COLUMN website VARCHAR(200) COMMENT ''网站''',
        'SELECT ''website 已存在'''
    );

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- 修改 article_id 允许为空（如果目前是 NOT NULL）
ALTER TABLE comment
MODIFY COLUMN article_id BIGINT COMMENT '文章ID (历史兼容字段)';

-- 添加复合索引（如果不存在）
SET
    @exist := (
        SELECT COUNT(*)
        FROM information_schema.statistics
        WHERE
            table_schema = DATABASE()
            AND table_name = 'comment'
            AND index_name = 'idx_target'
    );

SET
    @sql := IF(
        @exist = 0,
        'ALTER TABLE comment ADD INDEX idx_target (target_type, target_id)',
        'SELECT ''idx_target 索引已存在'''
    );

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- 迁移旧数据：将已有的文章评论设置 target_type 和 target_id
UPDATE comment
SET
    target_type = 'ARTICLE',
    target_id = article_id
WHERE
    article_id IS NOT NULL
    AND target_type IS NULL;

SELECT '数据库升级完成！comment 表已支持统一评论系统。' AS message;