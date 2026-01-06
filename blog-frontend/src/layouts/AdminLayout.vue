<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <span v-if="!isCollapse">博客后台</span>
        <span v-else>B</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/admin">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        
        <el-sub-menu index="article-group">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>文章管理</span>
          </template>
          <el-menu-item index="/admin/articles">
            <el-icon><Document /></el-icon>
            <template #title>文章列表</template>
          </el-menu-item>
          <el-menu-item index="/admin/article-comments">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>文章评论</template>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Folder /></el-icon>
            <template #title>分类管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/tags">
            <el-icon><PriceTag /></el-icon>
            <template #title>标签管理</template>
          </el-menu-item>
        </el-sub-menu>

        <!-- 论坛管理 -->
        <el-sub-menu index="forum-group">
          <template #title>
            <el-icon><ChatLineSquare /></el-icon>
            <span>论坛管理</span>
          </template>
          <el-menu-item index="/admin/forum-posts">
            <el-icon><Postcard /></el-icon>
            <template #title>帖子列表</template>
          </el-menu-item>
          <el-menu-item index="/admin/forum-comments">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>论坛评论</template>
          </el-menu-item>
          <el-menu-item index="/admin/forum-sections">
            <el-icon><Grid /></el-icon>
            <template #title>板块管理</template>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/media">
          <el-icon><Picture /></el-icon>
          <template #title>媒体库</template>
        </el-menu-item>
        <el-menu-item index="/admin/friend-links">
          <el-icon><Link /></el-icon>
          <template #title>友情链接</template>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部 -->
      <el-header class="header">
        <el-button :icon="isCollapse ? Expand : Fold" @click="isCollapse = !isCollapse" text />
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.avatar || undefined">
                {{ userStore.displayNickname?.charAt(0) || '未' }}
              </el-avatar>
              <span class="nickname">{{ userStore.displayNickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/admin/profile')">个人信息</el-dropdown-item>
                <el-dropdown-item @click="$router.push('/')">访问前台</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import {
  DataAnalysis, Document, Folder, PriceTag, ChatDotRound,
  Picture, Link, Setting, Expand, Fold, ChatLineSquare, Postcard, Grid, User
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    // 退出登录后刷新页面以更新UI状态
    window.location.reload()
  })
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background-color: #304156;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  background: #263445;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}

.header-right {
  margin-left: auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.nickname {
  color: #666;
}

.main {
  background: #f0f2f5;
}

.el-menu {
  border-right: none;
}
</style>
