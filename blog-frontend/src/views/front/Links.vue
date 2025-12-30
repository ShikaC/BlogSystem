<template>
  <div class="links-page">
    <h1><el-icon><Link /></el-icon> 友情链接</h1>
    
    <div class="links-grid">
      <a v-for="link in links" :key="link.id" :href="link.url" target="_blank" class="link-card">
        <el-avatar :size="60" :src="link.logo || undefined">{{ link.name?.charAt(0) }}</el-avatar>
        <div class="link-info">
          <h3>{{ link.name }}</h3>
          <p>{{ link.description || link.url }}</p>
        </div>
      </a>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFriendLinks } from '@/api/front'
import { Link } from '@element-plus/icons-vue'

const links = ref([])

onMounted(async () => {
  const res = await getFriendLinks()
  links.value = res.data || []
})
</script>

<style scoped>
.links-page h1 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 30px;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.link-card {
  display: flex;
  align-items: center;
  gap: 15px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
  transition: transform 0.3s, box-shadow 0.3s;
}

.link-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}

.link-info h3 {
  margin: 0 0 5px;
  font-size: 16px;
}

.link-info p {
  margin: 0;
  color: #999;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
