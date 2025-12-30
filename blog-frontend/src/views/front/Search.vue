<template>
  <div class="search-page">
    <div class="search-header">
      <el-input v-model="keyword" placeholder="输入关键词搜索..." size="large" @keyup.enter="handleSearch">
        <template #append>
          <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
    </div>

    <div class="search-results" v-if="searched">
      <p class="result-info">共找到 {{ total }} 篇相关文章</p>
      
      <div class="article-list">
        <article v-for="article in articles" :key="article.id" class="article-card" @click="$router.push(`/article/${article.id}`)">
          <h2>{{ article.title }}</h2>
          <p>{{ article.summary }}</p>
          <div class="meta">
            <span>{{ formatDate(article.createdAt) }}</span>
            <span>{{ article.viewCount }} 阅读</span>
          </div>
        </article>
      </div>

      <el-pagination
        v-if="total > pageSize"
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchArticles } from '@/api/front'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const articles = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searched = ref(false)

const formatDate = (dateStr) => new Date(dateStr).toLocaleDateString('zh-CN')

const handleSearch = () => {
  if (!keyword.value.trim()) return
  router.push({ query: { q: keyword.value } })
  page.value = 1
  doSearch()
}

const doSearch = async () => {
  if (!keyword.value) return
  const res = await searchArticles({ keyword: keyword.value, page: page.value, size: pageSize.value })
  articles.value = res.data.list
  total.value = res.data.total
  searched.value = true
}

const handlePageChange = (p) => {
  page.value = p
  doSearch()
}

onMounted(() => {
  if (route.query.q) {
    keyword.value = route.query.q
    doSearch()
  }
})
</script>

<style scoped>
.search-page {
  max-width: 800px;
  margin: 0 auto;
}

.search-header {
  margin-bottom: 30px;
}

.result-info {
  color: #666;
  margin-bottom: 20px;
}

.article-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.article-card:hover {
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

.article-card h2 {
  font-size: 1.2rem;
  margin-bottom: 10px;
}

.article-card p {
  color: #666;
  font-size: 14px;
}

.meta {
  margin-top: 10px;
  color: #999;
  font-size: 13px;
  display: flex;
  gap: 15px;
}
</style>
