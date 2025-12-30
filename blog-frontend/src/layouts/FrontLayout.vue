<template>
  <div class="front-layout" :class="{ 'dark': themeStore.isDark }">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="container">
        <div class="logo" @click="$router.push('/')">
          <span>{{ siteName }}</span>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-item">首页</router-link>
          <router-link to="/forum" class="nav-item">论坛</router-link>
          <router-link to="/archives" class="nav-item">归档</router-link>
          <router-link to="/about" class="nav-item">关于</router-link>
        </nav>
        <div class="header-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索文章/帖子..."
            class="search-input"
            @keyup.enter="handleSearch"
            :prefix-icon="Search"
          />
          <el-button :icon="themeStore.isDark ? Sunny : Moon" circle @click="themeStore.toggleTheme" />
          
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleUserCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo.avatar" />
                <span class="nickname">{{ userStore.userInfo.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="article">发布文章</el-dropdown-item>
                  <el-dropdown-item command="post">发布帖子</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin">系统后台</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" link @click="router.push('/login')">登录</el-button>
          </template>
        </div>
        <el-button class="mobile-menu-btn" :icon="Menu" @click="showMobileMenu = true" />
      </div>
    </header>

    <!-- 移动端菜单 -->
    <el-drawer v-model="showMobileMenu" direction="rtl" size="70%">
      <div class="mobile-nav">
        <router-link to="/" class="mobile-nav-item" @click="showMobileMenu = false">首页</router-link>
        <router-link to="/archives" class="mobile-nav-item" @click="showMobileMenu = false">归档</router-link>
        <router-link to="/about" class="mobile-nav-item" @click="showMobileMenu = false">关于</router-link>
        <router-link to="/links" class="mobile-nav-item" @click="showMobileMenu = false">友链</router-link>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文章..."
          class="mobile-search"
          @keyup.enter="handleSearch"
        />
      </div>
    </el-drawer>

    <!-- 主内容 -->
    <main class="main">
      <div class="container">
        <router-view />
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <p>{{ siteFooter }}</p>
        <p v-if="siteIcp">{{ siteIcp }}</p>
      </div>
    </footer>

    <!-- 回到顶部 -->
    <el-backtop :right="20" :bottom="20" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { getFrontConfig } from '@/api/front'
import { Search, Sunny, Moon, Menu } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const themeStore = useThemeStore()
const userStore = useUserStore()

const searchKeyword = ref('')
const showMobileMenu = ref(false)
const siteName = ref('我的博客')
const siteFooter = ref('')
const siteIcp = ref('')

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'Search', query: { q: searchKeyword.value } })
    showMobileMenu.value = false
  }
}

const handleUserCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } else if (command === 'profile') {
    router.push('/user/profile')
  } else if (command === 'article') {
    router.push('/user/article/edit')
  } else if (command === 'post') {
    router.push('/user/post/edit')
  } else if (command === 'admin') {
    router.push('/admin')
  }
}

onMounted(async () => {
  try {
    const res = await getFrontConfig()
    siteName.value = res.data?.site_name || '我的博客'
    siteFooter.value = res.data?.site_footer || ''
    siteIcp.value = res.data?.site_icp || ''
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-color, #f5f7fa);
  transition: background 0.3s;
}

.front-layout.dark {
  --bg-color: #1a1a2e;
  --text-color: #e0e0e0;
  --card-bg: #16213e;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.dark .header {
  background: #16213e;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header .container {
  display: flex;
  align-items: center;
  height: 60px;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}

.nav {
  display: flex;
  margin-left: 40px;
  gap: 30px;
}

.nav-item {
  color: #666;
  text-decoration: none;
  transition: color 0.3s;
}

.nav-item:hover, .nav-item.router-link-active {
  color: #409eff;
}

.dark .nav-item {
  color: #aaa;
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input {
  width: 200px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 0 10px;
}

.nickname {
  font-size: 14px;
  color: #666;
}

.dark .nickname {
  color: #aaa;
}

.mobile-menu-btn {
  display: none;
  margin-left: auto;
}

.main {
  flex: 1;
  padding: 20px 0;
}

.footer {
  background: #fff;
  padding: 20px;
  text-align: center;
  color: #666;
  font-size: 14px;
}

.dark .footer {
  background: #16213e;
  color: #aaa;
}

.mobile-nav {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
}

.mobile-nav-item {
  font-size: 18px;
  color: #333;
  text-decoration: none;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

@media (max-width: 768px) {
  .nav, .header-right {
    display: none;
  }
  .mobile-menu-btn {
    display: block;
  }
  .container {
    padding: 0 15px;
  }
}
</style>
