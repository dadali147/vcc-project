import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const language = ref(localStorage.getItem('language') || 'zh')
  const theme = ref(localStorage.getItem('theme') || 'light')
  const sidebarCollapsed = ref(false)

  // Actions
  function setLanguage(lang) {
    language.value = lang
    localStorage.setItem('language', lang)
  }

  function setTheme(newTheme) {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  return {
    // State
    language,
    theme,
    sidebarCollapsed,
    
    // Methods
    setLanguage,
    setTheme,
    toggleSidebar
  }
})
