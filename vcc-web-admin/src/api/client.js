import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

// TODO(security): Migrate token transport to httpOnly cookie.
// When backend issues Set-Cookie on login, remove Authorization header injection
// and remove localStorage token storage in auth store.
const client = axios.create({
  baseURL: ''
})

client.interceptors.request.use(config => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

client.interceptors.response.use(
  response => {
    const res = response.data
    // RuoYi 标准响应: { code: 200, msg: "...", data/token/... }
    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      router.push('/login')
    }
    const msg = error.response?.data?.msg || error.response?.data?.message || '请求失败'
    ElMessage.error(msg)
    if (import.meta.env.DEV) {
      console.error('[API Error]', error.response?.status, msg)
    }
    return Promise.reject(error)
  }
)

export default client
