<template>
  <div class="archives-page">
    <h1><el-icon><Calendar /></el-icon> 文章归档</h1>
    
    <div class="timeline">
      <div v-for="archive in archives" :key="`${archive.year}-${archive.month}`" class="timeline-item">
        <div class="timeline-date" @click="toggleArchive(archive)">
          <span>{{ archive.year }}年{{ archive.month }}月</span>
          <span class="count">{{ archive.count }} 篇</span>
          <el-icon :class="{ expanded: archive.expanded }"><ArrowDown /></el-icon>
        </div>
        <div class="timeline-articles" v-show="archive.expanded">
          <div v-for="article in archive.articles" :key="article.id" class="article-item" @click="$router.push(`/article/${article.id}`)">
            <span class="date">{{ formatDay(article.createdAt) }}</span>
            <span class="title">{{ article.title }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArchives, getArticlesByYearMonth } from '@/api/front'
import { Calendar, ArrowDown } from '@element-plus/icons-vue'

const archives = ref([])

const formatDay = (dateStr) => {
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}-${d.getDate()}`
}

const toggleArchive = async (archive) => {
  if (!archive.articles) {
    const res = await getArticlesByYearMonth(archive.year, archive.month, { page: 1, size: 100 })
    archive.articles = res.data.list
  }
  archive.expanded = !archive.expanded
}

onMounted(async () => {
  const res = await getArchives()
  archives.value = (res.data || []).map(a => ({ ...a, expanded: false, articles: null }))
})
</script>

<style scoped>
.archives-page h1 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 30px;
}

.timeline {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.timeline-item {
  margin-bottom: 15px;
}

.timeline-date {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
}

.timeline-date:hover {
  background: #e8f4ff;
}

.count {
  color: #999;
  font-size: 14px;
}

.timeline-date .el-icon {
  margin-left: auto;
  transition: transform 0.3s;
}

.timeline-date .el-icon.expanded {
  transform: rotate(180deg);
}

.timeline-articles {
  padding: 10px 15px;
}

.article-item {
  display: flex;
  gap: 15px;
  padding: 10px 0;
  cursor: pointer;
  border-bottom: 1px dashed #eee;
}

.article-item:hover .title {
  color: #409eff;
}

.article-item .date {
  color: #999;
  font-size: 14px;
}

.article-item .title {
  flex: 1;
}
</style>
