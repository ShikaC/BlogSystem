import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  // 前台路由
  {
    path: '/',
    component: () => import('@/layouts/FrontLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/front/Home.vue') },
      { path: 'forum', name: 'Forum', component: () => import('@/views/front/Forum.vue') },
      { path: 'forum/post/:id', name: 'PostDetail', component: () => import('@/views/front/PostDetail.vue') },
      { path: 'article/:id', name: 'Article', component: () => import('@/views/front/Article.vue') },
      { path: 'category/:id', name: 'Category', component: () => import('@/views/front/Category.vue') },
      { path: 'tag/:id', name: 'Tag', component: () => import('@/views/front/Tag.vue') },
      { path: 'archives', name: 'Archives', component: () => import('@/views/front/Archives.vue') },
      { path: 'search', name: 'Search', component: () => import('@/views/front/Search.vue') },
      { path: 'about', name: 'About', component: () => import('@/views/front/About.vue') },
      { path: 'links', name: 'Links', component: () => import('@/views/front/Links.vue') },
      // 用户中心 -- 仅自己可见
      { path: 'user/profile', name: 'UserProfile', component: () => import('@/views/front/UserCenter.vue'), meta: { requiresAuth: true } },
      { path: 'user/article/edit/:id?', name: 'UserArticleEdit', component: () => import('@/views/admin/ArticleEdit.vue'), meta: { requiresAuth: true } },
      { path: 'user/post/edit/:id?', name: 'UserPostEdit', component: () => import('@/views/front/Forum.vue'), meta: { requiresAuth: true } },
      // 公开用户主页 -- 所有人可见 (放在最后，避免覆盖上面的路由)
      { path: 'user/:userId', name: 'PublicUserProfile', component: () => import('@/views/front/PublicUserProfile.vue') }
    ]
  },
  // 登录/注册页
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/Login.vue') },
  // 后台路由
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'articles', name: 'AdminArticles', component: () => import('@/views/admin/Articles.vue') },
      { path: 'article/edit/:id?', name: 'ArticleEdit', component: () => import('@/views/admin/ArticleEdit.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('@/views/admin/Categories.vue') },
      { path: 'tags', name: 'AdminTags', component: () => import('@/views/admin/Tags.vue') },
      { path: 'comments', name: 'AdminComments', component: () => import('@/views/admin/Comments.vue') },
      { path: 'media', name: 'AdminMedia', component: () => import('@/views/admin/Media.vue') },
      { path: 'friend-links', name: 'AdminFriendLinks', component: () => import('@/views/admin/FriendLinks.vue') },
      { path: 'settings', name: 'AdminSettings', component: () => import('@/views/admin/Settings.vue') },
      { path: 'profile', name: 'AdminProfile', component: () => import('@/views/admin/Profile.vue') }
    ]
  },
  // 404
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!userStore.isLoggedIn) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
    } else {
      // 访问后台需要管理员权限
      if (to.path.startsWith('/admin') && !userStore.isAdmin) {
        next({ name: 'Home' })
      } else {
        next()
      }
    }
  } else {
    next()
  }
})

export default router
