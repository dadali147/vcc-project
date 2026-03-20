import { defineStore } from 'pinia'
import client from '@/api/client'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    adminInfo: JSON.parse(localStorage.getItem('admin_info') || '{}')
  }),
  actions: {
    async login(email, password) {
      const res = await client.post('/admin/login', { email, password })
      this.token = res.token || res.data?.token
      this.adminInfo = res.user || res.data?.user || { email }
      localStorage.setItem('admin_token', this.token)
      localStorage.setItem('admin_info', JSON.stringify(this.adminInfo))
    },
    logout() {
      this.token = ''
      this.adminInfo = {}
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_info')
    }
  }
})
