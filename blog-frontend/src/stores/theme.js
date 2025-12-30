import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getTheme, setTheme, toggleTheme as toggle } from '@/utils/theme'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref(getTheme())
  const isDark = computed(() => theme.value === 'dark')
  
  const toggleTheme = () => {
    theme.value = toggle()
  }
  
  const setThemeMode = (mode) => {
    theme.value = mode
    setTheme(mode)
  }
  
  return { theme, isDark, toggleTheme, setThemeMode }
})
