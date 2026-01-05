# Blogs - Spring Boot + Vue 3 博客系统

一个现代化、简洁且功能完善的博客与论坛系统，基于 Spring Boot 3 和 Vue 3 开发。

GitHub 仓库: [https://github.com/ShikaC/BlogSystem](https://github.com/ShikaC/BlogSystem)

## ✨ 特性

- **📝 文章发布**：支持 Markdown 编辑、实时预览、封面上传、标签分类管理。
- **💬 社区互动**：内置论坛板块，支持发帖、回帖、富文本编辑器、点赞、收藏。
- **🔍 全文搜索**：支持对文章标题和内容的快速检索。
- **👤 用户中心**：用户注册/登录、个人资料修改、我的文章/帖子/收藏管理。
- **🛡️ 权限管理**：基于 Spring Security + JWT 的安全认证，区分普通用户与管理员。
- **📱 响应式设计**：适配 PC 与移动端，提供极致的阅读体验。
- **🎨 现代 UI**：使用 Element Plus 组件库，界面美观大方，支持夜间模式（自动跟随系统或手动切换）。

## 🛠️ 技术栈

### 后端
- **核心框架**：Spring Boot 3.x
- **数据库**：MySQL 8.0
- **持久层**：Spring Data JPA
- **安全安全**：Spring Security + JWT
- **工具库**：Lombok, Hutool

### 前端
- **框架**：Vue 3 (Composition API)
- **构建工具**：Vite
- **路由管理**：Vue Router 4
- **状态管理**：Pinia
- **UI 组件库**：Element Plus
- **HTTP 客户端**：Axios

## 🚀 快速开始

### 环境要求
- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 1. 数据库配置
1. 创建数据库 `blog_db`。
2. 导入项目根目录下的 `sql/init.sql`（如果有）或者让 JPA 自动生成表结构。
3. 修改 `src/main/resources/application.properties` 中的数据库连接信息：
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/blog_db?useSSL=false&serverTimezone=Asia/Shanghai
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

### 2. 启动项目

#### 方式一：使用一键启动脚本 (Windows)
在项目根目录下，右键使用 PowerShell 运行 `run.ps1` 脚本：
```powershell
.\run.ps1
```
该脚本会自动：
1. 检查各端口占用情况。
2. 启动 Spring Boot 后端服务 (端口 8080)。
3. 安装前端依赖（首次运行）。
4. 启动 Vue 前端开发服务器 (端口 3000)。

#### 方式二：手动启动

**启动后端**：
```bash
mvn spring-boot:run
```
后端服务地址：`http://localhost:8080`

**启动前端**：
```bash
cd blog-frontend
npm install
npm run dev
```
前端访问地址：`http://localhost:3000`

## 📂 项目结构

```
Blogs/
├── src/main/java/com/blogs/    # 后端源代码
│   ├── config/                 # 配置类 (Security, WebMvc等)
│   ├── controller/             # 控制器 (API 接口)
│   ├── entity/                 # 实体类 (数据库表映射)
│   ├── repository/             # 数据访问层 (JPA)
│   ├── service/                # 业务逻辑层
│   └── utils/                  # 工具类
├── src/main/resources/         # 后端资源
│   ├── application.properties  # 配置文件
│   └── static/                 # 静态资源
├── blog-frontend/              # 前端项目
│   ├── src/
│   │   ├── api/                # API 接口封装
│   │   ├── assets/             # 静态资源 (图片, 样式)
│   │   ├── components/         # 公共组件
│   │   ├── views/              # 页面视图
│   │   ├── stores/             # Pinia 状态管理
│   │   └── utils/              # 前端工具库
│   ├── index.html              # 入口 HTML
│   └── vite.config.js          # Vite 配置
└── run.ps1                     # 一键启动脚本
```

## 📝 开发计划
- [ ] 增加第三方登录 (GitHub, Google)
- [ ] 完善消息通知系统
- [ ] 优化 SEO 支持
- [ ] 增加更多自定义主题

## 📄 许可证
MIT License
