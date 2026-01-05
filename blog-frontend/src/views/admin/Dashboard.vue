<template>
  <div class="dashboard">
    <h2>仪表盘</h2>
    
    <el-row :gutter="20">
      <!-- 第一行：核心数据 -->
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #409eff;"><el-icon><Document /></el-icon></div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.totalArticles }}</p>
            <p class="stat-label">文章总数</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;"><el-icon><ChatLineSquare /></el-icon></div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.totalPosts }}</p>
            <p class="stat-label">论坛帖子</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #f56c6c;"><el-icon><ChatDotRound /></el-icon></div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.totalComments }}</p>
            <p class="stat-label">总评论数</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #36cfc9;"><el-icon><User /></el-icon></div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.totalUsers }}</p>
            <p class="stat-label">用户总数</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第二行：互动数据 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #67c23a;"><el-icon><View /></el-icon></div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.totalViews }}</p>
            <p class="stat-label">总阅读量</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;"><el-icon><Star /></el-icon></div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.totalLikes }}</p>
            <p class="stat-label">总点赞数</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #909399;"><el-icon><Message /></el-icon></div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.pendingComments }}</p>
            <p class="stat-label">待审评论</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #909399;"><el-icon><EditPen /></el-icon></div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.draftArticles }}</p>
            <p class="stat-label">草稿箱</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card header="快捷操作">
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/admin/article/edit')">
              <el-icon><Plus /></el-icon> 写文章
            </el-button>
            <el-button @click="$router.push('/admin/article-comments')">文章评论</el-button>
            <el-button @click="$router.push('/admin/forum-comments')">论坛评论</el-button>
            <el-button @click="$router.push('/admin/forum-posts')">帖子管理</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="数据备份">
          <div class="backup-actions">
            <el-button @click="downloadBackup('md')">导出Markdown</el-button>
            <el-button @click="downloadBackup('html')">导出HTML</el-button>
            <el-button @click="downloadBackup('all')">一键备份全部</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card header="热门文章 TOP10" style="margin-top: 20px;">
      <el-table :data="hotArticles" stripe>
        <el-table-column type="index" width="50" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="viewCount" label="阅读量" width="100" />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="commentCount" label="评论" width="80" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStatistics, getHotArticles, exportArticlesMd, exportArticlesHtml, exportAllData } from '@/api/admin'
import { Document, View, Star, ChatDotRound, Plus, ChatLineSquare, User, Message, EditPen } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const stats = ref({})
const hotArticles = ref([])

const downloadBackup = (type) => {
  let url
  switch (type) {
    case 'md': url = exportArticlesMd(); break
    case 'html': url = exportArticlesHtml(); break
    case 'all': url = exportAllData(); break
  }
  const a = document.createElement('a')
  a.href = url
  a.target = '_blank'
  // 添加认证header
  fetch(url, { headers: { Authorization: `Bearer ${userStore.token}` } })
    .then(res => res.blob())
    .then(blob => {
      const u = URL.createObjectURL(blob)
      a.href = u
      a.download = ''
      a.click()
    })
}

onMounted(async () => {
  const [statRes, hotRes] = await Promise.all([
    getStatistics(),
    getHotArticles(10)
  ])
  stats.value = statRes.data || {}
  hotArticles.value = hotRes.data || []
})
</script>

<style scoped>
.dashboard h2 {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.stat-label {
  color: #999;
  margin: 5px 0 0;
  font-size: 14px;
}

.quick-actions, .backup-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.badge {
  margin-left: 5px;
}
</style>
