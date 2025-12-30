/**
 * 主题切换工具
 * 支持浅色/暗色模式切换，状态持久化到本地存储
 */

const THEME_KEY = 'blog-theme'

// 获取当前主题
export function getTheme() {
  const saved = localStorage.getItem(THEME_KEY)
  if (saved) {
    return saved
  }
  // 跟随系统偏好
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

// 设置主题
export function setTheme(theme) {
  localStorage.setItem(THEME_KEY, theme)
  applyTheme(theme)
}

// 应用主题
export function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme)
  
  // 同时设置Element Plus暗色模式
  if (theme === 'dark') {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

// 切换主题
export function toggleTheme() {
  const current = getTheme()
  const next = current === 'dark' ? 'light' : 'dark'
  setTheme(next)
  return next
}

// 初始化主题
export function initTheme() {
  const theme = getTheme()
  applyTheme(theme)
  
  // 监听系统主题变化
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
    // 只有在没有手动设置过主题时才跟随系统
    if (!localStorage.getItem(THEME_KEY)) {
      applyTheme(e.matches ? 'dark' : 'light')
    }
  })
}

export default {
  getTheme,
  setTheme,
  applyTheme,
  toggleTheme,
  initTheme
}
