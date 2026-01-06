#!/bin/bash

# ============================================
# Blogs 项目 Linux 云服务器部署脚本
# ============================================
# 功能：
# 1. 检查运行环境（Java、Maven、Node.js、MySQL）
# 2. 初始化数据库
# 3. 构建前端项目
# 4. 打包后端项目
# 5. 启动后端服务
# ============================================

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 配置变量（可根据实际情况修改）
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-blog_db}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-}"
SERVER_PORT="${SERVER_PORT:-8080}"
SERVER_HOST="${SERVER_HOST:-0.0.0.0}"

echo -e "${CYAN}============================================${NC}"
echo -e "${CYAN}  Blogs 项目部署脚本${NC}"
echo -e "${CYAN}============================================${NC}"
echo ""

# ============================================
# 1. 环境检查
# ============================================
echo -e "${CYAN}[1/5] 检查运行环境...${NC}"

# 检查 Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}错误: 未找到 Java，请先安装 JDK 17 或更高版本${NC}"
    echo "安装示例: sudo apt install openjdk-17-jdk"
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${YELLOW}警告: Java 版本低于 17，建议升级${NC}"
else
    echo -e "${GREEN}✓ Java 版本检查通过 ($(java -version 2>&1 | head -n 1))${NC}"
fi

# 检查 Maven
if command -v mvn &> /dev/null; then
    MVN_CMD="mvn"
    echo -e "${GREEN}✓ Maven 检查通过${NC}"
elif [ -f "./mvnw" ]; then
    MVN_CMD="./mvnw"
    chmod +x ./mvnw
    echo -e "${GREEN}✓ 使用 Maven Wrapper${NC}"
else
    echo -e "${RED}错误: 未找到 Maven，请先安装 Maven 3.6+${NC}"
    echo "安装示例: sudo apt install maven"
    exit 1
fi

# 检查 Node.js
if ! command -v node &> /dev/null; then
    echo -e "${RED}错误: 未找到 Node.js，请先安装 Node.js 16+${NC}"
    echo "安装示例: curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash - && sudo apt install -y nodejs"
    exit 1
fi
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 16 ]; then
    echo -e "${YELLOW}警告: Node.js 版本低于 16，建议升级${NC}"
else
    echo -e "${GREEN}✓ Node.js 版本检查通过 ($(node -v))${NC}"
fi

# 检查 npm
if ! command -v npm &> /dev/null; then
    echo -e "${RED}错误: 未找到 npm${NC}"
    exit 1
fi
echo -e "${GREEN}✓ npm 检查通过 ($(npm -v))${NC}"

# 检查 MySQL
if ! command -v mysql &> /dev/null; then
    echo -e "${YELLOW}警告: 未找到 mysql 客户端，将跳过数据库初始化检查${NC}"
else
    echo -e "${GREEN}✓ MySQL 客户端检查通过${NC}"
fi

echo ""

# ============================================
# 2. 数据库初始化
# ============================================
echo -e "${CYAN}[2/5] 初始化数据库...${NC}"

if [ -f "init.sql" ]; then
    if command -v mysql &> /dev/null; then
        if [ -z "$DB_PASS" ]; then
            echo -e "${YELLOW}提示: 未设置 DB_PASS 环境变量，将尝试无密码连接${NC}"
            MYSQL_CMD="mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER}"
        else
            MYSQL_CMD="mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASS}"
        fi
        
        echo "正在创建数据库和表结构..."
        if $MYSQL_CMD < init.sql 2>/dev/null; then
            echo -e "${GREEN}✓ 数据库初始化成功${NC}"
        else
            echo -e "${YELLOW}警告: 数据库初始化可能失败，请手动检查${NC}"
            echo "可以手动执行: mysql -u${DB_USER} -p < init.sql"
        fi
    else
        echo -e "${YELLOW}提示: 未找到 mysql 客户端，请手动执行 init.sql 初始化数据库${NC}"
    fi
else
    echo -e "${YELLOW}提示: 未找到 init.sql，将依赖 JPA 自动创建表结构${NC}"
fi

echo ""

# ============================================
# 3. 构建前端项目
# ============================================
echo -e "${CYAN}[3/5] 构建前端项目...${NC}"

cd blog-frontend

# 安装依赖（如果 node_modules 不存在）
if [ ! -d "node_modules" ]; then
    echo "正在安装前端依赖..."
    npm install
fi

# 构建前端
echo "正在构建前端项目..."
npm run build

