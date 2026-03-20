import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const DEV_BASE_URL = ''
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_URL || DEV_BASE_URL

const client = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

client.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

client.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const authStore = useAuthStore()
    const status = error.response?.status

    if (status === 401) {
      authStore.clearSession()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }

    console.error('API Error:', {
      status,
      message: error.response?.data?.message || error.message,
      data: error.response?.data
    })

    return Promise.reject(error)
  }
)

export { API_BASE_URL, DEV_BASE_URL }
export default client
