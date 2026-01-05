<template>
  <div class="forum-page">
    <div class="forum-header">
      <h1>社区论坛</h1>
      <el-button type="primary" :icon="Plus" @click="handleCreatePost">发布帖子</el-button>
    </div>

    <el-tabs v-model="activeSection" @tab-change="handleSectionChange">
      <el-tab-pane v-for="section in sections" :key="section.id" :label="section.name" :name="section.id">
        <div v-loading="loading" class="post-list">
          <div v-if="posts.length === 0" class="empty">暂无帖子</div>
          <div v-for="post in posts" :key="post.id" class="post-item" @click="goToDetail(post.id)">
            <div class="post-info">
              <h3 class="post-title">
                <el-tag v-if="post.isTop" size="small" type="danger">置顶</el-tag>
                <el-tag v-if="post.isEssence" size="small" type="warning">精华</el-tag>
                {{ post.title }}
              </h3>
              <div class="post-meta">
                <span><el-icon><User /></el-icon> {{ post.userNickname || '匿名用户' }}</span>
                <span>发布于: {{ formatDate(post.createdAt) }}</span>
                <span>查看: {{ post.viewCount }}</span>
                <span>回复: {{ post.commentCount }}</span>
              </div>
            </div>
          </div>
          
          <el-pagination
            v-if="total > 0"
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadPosts"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 发布帖子对话框 -->
    <el-dialog v-model="dialogVisible" title="发布新帖子" width="50%">
      <el-form :model="postForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="postForm.title" placeholder="请输入帖子标题" />
        </el-form-item>
        <el-form-item label="板块">
          <el-select v-model="postForm.sectionId" placeholder="请选择板块">
            <el-option v-for="s in sections" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="postForm.content" type="textarea" :rows="10" placeholder="支持Markdown格式" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { getForumSections, getForumPosts, savePost } from '@/api/front'
import { useUserStore } from '@/stores/user'
import { Plus, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const sections = ref([])
const activeSection = ref(null)
const posts = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)

const queryParams = reactive({
  page: 1,
  size: 10
})

const postForm = reactive({
  title: '',
  sectionId: null,
  content: ''
})

const loadSections = async () => {
  const res = await getForumSections()
  sections.value = res.data
  if (sections.value.length > 0) {
    activeSection.value = sections.value[0].id
    loadPosts()
  }
}

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await getForumPosts({
      sectionId: activeSection.value,
      ...queryParams
    })
    posts.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSectionChange = () => {
  queryParams.page = 1
  loadPosts()
}

const handleCreatePost = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  postForm.sectionId = activeSection.value
  dialogVisible.value = true
}

const submitPost = async () => {
  if (!postForm.title || !postForm.content) {
    ElMessage.error('请填写标题和内容')
    return
  }
  await savePost(postForm)
  ElMessage.success('发布成功')
  dialogVisible.value = false
  loadPosts()
}

const goToDetail = (id) => {
  if (id) {
    router.push(`/forum/post/${id}`)
  }
}

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

onMounted(loadSections)
</script>

<style scoped>
.forum-page {
  padding: 20px;
  background: var(--card-bg, #fff);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  color: var(--text-color, #333);
}

.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.forum-header h1 {
  color: var(--text-color, #333);
}

.post-item {
  padding: 15px;
  border-bottom: 1px solid var(--border-color, #eee);
  cursor: pointer;
  transition: background 0.3s;
  background: var(--card-bg, #fff);
}

.post-item:hover {
  background: var(--hover-bg, #f9f9f9);
}

.post-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: var(--text-color, #303133);
}

.post-meta {
  font-size: 12px;
  color: var(--meta-color, #909399);
  display: flex;
  gap: 20px;
}

.empty {
  text-align: center;
  padding: 40px;
  color: var(--meta-color, #909399);
}

/* 夜间模式样式 */
:deep(.dark) .forum-page,
.dark .forum-page {
  --card-bg: #16213e;
  --text-color: #e0e0e0;
  --border-color: #2a3f5f;
  --hover-bg: #1a2942;
  --meta-color: #8899aa;
}
</style>


