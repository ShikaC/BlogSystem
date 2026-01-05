<template>
  <div class="article-page" v-if="article">
    <div class="content-wrapper">
      <!-- 文章主体 -->
      <article class="article-main">
        <header class="article-header">
          <div class="header-top">
            <h1>{{ article.title }}</h1>
            <div class="action-buttons" v-if="userStore.isLoggedIn && article.userId === Number(userStore.userInfo.id)">
              <!-- 编辑按钮，仅作者可见 -->
              <el-button 
                type="primary" 
                @click="handleEditArticle"
                :icon="Edit"
              >
                编辑文章
              </el-button>
              <!-- 删除按钮，仅作者可见 -->
              <el-button 
                type="danger" 
                @click="handleDeleteArticle"
                :icon="Delete"
              >
                删除文章
              </el-button>
            </div>
          </div>
          <div class="article-meta">
            <span><el-icon><Calendar /></el-icon> 发布于 {{ formatDate(article.createdAt) }}</span>
            <span v-if="article.updatedAt && article.updatedAt !== article.createdAt"><el-icon><Edit /></el-icon> 最后编辑 {{ formatDate(article.updatedAt) }}</span>
            <span><el-icon><View /></el-icon> {{ article.viewCount }}</span>
            <span v-if="article.categoryName"><el-icon><Folder /></el-icon> {{ article.categoryName }}</span>
            <span><el-icon><Timer /></el-icon> {{ article.wordCount }} 字，约 {{ Math.ceil(article.wordCount / 400) }} 分钟</span>
          </div>
          <div class="article-tags" v-if="article.tags?.length">
            <el-tag v-for="tag in article.tags" :key="tag.id" @click="$router.push(`/tag/${tag.id}`)">
              {{ tag.name }}
            </el-tag>
          </div>
        </header>

        <div class="article-content" v-html="article.content"></div>

        <!-- 互动 -->
        <div class="article-actions">
          <el-button :type="isLiked ? 'primary' : 'default'" @click="handleLike">
            <el-icon><Star /></el-icon> 点赞 {{ article.likeCount }}
          </el-button>
          <el-button :type="isCollected ? 'primary' : 'default'" @click="handleCollect">
            <el-icon><Collection /></el-icon> 收藏 {{ article.collectCount }}
          </el-button>
        </div>

        <!-- 相关推荐 -->
        <div class="related-articles" v-if="relatedArticles.length">
          <h3>相关推荐</h3>
          <div class="related-list">
            <div v-for="ra in relatedArticles" :key="ra.id" class="related-item" @click="$router.push(`/article/${ra.id}`)">
              {{ ra.title }}
            </div>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
          <h3>评论 ({{ comments.length }})</h3>
          
          <!-- 发表评论 -->
          <div class="comment-form">
            <div v-if="!userStore.isLoggedIn">
              <p style="color: #999; margin-bottom: 15px;">您需要登录后才能发表评论</p>
              <el-button type="primary" @click="router.push('/login')">立即登录</el-button>
            </div>
            <div v-else>
              <el-form :model="commentForm" ref="commentFormRef">
                <el-input
                  v-model="commentForm.content"
                  type="textarea"
                  :rows="4"
                  placeholder="说点什么..."
                  style="margin-top: 10px;"
                />
                <div style="margin-top: 10px; display: flex; gap: 10px; align-items: center;">
                  <el-button type="primary" @click="submitComment" :loading="submitting">发表评论</el-button>
                </div>
              </el-form>
            </div>
          </div>

          <!-- 评论列表 -->
          <div class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <el-avatar :size="40" :src="comment.avatar || undefined">{{ comment.nickname?.charAt(0) || '匿' }}</el-avatar>
              <div class="comment-body">
                <div class="comment-header">
                  <span class="comment-nickname" :class="{ blogger: comment.isBlogger }">
                    {{ comment.nickname || '匿名访客' }}
                    <el-tag v-if="comment.isBlogger" size="small" type="danger">博主</el-tag>
                  </span>
                  <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                  <!-- 删除按钮，仅显示给评论作者 -->
                  <el-button 
                    v-if="userStore.isLoggedIn && comment.userId === Number(userStore.userInfo.id)" 
                    type="danger" 
                    size="small" 
                    @click="deleteComment(comment.id)"
                    style="margin-left: auto;"
                  >
                    删除
                  </el-button>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <!-- 子评论 -->
                <div v-if="comment.children?.length" class="comment-children">
                  <div v-for="child in comment.children" :key="child.id" class="comment-item child">
                    <el-avatar :size="32" :src="child.avatar || undefined">{{ child.nickname?.charAt(0) || '匿' }}</el-avatar>
                    <div class="comment-body">
                      <div class="comment-header">
                        <span class="comment-nickname" :class="{ blogger: child.isBlogger }">
                          {{ child.nickname || '匿名访客' }}
                          <el-tag v-if="child.isBlogger" size="small" type="danger">博主</el-tag>
                        </span>
                        <span v-if="child.replyToNickname">回复 @{{ child.replyToNickname }}</span>
                        <span class="comment-time">{{ formatDate(child.createdAt) }}</span>
                        <!-- 子评论删除按钮，仅显示给评论作者 -->
                        <el-button 
                          v-if="userStore.isLoggedIn && child.userId === Number(userStore.userInfo.id)" 
                          type="danger" 
                          size="small" 
                          @click="deleteComment(child.id)"
                          style="margin-left: auto;"
                        >
                          删除
                        </el-button>
                      </div>
                      <div class="comment-content">{{ child.content }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </article>

      <!-- 目录 -->
      <aside class="sidebar toc" v-if="toc.length">
        <h4>目录</h4>
        <div class="toc-list">
          <a v-for="item in toc" :key="item.id" :href="`#${item.id}`" class="toc-item" :class="`level-${item.level}`">
            {{ item.text }}
          </a>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticle, getRelatedArticles, getArticleComments, likeArticle, unlikeArticle, collectArticle, uncollectArticle, createComment, deleteComment as deleteCommentApi } from '@/api/front'
