<template>
  <div class="home-page">
    <div class="content-wrapper">
      <!-- 文章列表 -->
      <div class="article-list">
        <article v-for="article in articles" :key="article.id" class="article-card" @click="goToArticle(article.id)">
          <div class="article-cover" v-if="article.coverImage">
            <img :src="article.coverImage" :alt="article.title" loading="lazy" />
          </div>
          <div class="article-info">
            <h2 class="article-title">
              <el-tag v-if="article.isTop" type="danger" size="small">置顶</el-tag>
              {{ article.title }}
            </h2>
            <p class="article-summary">{{ article.summary }}</p>
            <div class="article-meta">
              <span><el-icon><Calendar /></el-icon> {{ formatDate(article.createdAt) }}</span>
              <span><el-icon><View /></el-icon> {{ article.viewCount }}</span>
              <span v-if="article.categoryName"><el-icon><Folder /></el-icon> {{ article.categoryName }}</span>
            </div>
            <div class="article-tags" v-if="article.tags?.length">
              <el-tag v-for="tag in article.tags" :key="tag.id" size="small" @click.stop="$router.push(`/tag/${tag.id}`)">
                {{ tag.name }}
              </el-tag>
            </div>
          </div>
        </article>

        <div v-if="loading" class="loading">
          <el-icon class="is-loading"><Loading /></el-icon> 加载中...
        </div>

        <div v-if="!loading && articles.length === 0" class="empty">
          暂无文章
        </div>

        <el-pagination
          v-if="total > pageSize"
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
          class="pagination"
        />
      </div>

      <!-- 侧边栏 -->
      <aside class="sidebar">
        <!-- 博主信息 -->
        <div class="widget blogger-card">
          <el-avatar :size="80" :src="blogger.avatar || undefined">{{ (blogger.nickname || '未').charAt(0) }}</el-avatar>
          <h3>{{ blogger.nickname || '未登录' }}</h3>
          <p class="bio">{{ userStore.isLoggedIn ? (blogger.bio || '暂无个人简介') : '请先登录以查看个人信息' }}</p>
        </div>

        <!-- 分类 -->
        <div class="widget">
          <h4>分类</h4>
          <div class="category-list">
            <div v-for="cat in categories" :key="cat.id" class="category-item" @click="$router.push(`/category/${cat.id}`)">
              <span>{{ cat.name }}</span>
              <span class="count">{{ cat.articleCount }}</span>
            </div>
          </div>
        </div>

        <!-- 标签云 -->
        <div class="widget">
          <h4>标签</h4>
          <div class="tag-cloud">
            <el-tag v-for="tag in tags" :key="tag.id" @click="$router.push(`/tag/${tag.id}`)" style="cursor:pointer;margin:4px;">
              {{ tag.name }} ({{ tag.articleCount }})
            </el-tag>
          </div>
        </div>

        <!-- 热门文章 -->
        <div class="widget">
          <h4>热门文章</h4>
          <div class="hot-list">
            <div v-for="(article, idx) in hotArticles" :key="article.id" class="hot-item" @click="goToArticle(article.id)">
              <span class="rank" :class="{ top: idx < 3 }">{{ idx + 1 }}</span>
              <span class="title">{{ article.title }}</span>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getArticles, getCategories, getTags, getHotArticles, getBloggerInfo, getUserInfo } from '@/api/front'
import { useUserStore } from '@/stores/user'
import { Calendar, View, Folder, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const articles = ref([])
const categories = ref([])
const tags = ref([])
const hotArticles = ref([])
const blogger = ref({})
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const goToArticle = (id) => {
  router.push(`/article/${id}`)
}

const handlePageChange = (newPage) => {
  page.value = newPage
  loadArticles()
}

const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getArticles({ page: page.value, size: pageSize.value })
    articles.value = res.data.list
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  loadArticles()
  
  try {
    // 根据登录状态决定加载哪个用户信息
    if (userStore.isLoggedIn) {
      try {
        const userRes = await getUserInfo()
        const userData = userRes.data || {}
        // 合并API返回的数据和显示昵称
        blogger.value = {
          ...userData,
          displayNickname: userStore.displayNickname
        }
      } catch (err) {
        console.error('获取用户信息失败:', err)
        // 使用存储在store中的用户信息作为备选
        blogger.value = {
          nickname: userStore.nickname,
          avatar: userStore.avatar,
          bio: '个人简介',
          displayNickname: userStore.displayNickname
        }
      }
    } else {
      // 未登录时显示“未登录”
      blogger.value = { 
        displayNickname: '未登录',
        nickname: '未登录', 
        bio: '', 
        avatar: null 
      }
    }
    
    const [catRes, tagRes, hotRes] = await Promise.all([
      getCategories(),
      getTags(),
      getHotArticles(10)
    ])
    categories.value = catRes.data || []
    tags.value = tagRes.data || []
    hotArticles.value = hotRes.data || []
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.home-page {
  padding: 20px 0;
}

.content-wrapper {
  display: flex;
  gap: 30px;
}

.article-list {
  flex: 1;
}

.article-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.article-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}

.article-cover img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.article-info {
  padding: 20px;
}

.article-title {
  font-size: 1.3rem;
  margin: 0 0 10px 0;
  color: #333;
}

.article-summary {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #999;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-tags {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.sidebar {
  width: 300px;
  flex-shrink: 0;
}

.widget {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.blogger-card {
  text-align: center;
}

.blogger-card h3 {
  margin: 10px 0 5px;
}

.blogger-card .bio {
  color: #666;
  font-size: 14px;
}

.widget h4 {
  margin: 0 0 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
}

.category-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  cursor: pointer;
  border-bottom: 1px dashed #eee;
}

.category-item:hover {
  color: #409eff;
}

.category-item .count {
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.hot-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  cursor: pointer;
}

.hot-item:hover .title {
  color: #409eff;
}

.hot-item .rank {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: #ddd;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  margin-right: 10px;
}

.hot-item .rank.top {
  background: #409eff;
  color: #fff;
}

.hot-item .title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
  }
}
</style>
