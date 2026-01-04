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
  
  const setUser = (data) => {
    token.value = data.token
    nickname.value = data.nickname || ''
    avatar.value = data.avatar || ''
    role.value = data.role || ''
    localStorage.setItem('token', data.token)
    localStorage.setItem('nickname', data.nickname || '')
    localStorage.setItem('avatar', data.avatar || '')
    localStorage.setItem('role', data.role || '')
  }
  
  const logout = () => {
    token.value = ''
    nickname.value = ''
    avatar.value = ''
    role.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('nickname')
    localStorage.removeItem('avatar')
    localStorage.removeItem('role')
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
    nickname: nickname.value,
    avatar: avatar.value,
    role: role.value
  }))
  
  return { token, nickname, avatar, role, setUser, logout, isLoggedIn, isAdmin, displayNickname, userInfo }
})
