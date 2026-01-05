<template>
  <div class="about-page">
    <div class="about-card">
      <el-avatar :size="100" shape="square" style="background: none;">
        <img src="/vite.svg" style="width:100%;height:100%;object-fit:contain;" alt="Logo" />
      </el-avatar>
      <h1>关于本系统</h1>
      <p class="desc">
        这是一个基于 Spring Boot + Vue 3 的现代化博客论坛系统。<br>
        致力于提供简洁、高效、美观的内容创作与交流平台。
      </p>

      <div class="features">
        <div class="feature-item">
          <h3>📚 文章发布</h3>
          <p>支持 Markdown 编辑，实时预览，代码高亮</p>
        </div>
        <div class="feature-item">
          <h3>💬 社区互动</h3>
          <p>发帖回帖，点赞收藏，实时交流</p>
        </div>
        <div class="feature-item">
          <h3>🎨 现代设计</h3>
          <p>响应式布局，夜间模式，极致体验</p>
        </div>
      </div>
      
      <div class="actions">
        <a href="https://github.com/ShikaC/BlogSystem" target="_blank" class="github-btn">
          <el-icon><Link /></el-icon> 访问 GitHub 仓库
        </a>
      </div>

      <div class="footer-info">
        <p>Current Version: v1.0.0</p>
        <p>Created by Shikaa</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Link } from '@element-plus/icons-vue'
</script>

<style scoped>
.about-page {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.about-card {
  background: var(--card-bg, #fff);
  border-radius: 12px;
  padding: 60px;
  text-align: center;
  max-width: 800px;
  width: 100%;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  color: var(--text-color, #333);
}

.about-card h1 {
  margin: 20px 0 20px;
  font-size: 2rem;
}

.desc {
  font-size: 1.1rem;
  line-height: 1.6;
  color: var(--meta-color, #666);
  margin-bottom: 50px;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 30px;
  margin-bottom: 50px;
  text-align: left;
}

.feature-item {
  padding: 20px;
  background: var(--bg-color, #f9fafc);
  border-radius: 8px;
  transition: transform 0.3s;
}

.feature-item:hover {
  transform: translateY(-5px);
}

.feature-item h3 {
  margin: 0 0 10px;
  color: #409eff;
}

.feature-item p {
  margin: 0;
  color: var(--meta-color, #666);
  font-size: 0.9rem;
}

.github-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: #333;
  color: #fff;
  padding: 12px 30px;
  border-radius: 30px;
  text-decoration: none;
  font-size: 1.1rem;
  transition: background 0.3s, transform 0.3s;
}

.github-btn:hover {
  background: #000;
  transform: scale(1.05);
}

.footer-info {
  margin-top: 50px;
  font-size: 12px;
  color: #999;
}

.footer-info p {
  margin: 5px 0;
}

/* 夜间模式 */
:deep(.dark) .feature-item {
  background: #1a2942;
}
</style>
