import { createRouter, createWebHistory } from 'vue-router'
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('../layouts/MainLayout.vue'),
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: () => import('../views/Dashboard.vue') },
        { path: 'cards', component: () => import('../views/Cards.vue') },
        { path: 'users', component: () => import('../views/Users.vue') }
      ]
    },
    { path: '/login', component: () => import('../views/Login.vue') }
  ]
})
export default router
