<template>
  <div class="search-page">
    <div class="search-header">
      <el-input 
        v-model="keyword" 
        placeholder="输入关键词搜索..." 
        size="large" 
        @keyup.enter="handleSearch"
        @input="handleInput"
        clearable
      >
        <template #append>
          <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      
      <!-- 热门搜索词提示 -->
      <div v-if="!searched && hotKeywords.length > 0" class="hot-keywords">
        <span class="hot-label">热门搜索：</span>
        <el-tag 
          v-for="(kw, index) in hotKeywords" 
          :key="index"
          size="small" 
          class="hot-tag"
          @click="keyword = kw; handleSearch()"
        >
          {{ kw }}
        </el-tag>
      </div>
    </div>

    <div v-if="searched" class="search-filters">
      <div class="filter-group">
        <span class="filter-label">搜索范围：</span>
        <el-radio-group v-model="contentType" size="small" @change="handleSearch">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="ARTICLE">仅文章</el-radio-button>
          <el-radio-button label="FORUM_POST">仅帖子</el-radio-button>
        </el-radio-group>
      </div>
      
      <div class="filter-group">
        <span class="filter-label">排序方式：</span>
        <el-radio-group v-model="sortBy" size="small" @change="handleSearch">
          <el-radio-button label="relevance">相关度</el-radio-button>
          <el-radio-button label="time">发布时间</el-radio-button>
          <el-radio-button label="views">阅读量</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div class="search-results" v-if="searched">
      <p class="result-info">共找到 {{ total }} 条相关结果</p>
      
      <div class="result-list">
        <div 
          v-for="item in results" 
          :key="`${item.contentType}-${item.id}`" 
          class="result-card" 
          @click="goToDetail(item)"
        >
          <div class="result-header">
            <h3 class="result-title" v-html="highlightKeyword(item.title)"></h3>
            <el-tag :type="item.contentType === 'ARTICLE' ? 'primary' : 'success'" size="small">
              {{ item.contentType === 'ARTICLE' ? '文章' : '帖子' }}
            </el-tag>
          </div>
          <p class="result-excerpt" v-html="highlightKeyword(item.excerpt)"></p>
          <div class="result-meta">
            <span class="author">
              <el-icon><User /></el-icon>
              {{ item.nickname || item.username || '匿名用户' }}
            </span>
            <span class="time">
              <el-icon><Clock /></el-icon>
              {{ formatDate(item.createdAt) }}
            </span>
            <span class="views">
              <el-icon><View /></el-icon>
              {{ item.viewCount || 0 }} 阅读
            </span>
          </div>
        </div>
      </div>

      <el-pagination
        v-if="total > pageSize"
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: center;"
      />
    </div>

    <div v-else-if="!searched && !keyword" class="empty-state">
      <el-empty description="输入关键词开始搜索" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchContent } from '@/api/front'
import { Search, User, Clock, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const results = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searched = ref(false)
const contentType = ref('') // ''=全部, 'ARTICLE'=仅文章, 'FORUM_POST'=仅帖子
const sortBy = ref('relevance') // relevance, time, views
const hotKeywords = ref(['Java', 'Spring Boot', 'Vue', '前端开发', '后端开发'])

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 关键词高亮
const highlightKeyword = (text) => {
  if (!text || !keyword.value) return text
  const kw = keyword.value.trim()
  if (!kw) return text
  
  // 转义HTML特殊字符
  const escapedText = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  const escapedKeyword = kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  
  // 不区分大小写高亮
  const regex = new RegExp(`(${escapedKeyword})`, 'gi')
  return escapedText.replace(regex, '<mark>$1</mark>')
}

const handleInput = () => {
  // 输入时清除搜索结果
  if (searched.value && !keyword.value) {
    searched.value = false
    results.value = []
    total.value = 0
  }
}

const handleSearch = () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  page.value = 1
  doSearch()
}

const doSearch = async () => {
  if (!keyword.value.trim()) return
  
  try {
    const params = {
      keyword: keyword.value.trim(),
      page: page.value,
      size: pageSize.value
    }
    
    if (contentType.value) {
      params.contentType = contentType.value
    }
    
    if (sortBy.value) {
      params.sortBy = sortBy.value
    }
    
    const res = await searchContent(params)
    results.value = res.data.list || []
    total.value = res.data.total || 0
    searched.value = true
    
    // 更新URL
    const query = { q: keyword.value }
    if (contentType.value) query.type = contentType.value
    if (sortBy.value !== 'relevance') query.sort = sortBy.value
    router.replace({ query })
  } catch (e) {
    console.error('搜索失败:', e)
    ElMessage.error('搜索失败，请稍后重试')
  }
}

const handlePageChange = (p) => {
  page.value = p
  doSearch()
}

const goToDetail = (item) => {
  if (item.contentType === 'ARTICLE') {
    router.push(`/article/${item.id}`)
  } else {
    router.push(`/forum/post/${item.id}`)
  }
}

onMounted(() => {
  // 从URL参数恢复搜索
  if (route.query.q) {
    keyword.value = route.query.q
    if (route.query.type) {
      contentType.value = route.query.type
    }
    if (route.query.sort) {
      sortBy.value = route.query.sort
    }
    doSearch()
  }
})
</script>

<style scoped>
.search-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.search-header {
  margin-bottom: 30px;
}

.hot-keywords {
  margin-top: 15px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.hot-label {
  color: #666;
  font-size: 14px;
}

.hot-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.hot-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.search-filters {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  color: #666;
  font-size: 14px;
  white-space: nowrap;
}

.result-info {
  color: #666;
  margin-bottom: 20px;
  font-size: 14px;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.result-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.3s;
}

.result-card:hover {
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
  border-color: #409eff;
  transform: translateY(-2px);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
  gap: 10px;
}

.result-title {
  font-size: 1.2rem;
  margin: 0;
  color: #303133;
  flex: 1;
  line-height: 1.5;
}

.result-title :deep(mark) {
  background: #fff3cd;
  color: #856404;
  padding: 2px 4px;
  border-radius: 3px;
  font-weight: bold;
}

.result-excerpt {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-excerpt :deep(mark) {
  background: #fff3cd;
  color: #856404;
  padding: 2px 4px;
  border-radius: 3px;
  font-weight: bold;
}

.result-meta {
  margin-top: 15px;
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 13px;
  align-items: center;
}

.result-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.empty-state {
  margin-top: 50px;
}

/* 夜间模式支持 */
:deep(.dark) .result-card,
.dark .result-card {
  background: #16213e;
  border-color: #2a3f5f;
  color: #e0e0e0;
}

:deep(.dark) .result-title,
.dark .result-title {
  color: #e0e0e0;
}

:deep(.dark) .result-excerpt,
.dark .result-excerpt {
  color: #c0c0c0;
}

:deep(.dark) .result-meta,
.dark .result-meta {
  color: #8899aa;
}

:deep(.dark) .search-filters,
.dark .search-filters {
  background: #1a2942;
}
</style>
