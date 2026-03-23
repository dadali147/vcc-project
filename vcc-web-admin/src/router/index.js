import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Layout from '@/components/common/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'users', name: 'Users', component: () => import('@/views/Users.vue'), meta: { title: '用户管理' } },
      { path: 'kyc', name: 'KYC', component: () => import('@/views/KYC.vue'), meta: { title: 'KYC审核' } },
      { path: 'cards', name: 'Cards', component: () => import('@/views/Cards.vue'), meta: { title: '卡管理' } },
      { path: 'card-bins', name: 'CardBINs', component: () => import('@/views/CardBINs.vue'), meta: { title: 'BIN管理' } },
      { path: 'transactions', name: 'Transactions', component: () => import('@/views/Transactions.vue'), meta: { title: '交易记录' } },
      { path: 'fee-config', name: 'FeeConfig', component: () => import('@/views/FeeConfig.vue'), meta: { title: '费率配置' } },
      { path: 'risk-config', name: 'RiskConfig', component: () => import('@/views/RiskConfig.vue'), meta: { title: '风控配置' } },
      { path: 'logs', name: 'Logs', component: () => import('@/views/Logs.vue'), meta: { title: '系统日志' } },
      { path: 'settings', name: 'Settings', component: () => import('@/views/Settings.vue'), meta: { title: '系统设置' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  if (to.name === 'Login') {
    next()
    return
  }
  if (!authStore.token) {
    next({ name: 'Login' })
    return
  }
  // Proactively validate token on first navigation (or if token may have expired)
  const valid = await authStore.validateToken()
  if (!valid) {
    next({ name: 'Login' })
  } else {
    next()
  }
})

export default router
