<template>
  <div class="user-center">
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

    <div class="center-content">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 我的文章 -->
        <el-tab-pane label="我的文章" name="articles">
          <el-table :data="articles" v-loading="loading">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusMap[row.status].type">{{ statusMap[row.status].label }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="180">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button link type="primary" @click="editArticle(row.id)">编辑</el-button>
                <el-button link type="danger" @click="handleDeleteArticle(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
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
                <span class="clickable" @click="$router.push(`/article/${row.targetId}`)">{{ row.title }}</span>
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
                <span class="clickable" @click="$router.push(`/article/${row.targetId}`)">{{ row.title }}</span>
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
            <el-form-item label="头像URL">
              <el-input v-model="profileForm.avatar" />
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
import { getMyArticles, getMyPosts, getMyNotifications, getUserProfile, updateUserProfile, deletePost, getMyLikedArticles, getMyCollectedArticles } from '@/api/front'
import { deleteArticle } from '@/api/admin' // 注册用户也能删自己的
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

const loadData = async () => {
  loading.value = true
  try {
    const [artRes, postRes, likedRes, collectedRes, noticeRes, profileRes] = await Promise.allSettled([
      getMyArticles({ page: 1, size: 50 }),
      getMyPosts({ page: 1, size: 50 }),
      getMyLikedArticles({ page: 1, size: 50 }),
      getMyCollectedArticles({ page: 1, size: 50 }),
      getMyNotifications({ page: 1, size: 50 }),
      getUserProfile()
    ])
    
    // 安全地获取数据，即使某个接口失败也不影响其他数据
    articles.value = artRes.status === 'fulfilled' && artRes.value?.data?.list ? artRes.value.data.list : []
    posts.value = postRes.status === 'fulfilled' && postRes.value?.data?.list ? postRes.value.data.list : []
    likedArticles.value = likedRes.status === 'fulfilled' && likedRes.value?.data?.list ? likedRes.value.data.list : []
    collectedArticles.value = collectedRes.status === 'fulfilled' && collectedRes.value?.data?.list ? collectedRes.value.data.list : []
    notifications.value = noticeRes.status === 'fulfilled' && noticeRes.value?.data?.list ? noticeRes.value.data.list : []
    
    if (profileRes.status === 'fulfilled' && profileRes.value?.data) {
      const data = profileRes.value.data
      Object.assign(profileForm, {
        nickname: data.nickname,
        avatar: data.avatar,
        bio: data.bio,
        github: data.github,
        zhihu: data.zhihu,
        weixin: data.weixin,
        likesPublic: data.likesPublic !== false, // 默认true
        favoritesPublic: data.favoritesPublic !== false // 默认true
      })
    }
  } catch (e) {
    console.error('加载用户中心数据失败:', e)
    ElMessage.error('加载数据失败，请稍后重试')
  } finally {
    loading.value = false
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
    // 更新store中的信息
    userStore.userInfo.nickname = profileForm.nickname
    userStore.userInfo.avatar = profileForm.avatar
    ElMessage.success('资料已更新')
  } catch (e) {
    // 检查是否是后端返回的错误信息
    const msg = e.response?.data?.message || '更新失败，请重试'
    ElMessage.error(msg)
    console.error(e)
  }
}

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

onMounted(loadData)
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

.center-content {
  margin-top: 20px;
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

/* 让 el-card 和 el-tabs 适配夜间模式 */
:deep(.dark) .user-center .el-card {
  background-color: #16213e;
  border-color: #2a3f5f;
}

:deep(.dark) .user-center .el-tabs {
  background-color: #16213e;
}

:deep(.dark) .user-center .el-tabs__header {
  background-color: #16213e;
  border-color: #2a3f5f;
}

:deep(.dark) .user-center .el-tabs__item {
  color: #a0a0a0;
}

:deep(.dark) .user-center .el-tabs__item.is-active {
  color: #66b1ff;
}

:deep(.dark) .user-center .el-table {
  background-color: #16213e;
  color: #e0e0e0;
}

:deep(.dark) .user-center .el-table th {
  background-color: #1a2942;
  color: #e0e0e0;
}

:deep(.dark) .user-center .el-table tr {
  background-color: #16213e;
}

:deep(.dark) .user-center .el-table td {
  border-color: #2a3f5f;
}

:deep(.dark) .user-center .el-table__body tr:hover > td {
  background-color: #1a2942 !important;
}
</style>