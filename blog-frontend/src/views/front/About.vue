<template>
  <div class="about-page">
    <div class="about-card">
      <el-avatar :size="120" :src="blogger.avatar || undefined">{{ (blogger.displayNickname || '未').charAt(0) }}</el-avatar>
      <h1>{{ blogger.displayNickname || '未登录' }}</h1>
      <p class="bio">{{ blogger.bio || '请先登录以查看个人信息' }}</p>
      
      <div class="social-links">
        <a v-if="blogger.github" :href="blogger.github" target="_blank">
          <el-icon :size="24"><Link /></el-icon> GitHub
        </a>
        <a v-if="blogger.zhihu" :href="blogger.zhihu" target="_blank">
          <el-icon :size="24"><Link /></el-icon> 知乎
        </a>
        <span v-if="blogger.email">
          <el-icon :size="24"><Message /></el-icon> {{ blogger.email }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBloggerInfo, getUserInfo } from '@/api/front'
import { useUserStore } from '@/stores/user'
import { Link, Message } from '@element-plus/icons-vue'

const userStore = useUserStore()
const blogger = ref({})

onMounted(async () => {
  if (userStore.isLoggedIn) {
    const res = await getUserInfo()
    const userData = res.data || {}
    blogger.value = {
      ...userData,
      displayNickname: userStore.displayNickname
    }
  } else {
    // 未登录时显示“未登录”
    blogger.value = { 
      displayNickname: '未登录',
      nickname: '未登录', 
      bio: '请先登录以查看个人信息', 
      avatar: null, 
      github: null, 
      zhihu: null, 
      email: null 
    }
  }
})
</script>

<style scoped>
.about-page {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.about-card {
  background: #fff;
  border-radius: 12px;
  padding: 60px;
  text-align: center;
  max-width: 600px;
  width: 100%;
}

.about-card h1 {
  margin: 20px 0 10px;
}

.bio {
  color: #666;
  font-size: 16px;
  line-height: 1.8;
  margin-bottom: 30px;
}

.social-links {
  display: flex;
  justify-content: center;
  gap: 30px;
  flex-wrap: wrap;
}

.social-links a, .social-links span {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  text-decoration: none;
}

.social-links a:hover {
  color: #409eff;
}
</style>