# 将构建产物复制到后端的 static 目录
cd ..
if [ -d "src/main/resources/static" ]; then
    rm -rf src/main/resources/static/*
else
    mkdir -p src/main/resources/static
fi
cp -r blog-frontend/dist/* src/main/resources/static/

echo -e "${GREEN}✓ 前端构建完成，已复制到后端 static 目录${NC}"
echo ""

# ============================================
# 4. 打包后端项目
# ============================================
echo -e "${CYAN}[4/5] 打包后端项目...${NC}"

# 创建上传目录
if [ ! -d "uploads" ]; then
    mkdir -p uploads
    echo -e "${GREEN}✓ 已创建 uploads 目录${NC}"
fi

# 清理并打包
echo "正在清理并打包..."
$MVN_CMD clean package -DskipTests

JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}错误: 未找到打包后的 jar 文件${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 后端打包完成: $JAR_FILE${NC}"
echo ""

# ============================================
# 5. 启动服务
# ============================================
echo -e "${CYAN}[5/5] 启动后端服务...${NC}"

# 检查端口是否被占用
if command -v lsof &> /dev/null; then
    if lsof -Pi :${SERVER_PORT} -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${YELLOW}警告: 端口 ${SERVER_PORT} 已被占用${NC}"
        read -p "是否要终止占用该端口的进程? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            lsof -ti:${SERVER_PORT} | xargs kill -9 2>/dev/null || true
            sleep 2
        else
            echo -e "${RED}请手动处理端口占用问题后重新运行脚本${NC}"
            exit 1
        fi
    fi
elif command -v netstat &> /dev/null; then
    if netstat -tuln | grep -q ":${SERVER_PORT} "; then
        echo -e "${YELLOW}警告: 端口 ${SERVER_PORT} 可能已被占用，请手动检查${NC}"
    fi
fi

# 如果已有运行中的服务，尝试停止
if [ -f "backend.pid" ]; then
    OLD_PID=$(cat backend.pid 2>/dev/null || echo "")
    if [ -n "$OLD_PID" ] && kill -0 "$OLD_PID" 2>/dev/null; then
        echo -e "${YELLOW}检测到已有运行中的服务 (PID: $OLD_PID)，正在停止...${NC}"
        kill "$OLD_PID" 2>/dev/null || true
        sleep 2
    fi
    rm -f backend.pid
fi

# 启动服务
echo "正在启动 Spring Boot 应用..."
echo "服务地址: http://${SERVER_HOST}:${SERVER_PORT}"
echo "API 地址: http://${SERVER_HOST}:${SERVER_PORT}/api"
echo ""

# 使用 nohup 在后台运行
nohup java -jar \
    -Dserver.port=${SERVER_PORT} \
    -Dserver.address=${SERVER_HOST} \
    "$JAR_FILE" \
    > backend.log 2>&1 &

# 等待服务启动
sleep 3

# 获取进程 ID（通过 jar 文件名查找）
PID=$(ps aux | grep "[j]ava.*$(basename $JAR_FILE)" | awk '{print $2}' | head -n 1)

if [ -z "$PID" ]; then
    echo -e "${RED}错误: 无法获取服务进程 ID，请检查日志${NC}"
    echo "查看日志: tail -f backend.log"
    exit 1
fi

echo $PID > backend.pid

echo -e "${GREEN}✓ 后端服务已启动 (PID: $PID)${NC}"
echo ""
echo -e "${CYAN}============================================${NC}"
echo -e "${GREEN}部署完成！${NC}"
echo -e "${CYAN}============================================${NC}"
echo ""
echo -e "服务信息:"
echo -e "  - 前端访问: ${GREEN}http://${SERVER_HOST}:${SERVER_PORT}${NC}"
echo -e "  - API 地址: ${GREEN}http://${SERVER_HOST}:${SERVER_PORT}/api${NC}"
echo -e "  - 日志文件: ${GREEN}backend.log${NC}"
echo -e "  - 进程 ID:  ${GREEN}$PID${NC} (保存在 backend.pid)"
echo ""
echo -e "${YELLOW}重要提示:${NC}"
echo -e "  1. 请确保云服务器防火墙已开放端口 ${SERVER_PORT}"
echo -e "  2. 如果使用云服务商安全组，请添加 ${SERVER_PORT} 端口规则"
echo -e "  3. 查看日志: tail -f backend.log"
echo -e "  4. 停止服务: kill \$(cat backend.pid)"
echo -e "  5. 默认管理员账号: admin / 123456"
echo ""

