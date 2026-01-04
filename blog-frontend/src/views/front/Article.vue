<template>
  <div class="article-page" v-if="article">
    <div class="content-wrapper">
      <!-- 文章主体 -->
      <article class="article-main">
        <header class="article-header">
          <h1>{{ article.title }}</h1>
          <div class="article-meta">
            <span><el-icon><Calendar /></el-icon> {{ formatDate(article.createdAt) }}</span>
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
                <el-row :gutter="10">
                  <el-col :span="8">
                    <el-input v-model="commentForm.nickname" placeholder="昵称（可选）" />
                  </el-col>
                  <el-col :span="8">
                    <el-input v-model="commentForm.email" placeholder="邮箱（可选）" />
                  </el-col>
                  <el-col :span="8">
                    <el-input v-model="commentForm.website" placeholder="网站（可选）" />
                  </el-col>
                </el-row>
                <el-input
                  v-model="commentForm.content"
                  type="textarea"
                  :rows="4"
                  placeholder="说点什么..."
                  style="margin-top: 10px;"
                />
                <div style="margin-top: 10px; display: flex; gap: 10px; align-items: center;">
                  <el-input v-model="commentForm.captcha" placeholder="验证码" style="width: 120px;" />
                  <img :src="captchaImg" @click="refreshCaptcha" style="height: 40px; cursor: pointer;" />
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
import { getArticle, getRelatedArticles, getArticleComments, likeArticle, unlikeArticle, collectArticle, uncollectArticle, createComment } from '@/api/front'
import { getCaptcha } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { Calendar, View, Folder, Timer, Star, Collection } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
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
const captchaImg = ref('')
const captchaKey = ref('')

const commentForm = ref({
  nickname: '',
  email: '',
  website: '',
  content: '',
  captcha: ''
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const refreshCaptcha = async () => {
  const res = await getCaptcha()
  captchaImg.value = res.data.image
  captchaKey.value = res.data.key
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
      ...commentForm.value,
      captchaKey: captchaKey.value
    })
    ElMessage.success('评论成功')
    commentForm.value.content = ''
    commentForm.value.captcha = ''
    loadComments()
    refreshCaptcha()
  } catch (e) {
    console.error(e)
    refreshCaptcha()
  } finally {
    submitting.value = false
  }
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
    
    // 加载验证码
    refreshCaptcha()
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
  background: #fff;
  border-radius: 8px;
  padding: 30px;
}

.article-header h1 {
  font-size: 2rem;
  margin-bottom: 15px;
}

.article-meta {
  display: flex;
  gap: 20px;
  color: #999;
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
  color: #333;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4) {
  margin-top: 30px;
  margin-bottom: 15px;
}

.article-content :deep(pre) {
  background: #f6f8fa;
  padding: 15px;
  border-radius: 6px;
  overflow-x: auto;
}

.article-content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}

.article-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 15px;
}

.related-articles {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.related-articles h3 {
  margin-bottom: 15px;
}

.related-item {
  padding: 10px 0;
  cursor: pointer;
  border-bottom: 1px dashed #eee;
}

.related-item:hover {
  color: #409eff;
}

.comment-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.comment-form {
  margin-bottom: 30px;
}

.comment-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
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
}

.comment-nickname.blogger {
  color: #409eff;
}

.comment-time {
  color: #999;
  font-size: 12px;
}

.comment-children {
  margin-top: 15px;
  padding-left: 20px;
  border-left: 2px solid #eee;
}

.sidebar.toc {
  width: 250px;
  position: sticky;
  top: 80px;
  height: fit-content;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.toc h4 {
  margin: 0 0 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
}

.toc-item {
  display: block;
  padding: 6px 0;
  color: #666;
  text-decoration: none;
  font-size: 14px;
}

.toc-item:hover {
  color: #409eff;
}

.toc-item.level-2 { padding-left: 15px; }
.toc-item.level-3 { padding-left: 30px; }
.toc-item.level-4 { padding-left: 45px; }

@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
  .sidebar.toc {
    display: none;
  }
}
</style>
