<template>
  <div class="tag-page">
    <h1><el-icon><PriceTag /></el-icon> {{ tag.name }}</h1>
    
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
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getTag, getArticlesByTag } from '@/api/front'
import { PriceTag } from '@element-plus/icons-vue'

const route = useRoute()
const tag = ref({})
const articles = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatDate = (dateStr) => new Date(dateStr).toLocaleDateString('zh-CN')

const loadData = async () => {
  const tagRes = await getTag(route.params.id)
  tag.value = tagRes.data
  
  const res = await getArticlesByTag(route.params.id, { page: page.value, size: pageSize.value })
  articles.value = res.data.list
  total.value = res.data.total
}

const handlePageChange = (p) => {
  page.value = p
  loadData()
}

watch(() => route.params.id, loadData)
onMounted(loadData)
</script>

<style scoped>
.tag-page h1 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 30px;
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
