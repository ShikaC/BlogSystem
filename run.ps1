# 一键启动脚本 (Windows PowerShell)
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# 1. 检查 Java 版本
Write-Host "正在检查 Java 版本..." -ForegroundColor Cyan
$javaVersion = & java -version 2>&1 | Out-String
if ($javaVersion -match "1[7-9]|[2-9][0-9]") {
    Write-Host "Java 版本检查通过" -ForegroundColor Green
} else {
    Write-Host "警告: 建议使用 Java 17 或更高版本，当前版本可能不符。" -ForegroundColor Yellow
}

# 2. 检查 Maven
Write-Host "正在检查 Maven..." -ForegroundColor Cyan
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "Maven 检查通过" -ForegroundColor Green
    $mvnCmd = "mvn"
} else {
    Write-Host "未找到 mvn 命令，将使用 mvnw.cmd" -ForegroundColor Yellow
    $mvnCmd = ".\mvnw.cmd"
}

# 3. 检查 Node.js
Write-Host "正在检查 Node.js..." -ForegroundColor Cyan
if (Get-Command node -ErrorAction SilentlyContinue) {
    Write-Host "Node.js 检查通过" -ForegroundColor Green
} else {
    Write-Host "错误: 未找到 Node.js，请先安装 Node.js 以运行前端。" -ForegroundColor Red
    exit
}

# 4. 创建上传目录
if (-not (Test-Path "uploads")) {
    New-Item -ItemType Directory -Path "uploads" | Out-Null
    Write-Host "已创建 uploads 目录" -ForegroundColor Green
}

# 5. 启动后端
Write-Host "正在启动后端服务 (端口 8080)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "$mvnCmd spring-boot:run" -WindowStyle Normal

# 6. 启动前端
Write-Host "正在启动前端服务 (端口 3000)..." -ForegroundColor Cyan
cd blog-frontend
if (-not (Test-Path "node_modules")) {
    Write-Host "正在安装前端依赖 (仅首次运行)..." -ForegroundColor Yellow
    npm install
}
Start-Process powershell -ArgumentList "-NoExit", "-Command", "npm run dev" -WindowStyle Normal

Write-Host "`n====================================================" -ForegroundColor Green
Write-Host "项目启动指令已发送！" -ForegroundColor Green
Write-Host "后端 API: http://localhost:8080/api" -ForegroundColor Green
Write-Host "前端地址: http://localhost:3000" -ForegroundColor Green
Write-Host "====================================================" -ForegroundColor Green
Write-Host "注意：请确保 MySQL 已启动并创建了 blog_db 数据库。" -ForegroundColor Yellow
Write-Host "首次运行请访问 http://localhost:3000/login 进行初始化。" -ForegroundColor Yellow