import { deleteArticle } from '@/api/admin'
import { useUserStore } from '@/stores/user'
import { Calendar, View, Folder, Timer, Star, Collection, Delete, Edit } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const article = ref(null)
const relatedArticles = ref([])
const comments = ref([])
const toc = ref([])
const isLiked = ref(false)
const isCollected = ref(false)
const submitting = ref(false)

const commentForm = ref({
  content: ''
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const handleLike = async () => {
  try {
    if (isLiked.value) {
      await unlikeArticle(article.value.id)
      article.value.likeCount--
    } else {
      await likeArticle(article.value.id)
      article.value.likeCount++
    }
    isLiked.value = !isLiked.value
    localStorage.setItem(`liked_${article.value.id}`, isLiked.value)
  } catch (e) {
    console.error(e)
  }
}

const handleCollect = async () => {
  try {
    if (isCollected.value) {
      await uncollectArticle(article.value.id)
      article.value.collectCount--
    } else {
      await collectArticle(article.value.id)
      article.value.collectCount++
    }
    isCollected.value = !isCollected.value
    localStorage.setItem(`collected_${article.value.id}`, isCollected.value)
  } catch (e) {
    console.error(e)
  }
}

const submitComment = async () => {
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    await createComment({
      articleId: article.value.id,
      ...commentForm.value
    })
    ElMessage.success('评论成功')
    commentForm.value.content = ''
    loadComments()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

// 删除评论功能
const deleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteCommentApi(commentId)
    ElMessage.success('评论已删除')
    loadComments() // 重新加载评论列表
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

// 删除文章功能
const handleDeleteArticle = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？删除后将无法恢复。', '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteArticle(article.value.id)
    ElMessage.success('文章已删除')
    router.push('/') // 删除后返回首页
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除文章失败:', e)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// 编辑文章功能
const handleEditArticle = () => {
  router.push(`/user/article/edit/${article.value.id}`)
}

const loadComments = async () => {
  const res = await getArticleComments(route.params.id)
  comments.value = res.data || []
}

const generateToc = () => {
  nextTick(() => {
    const contentEl = document.querySelector('.article-content')
    if (!contentEl) return
    const headings = contentEl.querySelectorAll('h1, h2, h3, h4, h5, h6')
    toc.value = Array.from(headings).map((h, idx) => {
      const id = `heading-${idx}`
      h.id = id
      return {
        id,
        text: h.textContent,
        level: parseInt(h.tagName.charAt(1))
      }
    })
    
    // 代码高亮
    contentEl.querySelectorAll('pre code').forEach((block) => {
      hljs.highlightElement(block)
    })
  })
}

onMounted(async () => {
  try {
    const res = await getArticle(route.params.id)
    article.value = res.data
    
    // 检查本地缓存的点赞收藏状态
    isLiked.value = localStorage.getItem(`liked_${article.value.id}`) === 'true'
    isCollected.value = localStorage.getItem(`collected_${article.value.id}`) === 'true'
    
    generateToc()
    
    // 加载相关文章
    const relRes = await getRelatedArticles(route.params.id, 5)
    relatedArticles.value = relRes.data || []
    
    // 加载评论
    loadComments()
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.article-page {
  padding: 20px 0;
}

.content-wrapper {
  display: flex;
  gap: 30px;
}

.article-main {
  flex: 1;
  background: var(--card-bg, #fff);
  border-radius: 8px;
  padding: 30px;
  color: var(--text-color, #333);
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.article-header h1 {
  font-size: 2rem;
  margin: 0;
  color: var(--text-color, #333);
}


.article-meta {
  display: flex;
  gap: 20px;
  color: var(--meta-color, #999);
  font-size: 14px;
  margin-bottom: 15px;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.article-content {
  line-height: 1.8;
  font-size: 16px;
  color: var(--content-color, #333);
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4) {
  margin-top: 30px;
  margin-bottom: 15px;
  color: var(--text-color, #333);
}

.article-content :deep(p) {
  color: var(--content-color, #555);
}

.article-content :deep(pre) {
  background: var(--code-bg, #f6f8fa);
  padding: 15px;
  border-radius: 6px;
  overflow-x: auto;
}

.article-content :deep(code) {
  color: var(--code-color, #333);
}

.article-content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}

.article-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color, #eee);
  display: flex;
  gap: 15px;
}

.related-articles {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color, #eee);
}

.related-articles h3 {
  margin-bottom: 15px;
  color: var(--text-color, #333);
}

.related-item {
  padding: 10px 0;
  cursor: pointer;
  border-bottom: 1px dashed var(--border-color, #eee);
  color: var(--text-color, #333);
}

.related-item:hover {
  color: #409eff;
}

.comment-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color, #eee);
}

.comment-section h3 {
  color: var(--text-color, #333);
}

.comment-form {
  margin-bottom: 30px;
}

.comment-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid var(--border-light, #f0f0f0);
}

.comment-item.child {
  padding: 10px 0;
  border-bottom: none;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.comment-nickname {
  font-weight: 500;
  color: var(--text-color, #333);
}

.comment-nickname.blogger {
  color: #409eff;
}

.comment-time {
  color: var(--meta-color, #999);
  font-size: 12px;
}

.comment-content {
  color: var(--content-color, #555);
}

.comment-children {
  margin-top: 15px;
  padding-left: 20px;
  border-left: 2px solid var(--border-color, #eee);
}

.sidebar.toc {
  width: 250px;
  position: sticky;
  top: 80px;
  height: fit-content;
  background: var(--card-bg, #fff);
  border-radius: 8px;
  padding: 20px;
}

.toc h4 {
  margin: 0 0 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
  color: var(--text-color, #333);
}

.toc-item {
  display: block;
  padding: 6px 0;
  color: var(--link-color, #666);
  text-decoration: none;
  font-size: 14px;
}

.toc-item:hover {
  color: #409eff;
}

.toc-item.level-2 { padding-left: 15px; }
.toc-item.level-3 { padding-left: 30px; }
.toc-item.level-4 { padding-left: 45px; }

/* 夜间模式样式 */
:deep(.dark) .article-page,
.dark .article-page {
  --card-bg: #16213e;
  --text-color: #e0e0e0;
  --content-color: #c0c0c0;
  --meta-color: #8899aa;
  --border-color: #2a3f5f;
  --border-light: #2a3f5f;
  --code-bg: #0f1419;
  --code-color: #e0e0e0;
  --link-color: #a0a0a0;
}

@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
  .sidebar.toc {
    display: none;
  }
  .header-top {
    flex-direction: column;
    gap: 10px;
  }
}
</style>