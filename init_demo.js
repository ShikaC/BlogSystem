/**
 * 博客系统演示数据初始化脚本
 * 该脚本将创建示例文章和用户来模拟一个初步的博客主页
 */

const axios = require('axios');

// API 基础URL
const BASE_URL = 'http://localhost:8080/api';

// 管理员登录凭据
const ADMIN_CREDENTIALS = {
  username: 'admin',
  password: '123456'
};

// 演示用户数据
const demoUsers = [
  {
    username: 'alice',
    password: '123456',
    nickname: '爱丽丝'
  },
  {
    username: 'bob',
    password: '123456',
    nickname: '鲍勃'
  },
  {
    username: 'charlie',
    password: '123456',
    nickname: '查理'
  },
  {
    username: 'diana',
    password: '123456',
    nickname: '戴安娜'
  }
];

// 演示文章数据
const demoArticles = [
  {
    title: '欢迎使用博客+论坛一体化系统',
    content: '<h2>系统概述</h2><p>这是一个功能丰富的博客+论坛一体化系统，集成了博客发布、论坛讨论、用户管理等功能。</p><h3>主要特性</h3><ul><li>博客功能：支持文章发布、编辑、分类、标签管理</li><li>论坛功能：支持板块划分、帖子发布、回帖讨论</li><li>用户系统：支持用户注册、登录、权限管理</li><li>内容管理：支持文章审核、评论管理、数据统计</li><li>响应式设计：支持PC端和移动端访问</li></ul><h3>技术栈</h3><p>前端：Vue 3 + Element Plus + Pinia<br/>后端：Spring Boot + MySQL + JWT</p><h3>使用说明</h3><p>注册用户后即可开始发布博客文章或参与论坛讨论。管理员账号可以管理内容和用户。</p>',
    summary: '这是一个功能丰富的博客+论坛一体化系统，集成了博客发布、论坛讨论、用户管理等功能。',
    status: 1,
    isTop: true,
    categoryId: 1
  },
  {
    title: 'Vue.js 3.0 新特性解析',
    content: '<h2>Vue.js 3.0 新特性</h2><p>Vue.js 3.0 是一个重要的版本更新，带来了许多新特性和性能改进。</p><h3>Composition API</h3><p>Composition API 是 Vue 3.0 中最引人注目的新特性之一，它允许我们更灵活地组织组件逻辑。</p><pre><code>import { ref, computed } from \'vue\'\n\nexport default {\n  setup() {\n    const count = ref(0)\n    const double = computed(() => count.value * 2)\n    \n    return {\n      count,\n      double\n    }\n  }\n}</code></pre><h3>性能改进</h3><p>Vue 3.0 在性能方面有显著提升，包括更快的渲染、更小的打包体积等。</p>',
    summary: 'Vue.js 3.0 是一个重要的版本更新，带来了许多新特性和性能改进。',
    status: 1,
    isTop: false,
    categoryId: 2
  },
  {
    title: 'Spring Boot 最佳实践',
    content: '<h2>Spring Boot 最佳实践</h2><p>Spring Boot 是一个基于 Spring 框架的快速开发框架，它简化了 Spring 应用的搭建和部署过程。</p><h3>配置管理</h3><p>使用 application.properties 或 application.yml 文件进行配置管理。</p><pre><code>server:\n  port: 8080\n\nspring:\n  datasource:\n    url: jdbc:mysql://localhost:3306/blog_db\n    username: root\n    password: password</code></pre><h3>安全配置</h3><p>使用 Spring Security 进行安全配置，保护应用免受攻击。</p>',
    summary: 'Spring Boot 是一个基于 Spring 框架的快速开发框架，它简化了 Spring 应用的搭建和部署过程。',
    status: 1,
    isTop: false,
    categoryId: 3
  },
  {
    title: '前端开发趋势分析',
    content: '<h2>2024年前端开发趋势</h2><p>前端开发技术日新月异，了解当前趋势对开发者至关重要。</p><h3>框架选择</h3><p>React、Vue.js 和 Angular 仍然是主流框架，但 Svelte 和 Solid.js 等新兴框架也值得关注。</p><h3>构建工具</h3><p>Vite 作为新兴构建工具，凭借其快速的启动和热更新功能，正在挑战 Webpack 的地位。</p><h3>Web Components</h3><p>Web Components 标准正在得到更多浏览器支持，提供了一种原生的组件化方案。</p>',
    summary: '前端开发技术日新月异，了解当前趋势对开发者至关重要。',
    status: 1,
    isTop: false,
    categoryId: 2
  }
];

