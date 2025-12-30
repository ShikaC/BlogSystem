# 轻量化个人博客系统

基于 SpringBoot + Vue3 + MySQL 的单博主个人博客系统。

## 技术栈

- **后端**: SpringBoot 3.5.9 + Spring Data JPA + Spring Security + JWT + MySQL
- **前端**: Vue3 + Vite + Pinia + Vue Router + Element Plus

## 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

## 快速开始

### 1. 数据库配置

请确保已安装 MySQL 8.0+，并执行 `init.sql` 脚本初始化数据库：

```bash
# 登录 MySQL
mysql -u root -p

# 创建并使用数据库
CREATE DATABASE blog_db;
USE blog_db;

# 执行 SQL 初始化脚本
source C:/Users/Shikaa/Desktop/Java/Blogs/init.sql
```

或者手动执行 `init.sql` 文件中的 SQL 语句。

### 2. 修改数据库连接配置

编辑 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/blog_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=shika
spring.datasource.password=123456
```

### 3. 一键启动 (推荐)

项目根目录下提供了一个 PowerShell 脚本 `run.ps1`，可以一键安装前端依赖并同时启动后端与前端服务：

```powershell
./run.ps1
```

### 4. 手动启动

#### 启动后端服务

```bash
mvn spring-boot:run
```

后端将在 http://localhost:8080/api 启动。

#### 启动前端服务

```bash
cd blog-frontend
npm install
npm run dev
```

前端将在 http://localhost:3000 启动。

### 5. 初始化博主账号

首次访问系统时，进入登录页面会提示初始化博主账号：

1. 访问 http://localhost:3000/login
2. 点击「初始化博主账号」
3. 设置用户名和密码
4. 登录后台开始使用

## 项目结构

```
Blogs/
├── src/main/java/com/blogs/     # 后端源码
│   ├── controller/              # 控制器
│   │   ├── admin/               # 后台管理接口
│   │   └── front/               # 前台展示接口
│   ├── entity/                  # 实体类
│   ├── repository/              # 数据访问层
│   ├── service/                 # 业务逻辑层
│   ├── dto/                     # 数据传输对象
│   ├── config/                  # 配置类
│   ├── security/                # 安全相关
│   └── common/                  # 公共类
├── src/main/resources/
│   └── application.properties   # 配置文件
├── blog-frontend/               # 前端项目
├── run.ps1                      # 一键启动脚本 (Windows)
├── uploads/                     # 上传文件目录
├── init.sql                     # 数据库初始化脚本
└── pom.xml                      # Maven 配置
```

## 功能模块

### 博主后台

- **仪表盘**: 数据统计、快捷操作、数据备份导出
- **文章管理**: 发布/编辑/预览、草稿箱、回收站、置顶
- **分类管理**: 增删改查
- **标签管理**: 增删改查
- **评论管理**: 审核、回复、删除
- **媒体库**: 图片上传管理
- **友情链接**: 增删改查
- **系统设置**: 站点配置、SEO 设置
- **个人信息**: 修改昵称、头像、密码

### 前台展示

- **首页**: 文章列表、侧边栏（分类、标签云、热门文章）
- **文章详情**: 内容展示、目录导航、互动（点赞/收藏/评论）
- **分类归档**: 按分类查看文章
- **标签归档**: 按标签查看文章
- **时间归档**: 按年月查看文章
- **搜索**: 关键词搜索文章
- **关于页面**: 博主介绍
- **友链页面**: 友情链接展示

## 默认账号

首次使用需要初始化博主账号，没有默认账号。

## 常见问题

### 1. 数据库连接失败

确保 MySQL 服务已启动，且 `blog_db` 数据库已创建，并根据实际情况修改 `application.properties` 中的用户名和密码。

### 2. 前端接口 404

检查后端服务是否正常启动，确保运行在 8080 端口。

### 3. 上传文件失败

确保项目根目录下存在 `uploads` 文件夹，且有写入权限。`run.ps1` 脚本会自动尝试创建此文件夹。
