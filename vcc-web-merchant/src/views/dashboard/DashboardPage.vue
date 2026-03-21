<template>
  <div class="page-shell">
    <div class="page-header">
      <h1>{{ t('dashboard.title') }}</h1>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in statCards" :key="stat.key">
        <div class="stat-icon" :style="{ background: stat.bg }">{{ stat.icon }}</div>
        <div class="stat-body">
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-value" v-if="!loading">{{ stat.value }}</div>
          <div class="stat-skeleton" v-else></div>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('dashboard.quickActions') }}</h2>
      </div>
      <div class="actions-grid">
        <button class="action-item" @click="$router.push('/card-apply')">
          <span class="action-icon">💳</span>
          <span>{{ t('dashboard.applyCard') }}</span>
        </button>
        <button class="action-item" @click="$router.push('/cards')">
          <span class="action-icon">📋</span>
          <span>{{ t('dashboard.viewCards') }}</span>
        </button>
        <button class="action-item" @click="$router.push('/kyc')">
          <span class="action-icon">🔒</span>
          <span>{{ t('dashboard.kycVerification') }}</span>
        </button>
        <button class="action-item" @click="$router.push('/downloads')">
          <span class="action-icon">📥</span>
          <span>{{ t('dashboard.downloadReport') }}</span>
        </button>
      </div>
    </section>

    <!-- Recent Transactions -->
    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('dashboard.recentTransactions') }}</h2>
        <button class="link-button" @click="$router.push('/transactions')">{{ t('common.all') }}</button>
      </div>
      <div v-if="loadingTx" class="loading-state">{{ t('common.loading') }}</div>
      <div v-else-if="recentTransactions.length" class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>{{ t('transactions.date') }}</th>
              <th>{{ t('transactions.card') }}</th>
              <th>{{ t('transactions.merchant') }}</th>
              <th>{{ t('transactions.amount') }}</th>
              <th>{{ t('transactions.status') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tx in recentTransactions" :key="tx.id">
              <td>{{ tx.createdAt }}</td>
              <td>{{ tx.cardNumber || tx.cardNoMask }}</td>
              <td>{{ tx.merchant }}</td>
              <td :class="tx.amount >= 0 ? 'amount-positive' : 'amount-negative'">
                {{ formatAmount(tx.amount) }}
              </td>
              <td><span class="status-pill" :class="tx.status">{{ tx.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="empty-state">{{ t('transactions.noTransactions') }}</div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { cardApi, transactionApi } from '@/api'

const { t } = useI18n()

const loading = ref(false)
const loadingTx = ref(false)
const recentTransactions = ref([])

const stats = ref({
  totalCards: 0,
  totalBalance: 0,
  monthlyTransactions: 0,
  pendingApplications: 0
})

const statCards = computed(() => [
  {
    key: 'cards',
    icon: '💳',
    bg: '#EFF6FF',
    label: t('dashboard.totalCards'),
    value: stats.value.totalCards
  },
  {
    key: 'balance',
    icon: '💰',
    bg: '#F0FDF4',
    label: t('dashboard.totalBalance'),
    value: '$' + Number(stats.value.totalBalance || 0).toFixed(2)
  },
  {
    key: 'transactions',
    icon: '📊',
    bg: '#FFF7ED',
    label: t('dashboard.monthlyTransactions'),
    value: stats.value.monthlyTransactions
  },
  {
    key: 'pending',
    icon: '⏳',
    bg: '#FEF3C7',
    label: t('dashboard.pendingApplications'),
    value: stats.value.pendingApplications
  }
])

const formatAmount = (amount) => {
  const prefix = amount > 0 ? '+' : ''
  return prefix + new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount || 0)
}

const loadStats = async () => {
  loading.value = true
  try {
    const [cardsRes, appsRes] = await Promise.all([
      cardApi.list({ page: 1, pageSize: 1 }),
      cardApi.getApplications({ page: 1, pageSize: 1, status: 'pending' })
    ])
    stats.value.totalCards = cardsRes.data?.total || cardsRes.total || 0
    stats.value.totalBalance = cardsRes.data?.totalBalance || cardsRes.totalBalance || 0
    stats.value.monthlyTransactions = cardsRes.data?.monthlyTransactions || cardsRes.monthlyTransactions || 0
    stats.value.pendingApplications = appsRes.data?.total || appsRes.total || 0
  } catch (err) {
    console.error('Failed to load stats:', err)
  } finally {
    loading.value = false
  }
}

const loadRecentTransactions = async () => {
  loadingTx.value = true
  try {
    const res = await transactionApi.list({ page: 1, pageSize: 5 })
    recentTransactions.value = res.data?.items || res.data || []
  } catch (err) {
    console.error('Failed to load recent transactions:', err)
  } finally {
    loadingTx.value = false
  }
}

onMounted(() => {
  loadStats()
  loadRecentTransactions()
})
</script>

<style scoped>
.page-shell { display: flex; flex-direction: column; gap: 20px; }
.page-header h1 { margin: 0; color: #111827; }
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-card {
  background: white; border: 1px solid #e5e7eb; border-radius: 16px; padding: 20px;
  display: flex; align-items: center; gap: 16px;
}
.stat-icon { width: 48px; height: 48px; border-radius: 12px; font-size: 22px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-label { font-size: 13px; color: #6b7280; margin-bottom: 6px; }
.stat-value { font-size: 22px; font-weight: 700; color: #111827; }
.stat-skeleton { height: 28px; background: #f3f4f6; border-radius: 6px; width: 80px; animation: pulse 1.5s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
.panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 16px; padding: 20px; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.panel-header h2 { margin: 0; color: #111827; }
.actions-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.action-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 20px; border: 1px solid #e5e7eb; border-radius: 12px; background: #fafafa;
  cursor: pointer; transition: all 0.2s; font-size: 14px; font-weight: 600; color: #374151;
}
.action-item:hover { border-color: #2563eb; background: #eff6ff; color: #2563eb; }
.action-icon { font-size: 28px; }
.table-wrapper { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 12px; border-bottom: 1px solid #f3f4f6; text-align: left; font-size: 14px; }
th { color: #6b7280; font-weight: 600; }
.status-pill { display: inline-flex; align-items: center; padding: 2px 8px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.status-pill.success { background: #d1fae5; color: #065f46; }
.status-pill.pending { background: #fef3c7; color: #92400e; }
.status-pill.failed { background: #fee2e2; color: #991b1b; }
.amount-positive { color: #059669; font-weight: 600; }
.amount-negative { color: #dc2626; font-weight: 600; }
.empty-state, .loading-state { text-align: center; padding: 32px; color: #6b7280; }
.link-button { border: none; background: transparent; color: #2563eb; cursor: pointer; font-weight: 600; }
@media (max-width: 1024px) {
  .stats-grid, .actions-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
  .stats-grid, .actions-grid { grid-template-columns: 1fr; }
}
</style>
