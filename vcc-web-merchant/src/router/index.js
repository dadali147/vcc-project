import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import Layout from '@/components/common/Layout.vue'
import LoginPage from '@/views/auth/LoginPage.vue'
import RegisterPage from '@/views/auth/RegisterPage.vue'
import DashboardPage from '@/views/dashboard/DashboardPage.vue'
import CardholdersPage from '@/views/cardholders/CardholdersPage.vue'
import CardsPage from '@/views/cards/CardsPage.vue'
import CardDetailPage from '@/views/cards/CardDetailPage.vue'
import CardApplyPage from '@/views/card-apply/CardApplyPage.vue'
import CardApplicationsPage from '@/views/card-applications/CardApplicationsPage.vue'
import TransactionsPage from '@/views/transactions/TransactionsPage.vue'
import ProfilePage from '@/views/profile/ProfilePage.vue'
import KycPage from '@/views/kyc/KycPage.vue'
import DownloadsPage from '@/views/downloads/DownloadsPage.vue'
import SharedCardLimitsPage from '@/views/shared-card-limits/SharedCardLimitsPage.vue'
import NotFoundPage from '@/views/NotFoundPage.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
    meta: { requiresAuth: false, title: 'common.login' }
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterPage,
    meta: { requiresAuth: false, title: 'common.register' }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: DashboardPage,
        meta: { title: 'dashboard.title', icon: 'dashboard' }
      },
      {
        path: 'cardholders',
        name: 'Cardholders',
        component: CardholdersPage,
        meta: { title: 'cardholders.title', icon: 'user' }
      },
      {
        path: 'cards',
        name: 'Cards',
        component: CardsPage,
        meta: { title: 'cards.title', icon: 'credit-card' }
      },
      {
        path: 'cards/:id',
        name: 'CardDetail',
        component: CardDetailPage,
        meta: { title: 'cards.cardDetails' }
      },
      {
        path: 'card-apply',
        name: 'CardApply',
        component: CardApplyPage,
        meta: { title: 'cardApplication.title', icon: 'plus' }
      },
      {
        path: 'card-applications',
        name: 'CardApplications',
        component: CardApplicationsPage,
        meta: { title: 'cardApplications.title', icon: 'document' }
      },
      {
        path: 'transactions',
        name: 'Transactions',
        component: TransactionsPage,
        meta: { title: 'transactions.title', icon: 'transaction' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: ProfilePage,
        meta: { title: 'profile.title', icon: 'user' }
      },
      {
        path: 'kyc',
        name: 'KYC',
        component: KycPage,
        meta: { title: 'kyc.title', icon: 'check' }
      },
      {
        path: 'downloads',
        name: 'Downloads',
        component: DownloadsPage,
        meta: { title: 'downloads.title', icon: 'download' }
      },
      {
        path: 'shared-card-limits',
        name: 'SharedCardLimits',
        component: SharedCardLimitsPage,
        meta: { title: 'sharedCardLimits.title', icon: 'chart' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFoundPage,
    meta: { requiresAuth: false, title: 'common.pageNotFound' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (!authStore.initialized) {
    await authStore.initAuth()
  }

  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth !== false)

  if (requiresAuth && !authStore.isAuthenticated) {
    return {
      path: '/login',
      query: to.path === '/login' ? {} : { redirect: to.fullPath }
    }
  }

  if ((to.path === '/login' || to.path === '/register') && authStore.isAuthenticated) {
    return '/dashboard'
  }

  return true
})

export default router
