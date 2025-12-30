import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const nickname = ref(localStorage.getItem('nickname') || '')
  const avatar = ref(localStorage.getItem('avatar') || '')
  
  const setUser = (data) => {
    token.value = data.token
    nickname.value = data.nickname || ''
    avatar.value = data.avatar || ''
    localStorage.setItem('token', data.token)
    localStorage.setItem('nickname', data.nickname || '')
    localStorage.setItem('avatar', data.avatar || '')
  }
  
  const logout = () => {
    token.value = ''
    nickname.value = ''
    avatar.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('nickname')
    localStorage.removeItem('avatar')
  }
  
  const isLoggedIn = () => !!token.value
  
  return { token, nickname, avatar, setUser, logout, isLoggedIn }
})
