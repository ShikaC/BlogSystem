import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 验证token是否有效（检查是否过期等）
  const isValidToken = (token) => {
    if (!token) return false
    try {
      const parts = token.split('.')
      if (parts.length !== 3) return false
      
      const payload = JSON.parse(atob(parts[1]))
      const currentTime = Date.now() / 1000
      
      // 检查是否过期
      return payload.exp > currentTime
    } catch (e) {
      console.error('Token validation error:', e)
      return false
    }
  }
  
  // 获取并验证本地存储的token
  const storedToken = localStorage.getItem('token') || ''
  const token = ref(isValidToken(storedToken) ? storedToken : '')
  const nickname = ref(token.value ? (localStorage.getItem('nickname') || '') : '')
  const avatar = ref(token.value ? (localStorage.getItem('avatar') || '') : '')
  const role = ref(token.value ? (localStorage.getItem('role') || '') : '')
  const id = ref(token.value ? (localStorage.getItem('userId') || '') : '') // 添加用户ID
  
  const setUser = (data) => {
    token.value = data.token
    nickname.value = data.nickname || ''
    avatar.value = data.avatar || ''
    role.value = data.role || ''
    id.value = data.id || '' // 设置用户ID
    localStorage.setItem('token', data.token)
    localStorage.setItem('nickname', data.nickname || '')
    localStorage.setItem('avatar', data.avatar || '')
    localStorage.setItem('role', data.role || '')
    localStorage.setItem('userId', data.id || '') // 存储用户ID
    
    // 清除旧的点赞/收藏状态（避免新用户看到前一个用户的状态）
    clearLikeCollectStatus()
  }
  
  // 清除 localStorage 中的点赞/收藏状态
  const clearLikeCollectStatus = () => {
    const keys = Object.keys(localStorage)
    keys.forEach(key => {
      if (key.startsWith('liked_') || key.startsWith('collected_')) {
        localStorage.removeItem(key)
      }
    })
  }
  
  const logout = () => {
    token.value = ''
    nickname.value = ''
    avatar.value = ''
    role.value = ''
    id.value = '' // 清除用户ID
    localStorage.removeItem('token')
    localStorage.removeItem('nickname')
    localStorage.removeItem('avatar')
    localStorage.removeItem('role')
    localStorage.removeItem('userId') // 移除用户ID
    
    // 清除点赞/收藏状态
    clearLikeCollectStatus()
  }
  
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ADMIN')
  const displayNickname = computed(() => {
    if (!token.value) {
      return '未登录'
    }
    if (role.value === 'ADMIN') {
      return '超级管理员'
    }
    return nickname.value || '用户'
  })
  const userInfo = computed(() => ({
    id: id.value, // 添加ID到userInfo
    nickname: nickname.value,
    avatar: avatar.value,
    role: role.value
  }))
  
  return { token, nickname, avatar, role, id, setUser, logout, isLoggedIn, isAdmin, displayNickname, userInfo, clearLikeCollectStatus }
})