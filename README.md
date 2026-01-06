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

## ☁️ 云服务器部署

### 环境准备

在云服务器上安装以下软件：

1. **安装 JDK 17+**
   ```bash
   # Ubuntu/Debian
   sudo apt update
   sudo apt install openjdk-17-jdk -y
   
   # CentOS/RHEL
   sudo yum install java-17-openjdk-devel -y
   ```

2. **安装 Maven**
   ```bash
   # Ubuntu/Debian
   sudo apt install maven -y
   
   # CentOS/RHEL
   sudo yum install maven -y
   ```

3. **安装 Node.js 16+**
   ```bash
   # 使用 NodeSource 仓库安装
   curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
   sudo apt install -y nodejs
   ```

4. **安装 MySQL 8.0+**
   ```bash
   # Ubuntu/Debian
   sudo apt install mysql-server -y
   sudo systemctl start mysql
   sudo systemctl enable mysql
   
   # CentOS/RHEL
   sudo yum install mysql-server -y
   sudo systemctl start mysqld
   sudo systemctl enable mysqld
   ```

5. **配置 MySQL**
   ```bash
   # 登录 MySQL
   sudo mysql -u root -p
   
   # 创建数据库和用户（在 MySQL 中执行）
   CREATE DATABASE blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'your_username'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON blog_db.* TO 'your_username'@'localhost';
   FLUSH PRIVILEGES;
   EXIT;
   ```

### 部署步骤

1. **克隆项目到服务器**
   ```bash
   git clone <your-repo-url>
   cd Blogs
   ```

2. **配置数据库连接**
   
   编辑 `src/main/resources/application.properties`，修改数据库连接信息：
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/blog_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **配置服务器端口和地址（可选）**
   
   如果需要修改端口或绑定地址，可以在 `application.properties` 中修改：
   ```properties
   server.port=8080
   # 绑定到所有网络接口，允许外部访问
   # server.address=0.0.0.0
   ```
   
   或者通过环境变量在启动脚本中设置（见下方）。

4. **运行部署脚本**
   ```bash
   # 赋予执行权限
   chmod +x deploy.sh
   
   # 方式一：使用默认配置运行
   ./deploy.sh
   
   # 方式二：通过环境变量自定义配置
   DB_HOST=localhost \
   DB_PORT=3306 \
   DB_NAME=blog_db \
   DB_USER=your_username \
   DB_PASS=your_password \
   SERVER_PORT=8080 \
   SERVER_HOST=0.0.0.0 \
   ./deploy.sh
   ```

   脚本会自动完成：
   - ✅ 检查运行环境（Java、Maven、Node.js、MySQL）
   - ✅ 初始化数据库（执行 `init.sql`）
   - ✅ 构建前端项目（`npm run build`）
   - ✅ 打包后端项目（`mvn clean package`）
   - ✅ 启动后端服务（后台运行）

5. **配置防火墙**

   **Ubuntu/Debian (ufw):**
   ```bash
   sudo ufw allow 8080/tcp
   sudo ufw reload
   ```

   **CentOS/RHEL (firewalld):**
   ```bash
   sudo firewall-cmd --permanent --add-port=8080/tcp
   sudo firewall-cmd --reload
   ```

   **云服务商安全组：**
   - 登录云服务商控制台
   - 找到服务器对应的安全组
   - 添加入站规则：端口 `8080`，协议 `TCP`，源 `0.0.0.0/0`（或指定 IP）

6. **访问应用**

   部署成功后，通过以下地址访问：
   - **前端页面**: `http://your-server-ip:8080`
   - **API 接口**: `http://your-server-ip:8080/api`
   - **默认管理员**: `admin` / `123456`

### 服务管理

- **查看日志**:
  ```bash
  tail -f backend.log
  ```

- **停止服务**:
  ```bash
  kill $(cat backend.pid)
  ```

- **重启服务**:
  ```bash
  kill $(cat backend.pid)
  等待几秒后
  ./deploy.sh
  ```

### 使用 Nginx 反向代理（推荐）

为了更好的性能和安全性，建议使用 Nginx 作为反向代理：

1. **安装 Nginx**
   ```bash
   sudo apt install nginx -y  # Ubuntu/Debian
   # 或
   sudo yum install nginx -y  # CentOS/RHEL
   ```

2. **配置 Nginx**
   
   创建配置文件 `/etc/nginx/sites-available/blogs`（Ubuntu/Debian）或 `/etc/nginx/conf.d/blogs.conf`（CentOS/RHEL）：
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;  # 替换为你的域名或 IP
       
       # 前端静态资源
       location / {
           root /path/to/Blogs/src/main/resources/static;
           try_files $uri $uri/ /index.html;
       }
       
       # 后端 API 代理
       location /api {
           proxy_pass http://127.0.0.1:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
       
       # 文件上传路径
       location /uploads {
           alias /path/to/Blogs/uploads;
       }
   }
   ```

3. **启用配置并重启 Nginx**
   ```bash
   # Ubuntu/Debian
   sudo ln -s /etc/nginx/sites-available/blogs /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl restart nginx
   
   # CentOS/RHEL
   sudo nginx -t
   sudo systemctl restart nginx
   ```

4. **配置防火墙开放 80 端口**
   ```bash
   sudo ufw allow 80/tcp  # Ubuntu/Debian
   # 或
   sudo firewall-cmd --permanent --add-port=80/tcp  # CentOS/RHEL
   ```

   现在可以通过 `http://your-domain.com` 访问应用。

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


## 📄 许可证
MIT License
