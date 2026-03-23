import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const Layout = () => import('@/components/common/Layout.vue')
const LoginPage = () => import('@/views/auth/LoginPage.vue')
const RegisterPage = () => import('@/views/auth/RegisterPage.vue')
const ForgotPasswordPage = () => import('@/views/auth/ForgotPasswordPage.vue')
const DashboardPage = () => import('@/views/dashboard/DashboardPage.vue')
const CardholdersPage = () => import('@/views/cardholders/CardholdersPage.vue')
const CardsPage = () => import('@/views/cards/CardsPage.vue')
const CardDetailPage = () => import('@/views/cards/CardDetailPage.vue')
const CardApplyPage = () => import('@/views/card-apply/CardApplyPage.vue')
const CardApplicationsPage = () => import('@/views/card-applications/CardApplicationsPage.vue')
const TransactionsPage = () => import('@/views/transactions/TransactionsPage.vue')
const ProfilePage = () => import('@/views/profile/ProfilePage.vue')
const KycPage = () => import('@/views/kyc/KycPage.vue')
const DownloadsPage = () => import('@/views/downloads/DownloadsPage.vue')
const SharedCardLimitsPage = () => import('@/views/shared-card-limits/SharedCardLimitsPage.vue')
const ThreeDsRecordsPage = () => import('@/views/3ds-records/ThreeDsRecordsPage.vue')
const NotFoundPage = () => import('@/views/NotFoundPage.vue')

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
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPasswordPage,
    meta: { requiresAuth: false, title: 'auth.resetPassword' }
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
      },
      {
        path: '3ds-records',
        name: 'ThreeDsRecords',
        component: ThreeDsRecordsPage,
        meta: { title: 'threeDsRecords.title', icon: 'shield' }
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

  if ((to.path === '/login' || to.path === '/register' || to.path === '/forgot-password') && authStore.isAuthenticated) {
    return '/dashboard'
  }

  return true
})

export default router
