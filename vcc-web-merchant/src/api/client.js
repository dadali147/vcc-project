import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const DEV_BASE_URL = ''
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_URL || DEV_BASE_URL

// TODO(security): Migrate token transport to httpOnly cookie.
// When backend issues Set-Cookie on login, remove Authorization header injection
// and remove localStorage token storage in auth store.
const client = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor - RuoYi uses Bearer token in Authorization header
client.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers['Authorization'] = 'Bearer ' + authStore.token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor - RuoYi AjaxResult format: { code, msg, ... }
client.interceptors.response.use(
  (response) => {
    const res = response.data
    // RuoYi standard: code 200 = success, 401 = unauthorized
    if (res.code === 401) {
      const authStore = useAuthStore()
      authStore.clearSession()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.msg || '认证失败'))
    }
    return res
  },
  async (error) => {
    const status = error.response?.status
    if (status === 401) {
      const authStore = useAuthStore()
      authStore.clearSession()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }

    if (import.meta.env.DEV) {
      console.error('API Error:', {
        status,
        message: error.response?.data?.message || error.message,
        data: error.response?.data
      })
    }

    return Promise.reject(error)
  }
)

export { API_BASE_URL, DEV_BASE_URL }
export default client
