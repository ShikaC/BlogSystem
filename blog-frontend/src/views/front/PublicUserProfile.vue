<template>
  <div class="user-profile-page" v-loading="loading">
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" style="margin-bottom: 20px;" />

    <div class="profile-header" v-if="userInfo && !error">
      <div class="profile-info">
        <el-avatar :size="80" :src="userInfo.avatar || undefined">
          {{ (userInfo.nickname || '匿').charAt(0) }}
        </el-avatar>
        <div class="info-content">
          <h1>{{ userInfo.nickname }}</h1>
          <p class="bio">{{ userInfo.bio || '这个人很懒，什么都没写~' }}</p>
          <div class="social-links">
            <el-tag size="small" v-if="userInfo.github" type="info">GitHub: {{ userInfo.github }}</el-tag>
            <el-tag size="small" v-if="userInfo.zhihu" type="info">知乎: {{ userInfo.zhihu }}</el-tag>
            <el-tag size="small" v-if="userInfo.weixin" type="success">微信: {{ userInfo.weixin }}</el-tag>
          </div>
          <div class="join-time">加入于 {{ formatDate(userInfo.createdAt) }}</div>
        </div>
      </div>
    </div>

    <div class="profile-content" v-if="userInfo">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="发表的文章" name="articles">
          <div v-if="articles.length === 0" class="empty-list">暂无发表的文章</div>
          <div v-else class="article-list">
            <div v-for="article in articles" :key="article.id" class="article-item" @click="handleArticleClick(article)">
              <div class="article-main">
                <h3 class="article-title">{{ article.title }}</h3>
                <p class="article-summary">{{ article.summary }}</p>
                <div class="article-meta">
                  <span>{{ formatDate(article.createdAt) }}</span>
                  <span><el-icon><View /></el-icon> {{ article.viewCount }}</span>
                  <span><el-icon><Star /></el-icon> {{ article.likeCount }}</span>
                </div>
              </div>
              <div class="article-cover" v-if="article.coverImage">
                <img :src="article.coverImage" alt="cover">
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="点赞的文章" name="likes">
          <div v-if="!userInfo.likesPublic" class="private-tip">
            <el-icon><Lock /></el-icon> 用户设置了仅自己可见
          </div>
          <template v-else>
            <div v-if="likes.length === 0" class="empty-list">暂无点赞记录</div>
            <el-table v-else :data="likes" style="width: 100%" @row-click="handleRowClick">
              <el-table-column prop="title" label="标题">
                <template #default="{ row }">
                  <a class="table-link">{{ row.title }}</a>
                </template>
              </el-table-column>
              <el-table-column prop="contentType" label="类型" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.contentType === 'ARTICLE' ? '' : 'warning'">
                    {{ row.contentType === 'ARTICLE' ? '文章' : '帖子' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="点赞时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createdAt) }}
                </template>
              </el-table-column>
            </el-table>
          </template>
        </el-tab-pane>

        <el-tab-pane label="收藏的文章" name="favorites">
          <div v-if="!userInfo.favoritesPublic" class="private-tip">
            <el-icon><Lock /></el-icon> 用户设置了仅自己可见
          </div>
          <template v-else>
            <div v-if="favorites.length === 0" class="empty-list">暂无收藏记录</div>
            <el-table v-else :data="favorites" style="width: 100%" @row-click="handleRowClick">
              <el-table-column prop="title" label="标题">
                <template #default="{ row }">
                  <a class="table-link">{{ row.title }}</a>
                </template>
              </el-table-column>
              <el-table-column prop="contentType" label="类型" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.contentType === 'ARTICLE' ? '' : 'warning'">
                    {{ row.contentType === 'ARTICLE' ? '文章' : '帖子' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="收藏时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createdAt) }}
                </template>
              </el-table-column>
            </el-table>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicUserInfo, getUserPublicArticles, getUserPublicLikes, getUserPublicFavorites } from '@/api/front'
import { View, Star, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref('') // 添加错误状态
const userInfo = ref(null)
const activeTab = ref('articles')
const articles = ref([])
const likes = ref([])
const favorites = ref([])

const loadData = async () => {
  const userId = route.params.userId
  if (!userId) return

  loading.value = true
  error.value = '' // 重置错误
  try {
    // 1. 获取用户信息
    const userRes = await getPublicUserInfo(userId)
    userInfo.value = userRes.data

    // 2. 获取文章列表
    const artRes = await getUserPublicArticles(userId, { page: 1, size: 20 })
    articles.value = artRes.data.list

    // 3. 获取点赞列表 (如果公开)
    if (userInfo.value.likesPublic) {
      try {
        const likeRes = await getUserPublicLikes(userId, { page: 1, size: 20 })
        likes.value = likeRes.data.list
      } catch (e) {
        console.error('获取点赞列表失败', e)
        // 可能是后端再次校验失败或网络问题，不影响页面显示
      }
    }

    // 4. 获取收藏列表 (如果公开)
    if (userInfo.value.favoritesPublic) {
      try {
        const favRes = await getUserPublicFavorites(userId, { page: 1, size: 20 })
        favorites.value = favRes.data.list
      } catch (e) {
        console.error('获取收藏列表失败', e)
      }
    }

  } catch (e) {
    console.error('加载用户数据失败:', e)
    error.value = '该用户不存在或无法访问'
    // ElMessage.error('用户不存在或无法访问')
    // router.push('/') // 不再自动跳转，避免混淆
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

// 检查文章点击
const handleArticleClick = (article) => {
  if (!article || !article.id) {
    ElMessage.error('文章信息无效')
    return
  }
  router.push(`/article/${article.id}`)
}

// 检查列表点击
const handleRowClick = (row) => {
  if (!row || !row.targetId) {
    ElMessage.error('内容信息无效')
    return
  }
  if (row.contentType === 'ARTICLE') {
    router.push(`/article/${row.targetId}`)
  } else {
    router.push(`/forum/post/${row.targetId}`)
  }
}

// 监听路由参数变化（例如从一个用户点到另一个用户）
watch(() => route.params.userId, (newUserId) => {
  if (newUserId && route.name === 'PublicUserProfile') {
    loadData()
  }
})

onMounted(loadData)
</script>

<style scoped>
.user-profile-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 0;
}

.profile-header {
  background: var(--card-bg, #fff);
  padding: 30px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.profile-info {
  display: flex;
  gap: 20px;
}

.info-content {
  flex: 1;
}

.info-content h1 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: var(--text-color, #333);
}

.bio {
  color: var(--text-secondary, #666);
  margin-bottom: 15px;
  font-size: 14px;
}

.social-links {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.join-time {
  font-size: 12px;
  color: #999;
}

.profile-content {
  background: var(--card-bg, #fff);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  min-height: 400px;
}

.empty-list {
  text-align: center;
  color: #999;
  padding: 40px 0;
}

.private-tip {
  text-align: center;
  color: #909399;
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.private-tip .el-icon {
  font-size: 24px;
}

/* 文章列表样式 */
.article-item {
  display: flex;
  padding: 20px 0;
  border-bottom: 1px solid var(--border-color, #eee);
  cursor: pointer;
  transition: background-color 0.2s;
}

.article-item:hover {
  background-color: var(--hover-bg, #f9f9f9);
}

.article-main {
  flex: 1;
  padding-right: 20px;
}

.article-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: var(--text-color, #333);
}

.article-summary {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 13px;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-cover {
  width: 120px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.table-link {
  color: var(--text-color, #333);
  text-decoration: none;
  font-weight: 500;
}

.table-link:hover {
  color: #409eff;
}
</style>
