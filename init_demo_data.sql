-- =============================================
-- 博客系统示例数据初始化脚本
-- =============================================

USE blog_db;

-- =============================================
-- 添加示例用户数据
-- =============================================
INSERT INTO
    users (
        username,
        password,
        nickname,
        bio,
        role,
        status,
        created_at
    )
VALUES (
        'alice',
        '$2b$12$AyzmbsXTz6URch/o5XcMoOFvrQUHJAgf5MXxW/9tjyj78FyhI/qBi',
        '爱丽丝',
        '前端开发工程师，热爱分享技术经验',
        'USER',
        1,
        NOW()
    ),
    (
        'bob',
        '$2b$12$AyzmbsXTz6URch/o5XcMoOFvrQUHJAgf5MXxW/9tjyj78FyhI/qBi',
        '鲍勃',
        '后端开发工程师，专注于系统架构设计',
        'USER',
        1,
        NOW()
    ),
    (
        'charlie',
        '$2b$12$AyzmbsXTz6URch/o5XcMoOFvrQUHJAgf5MXxW/9tjyj78FyhI/qBi',
        '查理',
        '全栈开发工程师，喜欢探索新技术',
        'USER',
        1,
        NOW()
    ),
    (
        'diana',
        '$2b$12$AyzmbsXTz6URch/o5XcMoOFvrQUHJAgf5MXxW/9tjyj78FyhI/qBi',
        '戴安娜',
        'UI/UX设计师，专注于用户体验',
        'USER',
        1,
        NOW()
    )
ON DUPLICATE KEY UPDATE
    username = username;

-- =============================================
-- 添加示例分类
-- =============================================
INSERT INTO
    category (
        name,
        description,
        sort_order,
        created_at
    )
VALUES (
        '系统介绍',
        '关于本博客系统的介绍',
        1,
        NOW()
    ),
    ('技术分享', '技术文章分享', 2, NOW()),
    ('生活随笔', '生活感悟与随笔', 3, NOW()),
    ('前端开发', '前端技术分享', 4, NOW()),
    ('后端开发', '后端技术分享', 5, NOW()),
    ('设计美学', '设计相关文章', 6, NOW())
ON DUPLICATE KEY UPDATE
    name = name;

-- =============================================
-- 添加示例标签
-- =============================================
INSERT INTO
    tag (name, color, created_at)
VALUES ('系统介绍', '#409eff', NOW()),
    ('博客系统', '#67c23a', NOW()),
    ('论坛', '#e6a23c', NOW()),
    ('全栈', '#f56c6c', NOW()),
    ('Vue.js', '#409eff', NOW()),
    (
        'Spring Boot',
        '#67c23a',
        NOW()
    ),
    (
        'Element Plus',
        '#e6a23c',
        NOW()
    ),
    ('Java', '#f56c6c', NOW()),
    (
        'JavaScript',
        '#909399',
        NOW()
    ),
    ('前端', '#409eff', NOW()),
    ('后端', '#67c23a', NOW()),
    ('开发', '#e6a23c', NOW())
ON DUPLICATE KEY UPDATE
    name = name;

-- =============================================
-- 添加系统介绍文章
-- =============================================
INSERT INTO
    article (
        user_id,
        title,
        content,
        summary,
        status,
        is_top,
        view_count,
        like_count,
        comment_count,
        category_id,
        created_at,
        published_at
    )
