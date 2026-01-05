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
        <div v-if="comments.length === 0" class="empty-comments">暂无回帖，快来抢沙发！</div>
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-info">
            <span class="comment-user">{{ comment.nickname || '用户 ' + comment.userId }}</span>
            <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
            <el-button 
              v-if="userStore.isLoggedIn && Number(userStore.id) === comment.userId" 
              type="danger" 
              link 
              size="small" 
              @click="handleDeleteComment(comment.id)"
            >
              删除
            </el-button>
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
import { getForumPost, getForumPostComments, createPostComment, deletePostComment, likePost, collectPost } from '@/api/front'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref(null)
const comments = ref([])
const commentContent = ref('')

const loadData = async () => {
  const id = route.params.id
  try {
    const res = await getForumPost(id)
    post.value = res.data
    // 加载评论
    try {
      const commentRes = await getForumPostComments(id, { page: 1, size: 100 })
      comments.value = commentRes.data?.list || commentRes.data || []
    } catch (e) {
      console.error('加载评论失败:', e)
      comments.value = []
    }
  } catch (e) {
    console.error('加载帖子失败:', e)
    ElMessage.error('加载帖子失败')
  }
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录')
  try {
    await likePost(post.value.id)
    post.value.likeCount++
    ElMessage.success('已点赞')
  } catch (e) {
    // 后端会在用户重复点赞时静默处理
    ElMessage.info('您已点赞过该帖子')
  }
}

const handleCollect = async () => {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录')
  try {
    await collectPost(post.value.id)
    post.value.collectCount++
    ElMessage.success('已收藏')
  } catch (e) {
    // 后端会在用户重复收藏时抛出异常
  }
}

const submitComment = async () => {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录')
  if (!commentContent.value.trim()) return ElMessage.error('请输入内容')
  
  try {
    await createPostComment({
      postId: post.value.id,
      content: commentContent.value
    })
    
    ElMessage.success('回复成功')
    commentContent.value = ''
    loadData() // 重新加载以展示新评论
  } catch (e) {
    console.error('回复失败:', e)
    ElMessage.error('回复失败，请稍后重试')
  }
}

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

const handleDeleteComment = async (commentId) => {
  try {
    await deletePostComment(commentId)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    console.error('删除评论失败:', e)
  }
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
  color: var(--text-color, #303133);
}

.post-meta {
  font-size: 13px;
  color: var(--meta-color, #909399);
  margin-bottom: 20px;
  display: flex;
  gap: 20px;
}

.post-content {
  line-height: 1.8;
  color: var(--content-color, #444);
  margin-bottom: 30px;
  white-space: pre-wrap;
}

.post-actions {
  border-top: 1px solid var(--border-color, #eee);
  padding-top: 20px;
}

.comment-section {
  margin-top: 30px;
  background: var(--card-bg, #fff);
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.comment-section h3 {
  color: var(--text-color, #303133);
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
  border-bottom: 1px solid var(--border-light, #f2f6fc);
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
  color: var(--meta-color, #909399);
}

.comment-text {
  color: var(--content-color, #606266);
  line-height: 1.6;
}

.empty-comments {
  text-align: center;
  padding: 40px;
  color: var(--meta-color, #909399);
}

/* 夜间模式样式 */
:deep(.dark) .post-detail-page,
.dark .post-detail-page {
  --card-bg: #16213e;
  --text-color: #e0e0e0;
  --content-color: #c0c0c0;
  --meta-color: #8899aa;
  --border-color: #2a3f5f;
  --border-light: #2a3f5f;
}
</style>


