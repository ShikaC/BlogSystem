import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const nickname = ref(localStorage.getItem('nickname') || '')
  const avatar = ref(localStorage.getItem('avatar') || '')
  const role = ref(localStorage.getItem('role') || '')
  
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
  const userInfo = computed(() => ({
    nickname: nickname.value,
    avatar: avatar.value,
    role: role.value
  }))
  
  return { token, nickname, avatar, role, setUser, logout, isLoggedIn, isAdmin, userInfo }
})
