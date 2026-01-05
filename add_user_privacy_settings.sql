-- 添加用户隐私设置字段
USE blog_db;

-- 添加点赞列表公开设置
ALTER TABLE users
ADD COLUMN likes_public BOOLEAN DEFAULT TRUE COMMENT '点赞列表是否公开';

-- 添加收藏列表公开设置
ALTER TABLE users
ADD COLUMN favorites_public BOOLEAN DEFAULT TRUE COMMENT '收藏列表是否公开';

-- 更新现有用户的默认值
UPDATE users SET likes_public = TRUE WHERE likes_public IS NULL;

UPDATE users
SET
    favorites_public = TRUE
WHERE
    favorites_public IS NULL;

SELECT '用户隐私设置字段添加完成！' AS message;