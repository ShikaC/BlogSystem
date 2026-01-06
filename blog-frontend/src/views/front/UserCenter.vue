<template>
  <div class="user-center">
    <!-- 用户信息卡片 -->
    <el-card>
      <div class="user-profile">
        <el-avatar :size="80" :src="userStore.userInfo.avatar || undefined">{{ (userStore.displayNickname || '未').charAt(0) }}</el-avatar>
        <div class="user-info">
          <h2>{{ userStore.displayNickname }}</h2>
          <p class="role-tag">
            <el-tag v-if="userStore.isLoggedIn">{{ userStore.isAdmin ? '超级管理员' : '创作者' }}</el-tag>
            <el-tag v-else>未登录</el-tag>
          </p>
          <p v-if="profileForm.bio" class="user-bio">{{ profileForm.bio }}</p>
        </div>
      </div>
    </el-card>

    <!-- 统计信息卡片 -->
    <div class="stats-cards" v-if="statistics">
      <el-card class="stat-card">
        <div class="stat-value">{{ statistics.totalArticles || 0 }}</div>
        <div class="stat-label">文章总数</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ statistics.publishedArticles || 0 }}</div>
        <div class="stat-label">已发布</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ statistics.totalViews || 0 }}</div>
        <div class="stat-label">总阅读</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ statistics.totalLikes || 0 }}</div>
        <div class="stat-label">总点赞</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ statistics.totalCollects || 0 }}</div>
        <div class="stat-label">总收藏</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ statistics.totalPosts || 0 }}</div>
        <div class="stat-label">帖子数</div>
      </el-card>
    </div>

    <div class="center-content">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 我的文章 -->
        <el-tab-pane label="我的文章" name="articles">
          <div class="tab-toolbar">
            <el-radio-group v-model="articleStatus" @change="loadArticlesFiltered" size="small">
              <el-radio-button :value="null">全部</el-radio-button>
              <el-radio-button :value="1">已发布</el-radio-button>
              <el-radio-button :value="0">草稿</el-radio-button>
              <el-radio-button :value="2">私密</el-radio-button>
              <el-radio-button :value="3">回收站</el-radio-button>
            </el-radio-group>
            <div class="toolbar-actions">
              <el-button size="small" type="primary" @click="$router.push('/user/article/edit')">写文章</el-button>
              <el-button v-if="selectedArticleIds.length" size="small" @click="handleBatchUnpublish">批量下架</el-button>
              <el-button v-if="selectedArticleIds.length" size="small" type="danger" @click="handleBatchDelete">批量删除</el-button>
            </div>
          </div>
          <el-table :data="articles" v-loading="loading" @selection-change="handleArticleSelectionChange">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="statusMap[row.status].type" size="small">{{ statusMap[row.status].label }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="阅读" width="70" />
            <el-table-column prop="likeCount" label="点赞" width="70" />
            <el-table-column prop="createdAt" label="时间" width="120">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button link type="primary" @click="editArticle(row.id)">编辑</el-button>
                <el-button link type="danger" @click="handleDeleteArticle(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination" v-if="articleTotal > articlePageSize">
            <el-pagination
              :current-page="articlePage"
              :page-size="articlePageSize"
              :total="articleTotal"
              layout="prev, pager, next"
              @current-change="handleArticlePageChange"
            />
          </div>
        </el-tab-pane>

        <!-- 我的帖子 -->
        <el-tab-pane label="我的帖子" name="posts">
          <el-table :data="posts" v-loading="loading">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="viewCount" label="浏览" width="80" />
            <el-table-column prop="commentCount" label="回复" width="80" />
            <el-table-column prop="createdAt" label="时间" width="180">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button link type="primary" @click="editPost(row.id)">编辑</el-button>
                <el-button link type="danger" @click="handleDeletePost(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 点赞的文章 -->
        <el-tab-pane label="点赞文章" name="liked">
          <el-table :data="likedArticles" v-loading="loading">
            <el-table-column prop="title" label="标题">
              <template #default="{ row }">
                <span class="clickable" @click="goToContent(row)">{{ row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="contentType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{ row.contentType === 'ARTICLE' ? '文章' : '帖子' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="点赞时间" width="180">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
          <div v-if="likedArticles.length === 0" class="empty-tip">暂无点赞的文章</div>
        </el-tab-pane>

        <!-- 收藏的文章 -->
        <el-tab-pane label="收藏文章" name="collected">
          <el-table :data="collectedArticles" v-loading="loading">
            <el-table-column prop="title" label="标题">
              <template #default="{ row }">
                <span class="clickable" @click="goToContent(row)">{{ row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="contentType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small" type="success">{{ row.contentType === 'ARTICLE' ? '文章' : '帖子' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="收藏时间" width="180">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
          <div v-if="collectedArticles.length === 0" class="empty-tip">暂无收藏的文章</div>
        </el-tab-pane>

        <!-- 消息中心 -->
        <el-tab-pane label="消息通知" name="notifications">
          <div v-if="notifications.length === 0" class="empty-tip">暂无消息通知</div>
          <div v-for="n in notifications" :key="n.id" class="notice-item" :class="{ unread: !n.isRead }">
            <div class="notice-header">
              <span class="notice-type">{{ n.type }}</span>
              <span class="notice-time">{{ formatDate(n.createdAt) }}</span>
            </div>
            <div class="notice-body">{{ n.content }}</div>
          </div>
        </el-tab-pane>

        <!-- 个人资料设置 -->
        <el-tab-pane label="资料设置" name="settings">
          <el-form :model="profileForm" label-width="100px" style="max-width: 500px">
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" />
            </el-form-item>
            <el-form-item label="头像">
              <div class="avatar-upload">
                <el-input v-model="profileForm.avatar" placeholder="头像URL" style="flex: 1;" />
                <el-upload
                  :show-file-list="false"
                  :http-request="handleAvatarUpload"
                  accept="image/*"
                >
                  <el-button size="small" type="primary">上传</el-button>
                </el-upload>
              </div>
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="profileForm.bio" type="textarea" :rows="3" />
            </el-form-item>
            
            <el-divider>社交信息</el-divider>
            <el-form-item label="GitHub">
              <el-input v-model="profileForm.github" placeholder="你的GitHub主页链接" />
            </el-form-item>
            <el-form-item label="知乎">
              <el-input v-model="profileForm.zhihu" placeholder="你的知乎主页链接" />
            </el-form-item>
            <el-form-item label="微信">
              <el-input v-model="profileForm.weixin" placeholder="微信号" />
            </el-form-item>

            <el-divider>隐私设置</el-divider>
            <el-form-item label="公开点赞">
              <el-switch v-model="profileForm.likesPublic" active-text="允许他人查看我的点赞列表" />
            </el-form-item>
            <el-form-item label="公开收藏">
              <el-switch v-model="profileForm.favoritesPublic" active-text="允许他人查看我的收藏列表" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="updateProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  getMyArticlesFiltered, getMyPosts, getMyNotifications, getUserProfile, updateUserProfile, 
  deletePost, getMyLikedArticles, getMyCollectedArticles, getUserStatistics, uploadUserImage,
  batchUnpublishArticles, batchDeleteMyArticles
} from '@/api/front'
import { deleteArticle } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('articles')
const loading = ref(false)
const articles = ref([])
const posts = ref([])
const likedArticles = ref([])
const collectedArticles = ref([])
const notifications = ref([])
const statistics = ref(null)

// 文章筛选和分页
const articleStatus = ref(null)
const articlePage = ref(1)
const articlePageSize = ref(10)
const articleTotal = ref(0)
const selectedArticleIds = ref([])

const profileForm = reactive({
  nickname: '',
  avatar: '',
  bio: '',
  github: '',
  zhihu: '',
  weixin: '',
  likesPublic: true,
  favoritesPublic: true
})

const statusMap = {
  0: { label: '草稿', type: 'info' },
  1: { label: '已发布', type: 'success' },
  2: { label: '私密', type: 'warning' },
  3: { label: '回收站', type: 'danger' }
}

const loadArticlesFiltered = async () => {
  loading.value = true
  try {
    const params = { page: articlePage.value, size: articlePageSize.value }
    if (articleStatus.value !== null) params.status = articleStatus.value
    const res = await getMyArticlesFiltered(params)
    articles.value = res.data?.list || []
    articleTotal.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const [postRes, likedRes, collectedRes, noticeRes, profileRes, statsRes] = await Promise.allSettled([
      getMyPosts({ page: 1, size: 50 }),
      getMyLikedArticles({ page: 1, size: 50 }),
      getMyCollectedArticles({ page: 1, size: 50 }),
      getMyNotifications({ page: 1, size: 50 }),
      getUserProfile(),
      getUserStatistics()
    ])
    
    posts.value = postRes.status === 'fulfilled' && postRes.value?.data?.list ? postRes.value.data.list : []
    likedArticles.value = likedRes.status === 'fulfilled' && likedRes.value?.data?.list ? likedRes.value.data.list : []
    collectedArticles.value = collectedRes.status === 'fulfilled' && collectedRes.value?.data?.list ? collectedRes.value.data.list : []
    notifications.value = noticeRes.status === 'fulfilled' && noticeRes.value?.data?.list ? noticeRes.value.data.list : []
    
    if (statsRes.status === 'fulfilled' && statsRes.value?.data) {
      statistics.value = statsRes.value.data
    }
    
    if (profileRes.status === 'fulfilled' && profileRes.value?.data) {
      const data = profileRes.value.data
      Object.assign(profileForm, {
        nickname: data.nickname,
        avatar: data.avatar,
        bio: data.bio,
        github: data.github,
        zhihu: data.zhihu,
        weixin: data.weixin,
        likesPublic: data.likesPublic !== false,
        favoritesPublic: data.favoritesPublic !== false
      })
    }
  } catch (e) {
    console.error('加载用户中心数据失败:', e)
  } finally {
    loading.value = false
  }
}

const handleArticleSelectionChange = (selection) => {
  selectedArticleIds.value = selection.map(item => item.id)
}

const handleArticlePageChange = (page) => {
  articlePage.value = page
  loadArticlesFiltered()
}

const handleBatchUnpublish = async () => {
  await ElMessageBox.confirm(`确定要下架选中的 ${selectedArticleIds.value.length} 篇文章吗？`, '提示')
  await batchUnpublishArticles(selectedArticleIds.value)
  ElMessage.success('已下架')
  loadArticlesFiltered()
  loadData()
}

const handleBatchDelete = async () => {
  await ElMessageBox.confirm(`确定要删除选中的 ${selectedArticleIds.value.length} 篇文章吗？`, '提示')
  await batchDeleteMyArticles(selectedArticleIds.value)
  ElMessage.success('已移到回收站')
  loadArticlesFiltered()
  loadData()
}

const handleAvatarUpload = async ({ file }) => {
  try {
    const res = await uploadUserImage(file)
    profileForm.avatar = res.data.fileUrl
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

const goToContent = (row) => {
  if (row.contentType === 'ARTICLE') {
    router.push(`/article/${row.targetId}`)
  } else {
    router.push(`/forum/post/${row.targetId}`)
  }
}

const editArticle = (id) => {
  router.push(`/user/article/edit/${id}`)
}

const editPost = (id) => {
  // TODO: 实现帖子编辑逻辑
}

const handleDeleteArticle = (id) => {
  ElMessageBox.confirm('确定要删除这篇文章吗？', '提示').then(async () => {
    await deleteArticle(id)
    ElMessage.success('已删除')
    loadArticlesFiltered()
    loadData()
  })
}

const handleDeletePost = (id) => {
  ElMessageBox.confirm('确定要删除这个帖子吗？', '提示').then(async () => {
    await deletePost(id)
    ElMessage.success('已删除')
    loadData()
  })
}

const updateProfile = async () => {
  try {
    await updateUserProfile(profileForm)
    userStore.userInfo.nickname = profileForm.nickname
    userStore.userInfo.avatar = profileForm.avatar
    ElMessage.success('资料已更新')
  } catch (e) {
    const msg = e.response?.data?.message || '更新失败，请重试'
    ElMessage.error(msg)
    console.error(e)
  }
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadArticlesFiltered()
  loadData()
})
</script>

<style scoped>
.user-center {
  padding: 20px 0;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 30px;
}

.user-info h2 {
  margin: 0 0 10px 0;
  color: var(--text-color, #333);
}

.user-bio {
  margin: 5px 0 0 0;
  color: var(--bio-color, #666);
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 15px;
  margin: 20px 0;
}

.stat-card {
  text-align: center;
  padding: 10px;
}

.stat-card .stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.stat-card .stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.center-content {
  margin-top: 20px;
}

/* 工具栏 */
.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}

.avatar-upload {
  display: flex;
  gap: 10px;
  align-items: center;
}

.clickable {
  cursor: pointer;
  color: var(--link-color, #409eff);
  transition: color 0.3s;
}

.clickable:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: var(--meta-color, #999);
  font-size: 14px;
}

.notice-item {
  padding: 15px;
  border-bottom: 1px solid var(--border-color, #eee);
  background: var(--card-bg, #fff);
}

.notice-item.unread {
  background: var(--unread-bg, #fdf6ec);
}

.notice-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 13px;
}

.notice-type {
  font-weight: bold;
  color: #409eff;
}

.notice-time {
  color: var(--meta-color, #909399);
}

.notice-body {
  color: var(--text-color, #333);
  line-height: 1.6;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}

/* 夜间模式样式 */
:deep(.dark) .user-center,
.dark .user-center {
  --card-bg: #16213e;
  --text-color: #e0e0e0;
  --bio-color: #a0a0a0;
  --meta-color: #8899aa;
  --border-color: #2a3f5f;
  --link-color: #66b1ff;
  --unread-bg: #2a3f5f;
}
</style>