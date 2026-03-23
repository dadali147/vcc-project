import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

// Storage keys intentionally generic to avoid revealing purpose in plain storage.
// TODO(security): Migrate to httpOnly cookie via Set-Cookie on login response
// when backend supports it. Remove localStorage usage at that point.
const TOKEN_KEY = '_t'
const USER_KEY = '_u'

function isTokenExpired(token) {
  if (!token) return true
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return Date.now() / 1000 >= payload.exp - 30
  } catch {
    return false // opaque token — let server decide
  }
}

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

  /**
   * RuoYi login flow:
   * 1. POST /login { username, password, code, uuid } → { code: 200, token: "xxx" }
   * 2. GET /getInfo → { code: 200, user: {...}, roles: [...], permissions: [...] }
   */
  async function login(credentials) {
    loading.value = true
    error.value = null
    try {
      // Step 1: Login to get token
      const loginRes = await authApi.login(credentials)
      const loginToken = loginRes.token
      if (!loginToken) {
        throw new Error(loginRes.msg || '登录失败')
      }
      setSession(loginToken)

      // Step 2: Get user info
      await getCurrentUser()
      initialized.value = true
      return loginRes
    } catch (err) {
      error.value = err.response?.data?.msg || err.message || '登录失败'
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
      if (import.meta.env.DEV) console.error('Logout error:', err)
    } finally {
      clearSession()
      initialized.value = true
    }
  }

  /**
   * RuoYi GET /getInfo returns: { code: 200, user: {...}, roles: [...], permissions: [...] }
   */
  async function getCurrentUser() {
    if (!token.value) return null

    try {
      const res = await authApi.getInfo()
      // RuoYi returns user info at top level, not nested in data
      user.value = res.user || res
      localStorage.setItem(USER_KEY, JSON.stringify(user.value))
      return user.value
    } catch (err) {
      if (import.meta.env.DEV) console.error('Failed to get current user:', err)
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

    if (isTokenExpired(token.value)) {
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

  /** Proactively validate token — used by router guard on navigation */
  async function validateToken() {
    if (!token.value || isTokenExpired(token.value)) {
      clearSession()
      return false
    }
    try {
      await getCurrentUser()
      return true
    } catch {
      clearSession()
      return false
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
    logout,
    getCurrentUser,
    initAuth,
    validateToken
  }
})

function safeParse(value) {
  if (!value) return null

  try {
    return JSON.parse(value)
  } catch (error) {
    if (import.meta.env.DEV) console.error('Failed to parse stored auth user:', error)
    return null
  }
}