// 演示分类数据
const demoCategories = [
  { name: '系统介绍', description: '关于本博客系统的介绍' },
  { name: '技术分享', description: '技术文章分享' },
  { name: '生活随笔', description: '生活感悟与随笔' }
];

// 演示标签数据
const demoTags = [
  { name: '系统介绍' },
  { name: '博客系统' },
  { name: '论坛' },
  { name: '全栈' },
  { name: 'Vue.js' },
  { name: 'Spring Boot' },
  { name: 'Java' },
  { name: '前端' },
  { name: '后端' }
];

// 存储认证令牌
let authToken = null;

/**
 * 登录管理员账户
 */
async function loginAdmin() {
  try {
    console.log('正在登录管理员账户...');
    const response = await axios.post(`${BASE_URL}/auth/login`, ADMIN_CREDENTIALS);
    
    if (response.data.code === 200) {
      authToken = response.data.data.token;
      console.log('管理员登录成功');
      return true;
    } else {
      console.error('管理员登录失败:', response.data.message);
      return false;
    }
  } catch (error) {
    console.error('登录请求失败:', error.message);
    return false;
  }
}

/**
 * 创建演示分类
 */
async function createDemoCategories() {
  console.log('正在创建演示分类...');
  
  for (const category of demoCategories) {
    try {
      const response = await axios.post(
        `${BASE_URL}/admin/categories`,
        null,
        {
          params: {
            name: category.name,
            description: category.description
          },
          headers: {
            'Authorization': `Bearer ${authToken}`
          }
        }
      );
      
      console.log(`分类 "${category.name}" 创建成功`);
    } catch (error) {
      console.error(`创建分类 "${category.name}" 失败:`, error.message);
    }
  }
}

/**
 * 创建演示标签
 */
async function createDemoTags() {
  console.log('正在创建演示标签...');
  
  for (const tag of demoTags) {
    try {
      const response = await axios.post(
        `${BASE_URL}/admin/tags`,
        null,
        {
          params: {
            name: tag.name
          },
          headers: {
            'Authorization': `Bearer ${authToken}`
          }
        }
      );
      
      console.log(`标签 "${tag.name}" 创建成功`);
    } catch (error) {
      console.error(`创建标签 "${tag.name}" 失败:`, error.message);
    }
  }
}

/**
 * 注册演示用户
 */
async function registerDemoUsers() {
  console.log('正在注册演示用户...');
  
  for (const user of demoUsers) {
    try {
      const response = await axios.post(`${BASE_URL}/auth/register`, null, {
        params: {
          username: user.username,
          password: user.password,
          nickname: user.nickname
        }
      });
      
      console.log(`用户 "${user.username}" 注册成功`);
    } catch (error) {
      console.error(`注册用户 "${user.username}" 失败:`, error.message);
    }
  }
}

/**
 * 创建演示文章
 */
async function createDemoArticles() {
  console.log('正在创建演示文章...');
  
  for (const article of demoArticles) {
    try {
      const response = await axios.post(
        `${BASE_URL}/admin/articles`,
        article,
        {
          headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
          }
        }
      );
      
      console.log(`文章 "${article.title}" 创建成功`);
    } catch (error) {
      console.error(`创建文章 "${article.title}" 失败:`, error.message);
    }
  }
}

/**
 * 初始化演示数据
 */
async function initDemoData() {
  console.log('开始初始化博客系统演示数据...');
  
  // 登录管理员
  const loginSuccess = await loginAdmin();
  if (!loginSuccess) {
    console.error('无法登录管理员账户，初始化失败');
    return;
  }
  
  // 创建分类
  await createDemoCategories();
  
  // 创建标签
  await createDemoTags();
  
  // 注册演示用户
  await registerDemoUsers();
  
  // 创建演示文章
  await createDemoArticles();
  
  console.log('演示数据初始化完成！');
  console.log('现在您可以访问博客系统查看示例内容。');
  console.log('管理员账户: admin / 123456');
  console.log('演示用户: alice/bob/charlie/diana 密码都是 123456');
}

// 运行初始化
if (require.main === module) {
  initDemoData().catch(error => {
    console.error('初始化过程中发生错误:', error);
  });
}

module.exports = {
  initDemoData,
  loginAdmin
};