<template>
  <div v-if="post" class="post-detail-page">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/forum' }">论坛首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{ post.title }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="post-container">
      <div class="post-header">
        <h1>{{ post.title }}</h1>
        <div class="post-meta">
          <span>作者 ID: {{ post.userId }}</span>
          <span>发布于: {{ formatDate(post.createdAt) }}</span>
          <span>阅读: {{ post.viewCount }}</span>
        </div>
      </div>
      
      <div class="post-content" v-html="post.content"></div>

      <div class="post-actions">
        <el-button type="primary" link @click="handleLike">点赞 ({{ post.likeCount }})</el-button>
        <el-button type="primary" link @click="handleCollect">收藏 ({{ post.collectCount }})</el-button>
      </div>
    </div>

    <!-- 回帖区域 -->
    <div class="comment-section">
      <h3>共 {{ post.commentCount }} 条回帖</h3>
      
      <div class="comment-input">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          placeholder="既然来了，就留点什么吧..."
        />
        <div class="submit-btn">
          <el-button type="primary" @click="submitComment">发表回帖</el-button>
        </div>
      </div>

      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-info">
            <span class="comment-user">用户 {{ comment.userId }}</span>
            <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
          </div>
          <div class="comment-text">{{ comment.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getForumPostDetail, createPostComment, likePost, collectPost } from '@/api/front'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref(null)
const comments = ref([]) // 简化处理，暂时直接在详情页加载或实现独立接口
const commentContent = ref('')

const loadData = async () => {
  const id = route.params.id
  const res = await getForumPostDetail(id)
  post.value = res.data
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录')
  await likePost(post.value.id)
  post.value.likeCount++
  ElMessage.success('已点赞')
}

const handleCollect = async () => {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录')
  await collectPost(post.value.id)
  post.value.collectCount++
  ElMessage.success('已收藏')
}

const submitComment = async () => {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录')
  if (!commentContent.value.trim()) return ElMessage.error('请输入内容')
  
  await createPostComment({
    postId: post.value.id,
    content: commentContent.value
  })
  
  ElMessage.success('回复成功')
  commentContent.value = ''
  loadData() // 重新加载以展示新评论
}

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

onMounted(loadData)
</script>

<style scoped>
.post-detail-page {
  max-width: 900px;
  margin: 0 auto;
}

.post-container {
  background: var(--card-bg, #fff);
  padding: 30px;
  border-radius: 8px;
  margin-top: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.post-header h1 {
  margin: 0 0 15px 0;
  color: #303133;
}

.post-meta {
  font-size: 13px;
  color: #909399;
  margin-bottom: 20px;
  display: flex;
  gap: 20px;
}

.post-content {
  line-height: 1.8;
  color: #444;
  margin-bottom: 30px;
  white-space: pre-wrap;
}

.post-actions {
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.comment-section {
  margin-top: 30px;
  background: var(--card-bg, #fff);
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.comment-input {
  margin-bottom: 30px;
}

.submit-btn {
  text-align: right;
  margin-top: 10px;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #f2f6fc;
}

.comment-info {
  margin-bottom: 8px;
  font-size: 13px;
}

.comment-user {
  font-weight: bold;
  color: #409eff;
  margin-right: 15px;
}

.comment-time {
  color: #909399;
}

.comment-text {
  color: #606266;
  line-height: 1.6;
}
</style>