VALUES (
        1,
        '欢迎使用博客+论坛一体化系统',
        '<h2>系统概述</h2><p>这是一个功能丰富的博客+论坛一体化系统，集成了博客发布、论坛讨论、用户管理等功能。</p><h3>主要特性</h3><ul><li>博客功能：支持文章发布、编辑、分类、标签管理</li><li>论坛功能：支持板块划分、帖子发布、回帖讨论</li><li>用户系统：支持用户注册、登录、权限管理</li><li>内容管理：支持文章审核、评论管理、数据统计</li><li>响应式设计：支持PC端和移动端访问</li></ul><h3>技术栈</h3><p>前端：Vue 3 + Element Plus + Pinia<br/>后端：Spring Boot + MySQL + JWT</p><h3>使用说明</h3><p>注册用户后即可开始发布博客文章或参与论坛讨论。管理员账号可以管理内容和用户。</p>',
        '这是一个功能丰富的博客+论坛一体化系统，集成了博客发布、论坛讨论、用户管理等功能。',
        1,
        1,
        100,
        5,
        2,
        1,
        NOW(),
        NOW()
    ),
    (
        2,
        'Vue.js 3.0 新特性解析',
        '<h2>Vue.js 3.0 新特性</h2><p>Vue.js 3.0 是一个重要的版本更新，带来了许多新特性和性能改进。</p><h3>Composition API</h3><p>Composition API 是 Vue 3.0 中最引人注目的新特性之一，它允许我们更灵活地组织组件逻辑。</p><pre><code>import { ref, computed } from \'vue\'\n\nexport default {\n  setup() {\n    const count = ref(0)\n    const double = computed(() => count.value * 2)\n    \n    return {\n      count,\n      double\n    }\n  }\n}</code></pre><h3>性能改进</h3><p>Vue 3.0 在性能方面有显著提升，包括更快的渲染、更小的打包体积等。</p>',
        'Vue.js 3.0 是一个重要的版本更新，带来了许多新特性和性能改进。',
        1,
        0,
        85,
        3,
        1,
        4,
        NOW(),
        NOW()
    ),
    (
        3,
        'Spring Boot 最佳实践',
        '<h2>Spring Boot 最佳实践</h2><p>Spring Boot 是一个基于 Spring 框架的快速开发框架，它简化了 Spring 应用的搭建和部署过程。</p><h3>配置管理</h3><p>使用 application.properties 或 application.yml 文件进行配置管理。</p><pre><code>server:\n  port: 8080\n\nspring:\n  datasource:\n    url: jdbc:mysql://localhost:3306/blog_db\n    username: root\n    password: password</code></pre><h3>安全配置</h3><p>使用 Spring Security 进行安全配置，保护应用免受攻击。</p>',
        'Spring Boot 是一个基于 Spring 框架的快速开发框架，它简化了 Spring 应用的搭建和部署过程。',
        1,
        0,
        72,
        4,
        0,
        5,
        NOW(),
        NOW()
    ),
    (
        4,
        '前端开发趋势分析',
        '<h2>2024年前端开发趋势</h2><p>前端开发技术日新月异，了解当前趋势对开发者至关重要。</p><h3>框架选择</h3><p>React、Vue.js 和 Angular 仍然是主流框架，但 Svelte 和 Solid.js 等新兴框架也值得关注。</p><h3>构建工具</h3><p>Vite 作为新兴构建工具，凭借其快速的启动和热更新功能，正在挑战 Webpack 的地位。</p><h3>Web Components</h3><p>Web Components 标准正在得到更多浏览器支持，提供了一种原生的组件化方案。</p>',
        '前端开发技术日新月异，了解当前趋势对开发者至关重要。',
        1,
        0,
        65,
        2,
        1,
        4,
        NOW(),
        NOW()
    ),
    (
        1,
        '用户体验设计原则',
        '<h2>用户体验设计原则</h2><p>良好的用户体验是产品成功的关键因素之一。以下是一些重要的设计原则：</p><h3>简洁性</h3><p>界面应该简洁明了，避免不必要的复杂性。用户应该能够快速找到他们需要的功能。</p><h3>一致性</h3><p>保持设计元素的一致性，包括颜色、字体、按钮样式等，有助于用户建立心理模型。</p><h3>可用性</h3><p>产品应该易于使用，用户不需要阅读复杂的使用手册就能上手操作。</p>',
        '良好的用户体验是产品成功的关键因素之一。以下是一些重要的设计原则。',
        1,
        0,
        48,
        1,
        0,
        6,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE
    title = title;

-- =============================================
-- 获取分类ID用于关联
-- =============================================
SET @sys_cat_id = ( SELECT id FROM category WHERE name = '系统介绍' );

SET @tech_cat_id = ( SELECT id FROM category WHERE name = '技术分享' );

SET @life_cat_id = ( SELECT id FROM category WHERE name = '生活随笔' );

SET @front_cat_id = ( SELECT id FROM category WHERE name = '前端开发' );

SET @back_cat_id = ( SELECT id FROM category WHERE name = '后端开发' );

SET @design_cat_id = ( SELECT id FROM category WHERE name = '设计美学' );

-- =============================================
-- 关联文章和标签
-- =============================================
INSERT INTO
    article_tag (article_id, tag_id)
SELECT 1, id
FROM tag
WHERE
    name = '系统介绍'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

INSERT INTO
    article_tag (article_id, tag_id)
SELECT 1, id
FROM tag
WHERE
    name = '博客系统'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

INSERT INTO
    article_tag (article_id, tag_id)
SELECT 1, id
FROM tag
WHERE
    name = '论坛'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

INSERT INTO
    article_tag (article_id, tag_id)
SELECT 1, id
FROM tag
WHERE
    name = '全栈'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

INSERT INTO
    article_tag (article_id, tag_id)
SELECT 2, id
FROM tag
WHERE
    name = 'Vue.js'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

INSERT INTO
    article_tag (article_id, tag_id)
SELECT 2, id
FROM tag
WHERE
    name = '前端'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

INSERT INTO
    article_tag (article_id, tag_id)
SELECT 2, id
FROM tag
WHERE
    name = '开发'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

INSERT INTO
    article_tag (article_id, tag_id)
SELECT 3, id
FROM tag
WHERE
    name = 'Spring Boot'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

INSERT INTO
    article_tag (article_id, tag_id)
SELECT 3, id
FROM tag
WHERE
    name = 'Java'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

INSERT INTO
    article_tag (article_id, tag_id)
SELECT 3, id
FROM tag
WHERE
    name = '后端'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

INSERT INTO
    article_tag (article_id, tag_id)
SELECT 4, id
FROM tag
WHERE
    name = '前端'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

INSERT INTO
    article_tag (article_id, tag_id)
SELECT 4, id
FROM tag
WHERE
    name = '开发'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

INSERT INTO
    article_tag (article_id, tag_id)
SELECT 5, id
FROM tag
WHERE
    name = '设计美学'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

INSERT INTO
    article_tag (article_id, tag_id)
SELECT 5, id
FROM tag
WHERE
    name = '前端'
ON DUPLICATE KEY UPDATE
    tag_id = tag_id;

-- =============================================
-- 添加示例评论
-- =============================================
INSERT INTO
    comment (
        article_id,
        user_id,
        content,
        nickname,
        avatar,
        status,
        created_at
    )
VALUES (
        1,
        2,
        '这个系统看起来很棒，期待更多功能！',
        '鲍勃',
        NULL,
        1,
        NOW()
    ),
    (
        1,
        3,
        '界面设计很现代化，用户体验应该不错。',
        '查理',
        NULL,
        1,
        NOW() - INTERVAL 1 DAY
    ),
    (
        2,
        4,
        'Composition API 确实让代码更清晰了。',
        '戴安娜',
        NULL,
        1,
        NOW() - INTERVAL 2 DAY
    ),
    (
        4,
        1,
        'Vite 的确比 Webpack 快很多，推荐大家试试。',
        '爱丽丝',
        NULL,
        1,
        NOW() - INTERVAL 1 DAY
    )
ON DUPLICATE KEY UPDATE
    content = content;

-- =============================================
-- 添加论坛板块
-- =============================================
INSERT INTO
    forum_section (
        name,
        description,
        icon,
        sort_order,
        status,
        created_at
    )
VALUES (
        '技术交流',
        '技术问题讨论、经验分享',
        'code',
        1,
        1,
        NOW()
    ),
    (
        '项目展示',
        '展示个人或团队项目',
        'lightning',
        2,
        1,
        NOW()
    ),
    (
        '学习笔记',
        '学习心得和笔记分享',
        'document',
        3,
        1,
        NOW()
    ),
    (
        '资源分享',
        '分享优质学习资源和工具',
        'share',
        4,
        1,
        NOW()
    ),
    (
        '问题求助',
        '遇到问题？这里寻求帮助',
        'question',
        5,
        1,
        NOW()
    ),
    (
        '闲聊灌水',
        '非技术话题讨论',
        'chat',
        6,
        1,
        NOW()
    )
ON DUPLICATE KEY UPDATE
    name = name;

-- =============================================
-- 添加论坛帖子
-- =============================================
INSERT INTO
    forum_post (
        section_id,
        user_id,
        title,
        content,
        status,
        view_count,
        like_count,
        comment_count,
        created_at
    )
VALUES (
        1,
        2,
        '如何在项目中使用 Composition API',
        '最近在项目中尝试使用 Composition API，感觉比 Options API 更灵活，但也有学习成本。大家有什么使用心得吗？',
        1,
        25,
        2,
        1,
        NOW()
    ),
    (
        2,
        3,
        '我的个人作品集网站',
        '最近完成了个人作品集网站的开发，使用了 Vue 3 + Vite + TailwindCSS，欢迎大家访问并提建议！',
        1,
        18,
        3,
        0,
        NOW() - INTERVAL 1 DAY
    ),
    (
        4,
        4,
        '周末爬山活动，有人一起吗？',
        '计划这个周末去爬山，呼吸新鲜空气，远离代码。有人有兴趣一起吗？',
        1,
        12,
        0,
        0,
        NOW() - INTERVAL 2 DAY
    )
ON DUPLICATE KEY UPDATE
    title = title;

-- =============================================
-- 添加论坛回帖
-- =============================================
INSERT INTO
    forum_post_comment (
        post_id,
        user_id,
        content,
        created_at
    )
VALUES (
        1,
        1,
        '确实，Composition API 在处理复杂逻辑时更有优势，特别是逻辑复用方面。',
        NOW()
    ),
    (
        2,
        2,
        '听起来不错，能分享一下链接吗？想看看具体实现。',
        NOW() - INTERVAL 1 DAY
    )
ON DUPLICATE KEY UPDATE
    content = content;

-- =============================================
-- 添加友链
-- =============================================
INSERT INTO
    friend_link (
        name,
        url,
        description,
        sort_order,
        is_visible,
        created_at
    )
VALUES (
        'Vue.js 官网',
        'https://vuejs.org/',
        '渐进式 JavaScript 框架',
        1,
        1,
        NOW()
    ),
    (
        'Element Plus',
        'https://element-plus.org/',
        '基于 Vue 3 的组件库',
        2,
        1,
        NOW()
    ),
    (
        'Spring 官网',
        'https://spring.io/',
        '企业级应用开发框架',
        3,
        1,
        NOW()
    )
ON DUPLICATE KEY UPDATE
    name = name;

-- =============================================
-- 更新分类文章数
-- =============================================
UPDATE category c
SET
    c.article_count = (
        SELECT COUNT(*)
        FROM article a
        WHERE
            a.category_id = c.id
    )
WHERE
    c.id IN (
        SELECT DISTINCT
            category_id
        FROM article
    );

SELECT '示例数据初始化完成！' AS message;