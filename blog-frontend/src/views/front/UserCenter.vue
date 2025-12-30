<template>
  <div class="user-center">
    <el-card>
      <div class="user-profile">
        <el-avatar :size="80" :src="userStore.userInfo.avatar" />
        <div class="user-info">
          <h2>{{ userStore.userInfo.nickname }}</h2>
          <p class="role-tag">
            <el-tag>{{ userStore.isAdmin ? '超级管理员' : '创作者' }}</el-tag>
          </p>
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

        <!-- 消息中心 -->
        <el-tab-pane label="消息通知" name="notifications">
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
              <el-input v-model="profileForm.bio" type="textarea" />
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
import { getMyArticles, getMyPosts, getMyNotifications, getUserProfile, deletePost } from '@/api/front'
import { deleteArticle } from '@/api/admin' // 注册用户也能删自己的
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('articles')
const loading = ref(false)
const articles = ref([])
const posts = ref([])
const notifications = ref([])
const profileForm = reactive({
  nickname: '',
  avatar: '',
  bio: ''
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
    const [artRes, postRes, noticeRes, profileRes] = await Promise.all([
      getMyArticles({ page: 1, size: 50 }),
      getMyPosts({ page: 1, size: 50 }),
      getMyNotifications({ page: 1, size: 50 }),
      getUserProfile()
    ])
    articles.value = artRes.data.list
    posts.value = postRes.data.list
    notifications.value = noticeRes.data.list
    Object.assign(profileForm, profileRes.data)
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

const updateProfile = () => {
  // TODO: 调用API更新资料
  ElMessage.success('资料已更新')
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
}

.center-content {
  margin-top: 20px;
}

.notice-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.notice-item.unread {
  background: #fdf6ec;
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
  color: #909399;
}
</style>

