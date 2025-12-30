# Blogs - 博客 + 论坛一体化综合系统

这是一个基于 **Spring Boot 3** 和 **Vue 3** 开发的现代博客与论坛一体化系统。它提供了丰富的功能，包括 Markdown 文章发布、分类标签管理、嵌套评论、论坛交流、消息通知、点赞收藏以及完善的后台管理系统。

## 🌟 项目亮点

- **一体化体验**：深度整合博客系统与论坛社区，数据互通，体验顺滑。
- **现代化技术栈**：后端采用 Spring Boot 3.5 + JPA + Security + JWT，前端采用 Vue 3 (Composition API) + Vite + Pinia + Element Plus。
- **全功能编辑器**：支持 Markdown 语法，实时预览，图片上传管理。
- **互动性强**：支持文章/帖子的点赞、收藏、多级回复、系统消息通知。
- **响应式设计**：前端界面简洁现代，适配不同屏幕尺寸。
- **SEO 友好**：支持自定义文章 SEO 标题、关键词和描述。

## 🛠️ 技术栈

### 后端 (Backend)
- **核心框架**：Spring Boot 3.5.9
- **权限安全**：Spring Security + JJWT (JSON Web Token)
- **数据持久化**：Spring Data JPA + Hibernate
- **数据库**：MySQL 8.0+
- **工具类库**：Hutool, Lombok, Commons IO
- **Markdown 处理**：Flexmark

### 前端 (Frontend)
- **框架**：Vue 3.5 (Composition API)
- **构建工具**：Vite 7.2
- **UI 组件库**：Element Plus
- **状态管理**：Pinia
- **路由**：Vue Router 4
- **网络请求**：Axios
- **代码高亮**：Highlight.js
- **Markdown 渲染**：Marked

## 📂 项目结构

```text
Blogs/
├── blog-frontend/          # 前端 Vue 项目
│   ├── src/                # 前端源码
│   └── vite.config.js      # Vite 配置
├── src/                    # 后端 Java 项目
│   ├── main/java/          # 业务逻辑
│   └── main/resources/     # 配置文件
├── init.sql                # 数据库初始化脚本
├── run.ps1                 # Windows 一键启动脚本
├── uploads/                # 文件上传存储目录
└── pom.xml                 # Maven 项目配置
```

## 🚀 快速开始

### 1. 环境准备
- **Java 17** 或更高版本
- **Node.js 18** 或更高版本
- **MySQL 8.0** 或更高版本
- **Maven 3.6+**

### 2. 数据库配置
1. 登录 MySQL，执行 `init.sql` 脚本：
   ```sql
   source your_path/init.sql;
   ```
2. 修改 `src/main/resources/application.properties` 中的数据库连接信息：
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### 3. 运行项目

#### 方式一：使用一键启动脚本 (Windows)
在项目根目录下运行 PowerShell 脚本：
```powershell
./run.ps1
```
该脚本会自动检查环境、安装依赖并同时启动前后端服务。

#### 方式二：手动运行
**启动后端：**
```bash
mvn spring-boot:run
```
后端 API 地址：`http://localhost:8080/api`

**启动前端：**
```bash
cd blog-frontend
npm install
npm run dev
```
前端访问地址：`http://localhost:3000` (通常 Vite 默认端口)

## 🔐 默认账号

- **用户名**：`admin`
- **密码**：`123456`
- **角色**：超级管理员 (ADMIN)

## 📝 主要功能模块

- **用户系统**：注册登录、个人资料修改（头像、社交链接）、密码重置。
- **博客模块**：文章发布/编辑、封面管理、分类与标签、草稿箱、置顶、回收站、访问密码。
- **论坛模块**：板块管理、帖子发布、精华帖设置、置顶管理。
- **互动系统**：嵌套评论（支持回复特定用户）、点赞、收藏。
- **通知中心**：收到的评论、回复、点赞、收藏及系统通知提醒。
- **后台管理**：仪表盘统计、文章/帖子/评论审核管理、分类标签管理、媒体文件管理、站点配置、友链管理。

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 协议。
