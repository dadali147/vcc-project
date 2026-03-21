import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

const TOKEN_KEY = 'auth_token'
const USER_KEY = 'auth_user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || null)
  const user = ref(safeParse(localStorage.getItem(USER_KEY)))
  const loading = ref(false)
  const error = ref(null)
  const initialized = ref(false)

  const isAuthenticated = computed(() => Boolean(token.value))

  function setSession(sessionToken, sessionUser = null) {
    token.value = sessionToken || null
    user.value = sessionUser || null

    if (sessionToken) {
      localStorage.setItem(TOKEN_KEY, sessionToken)
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }

    if (sessionUser) {
      localStorage.setItem(USER_KEY, JSON.stringify(sessionUser))
    } else {
      localStorage.removeItem(USER_KEY)
    }
  }

  function clearSession() {
    setSession(null, null)
  }

  async function login(credentials) {
    loading.value = true
    error.value = null
    try {
      const response = await authApi.login(credentials)
      // Support both { token, user } and { data: { token, user } } response formats
      const token = response.data?.token || response.token
      const userInfo = response.data?.user || response.user
      setSession(token, userInfo)
      initialized.value = true
      return response
    } catch (err) {
      error.value = err.response?.data?.message || 'Login failed'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function register(userData) {
    loading.value = true
    error.value = null
    try {
      const response = await authApi.register(userData)
      // Support both { token, user } and { data: { token, user } } response formats
      const token = response.data?.token || response.token
      const userInfo = response.data?.user || response.user
      setSession(token, userInfo)
      initialized.value = true
      return response
    } catch (err) {
      error.value = err.response?.data?.message || 'Registration failed'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    try {
      if (token.value) {
        await authApi.logout()
      }
    } catch (err) {
      console.error('Logout error:', err)
    } finally {
      clearSession()
      initialized.value = true
    }
  }

  async function getCurrentUser() {
    if (!token.value) return null

    try {
      const response = await authApi.getCurrentUser()
      user.value = response
      localStorage.setItem(USER_KEY, JSON.stringify(response))
      return response
    } catch (err) {
      console.error('Failed to get current user:', err)
      if (err.response?.status === 401) {
        clearSession()
      }
      throw err
    }
  }

  async function initAuth() {
    if (initialized.value) return

    if (!token.value) {
      clearSession()
      initialized.value = true
      return
    }

    try {
      await getCurrentUser()
    } catch (err) {
      // 401 handled in interceptor/store, keep graceful init
    } finally {
      initialized.value = true
    }
  }

  return {
    token,
    user,
    loading,
    error,
    initialized,
    isAuthenticated,
    setSession,
    clearSession,
    login,
    register,
    logout,
    getCurrentUser,
    initAuth
  }
})

function safeParse(value) {
  if (!value) return null

  try {
    return JSON.parse(value)
  } catch (error) {
    console.error('Failed to parse stored auth user:', error)
    return null
  }
}
