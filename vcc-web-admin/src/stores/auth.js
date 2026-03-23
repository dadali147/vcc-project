import { defineStore } from 'pinia'
import client from '@/api/client'

// Storage key intentionally generic to avoid revealing purpose in plain storage.
// TODO(security): Migrate to httpOnly cookie via Set-Cookie on login response
// when backend supports it. Remove localStorage usage at that point.
const _SK = '_s'
const _IK = '_u'

function isTokenExpired(token) {
  if (!token) return true
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    // exp is in seconds; subtract 30s buffer to refresh before hard expiry
    return Date.now() / 1000 >= payload.exp - 30
  } catch {
    return false // non-JWT or opaque token — let server decide
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(_SK) || '',
    adminInfo: JSON.parse(localStorage.getItem(_IK) || '{}')
  }),
  actions: {
    /**
     * RuoYi 登录接口
     * POST /login  body: { username, password, code, uuid }
     * 响应: { code: 200, msg: "...", token: "xxx" }
     */
    async login(username, password, code = '', uuid = '') {
      const res = await client.post('/login', { username, password, code, uuid })
      // RuoYi 返回 token 在顶层
      this.token = res.token
      localStorage.setItem(_SK, this.token)

      // 获取用户信息
      await this.fetchAdminInfo()
    },

    async fetchAdminInfo() {
      const res = await client.get('/getInfo')
      this.adminInfo = res.user || {}
      this.roles = res.roles || []
      this.permissions = res.permissions || []
      localStorage.setItem(_IK, JSON.stringify(this.adminInfo))
    },

    logout() {
      this.token = ''
      this.adminInfo = {}
      this.roles = []
      this.permissions = []
      localStorage.removeItem(_SK)
      localStorage.removeItem(_IK)
    },

    /** Proactively validate token before guarded navigation */
    async validateToken() {
      if (!this.token) return false
      if (isTokenExpired(this.token)) {
        this.logout()
        return false
      }
      try {
        await this.fetchAdminInfo()
        return true
      } catch {
        this.logout()
        return false
      }
    }
  },
  getters: {
    isLoggedIn: (state) => !!state.token
  }
})
