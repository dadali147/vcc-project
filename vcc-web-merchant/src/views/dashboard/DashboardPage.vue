<template>
  <div class="page">
    <h1>{{ $t('dashboard.title') }}</h1>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else class="dashboard-grid">
      <div class="stat-card">
        <div class="stat-label">{{ $t('dashboard.totalCards') }}</div>
        <div class="stat-value">{{ stats.totalCards }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">{{ $t('dashboard.totalBalance') }}</div>
        <div class="stat-value">${{ stats.totalBalance.toFixed(2) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">{{ $t('dashboard.monthlyTransactions') }}</div>
        <div class="stat-value">{{ stats.monthlyTransactions }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">{{ $t('dashboard.pendingApplications') }}</div>
        <div class="stat-value">{{ stats.pendingApplications }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { cardApi } from '@/api'

const loading = ref(false)
const stats = ref({
  totalCards: 0,
  totalBalance: 0,
  monthlyTransactions: 0,
  pendingApplications: 0
})

onMounted(async () => {
  loading.value = true
  try {
    const [cardsRes, applicationsRes] = await Promise.all([
      cardApi.list({ page: 1, pageSize: 1 }),
      cardApi.getApplications({ page: 1, pageSize: 1, status: 'pending' })
    ])
    stats.value.totalCards = cardsRes.total || 0
    stats.value.totalBalance = cardsRes.totalBalance || 0
    stats.value.monthlyTransactions = cardsRes.monthlyTransactions || 0
    stats.value.pendingApplications = applicationsRes.total || 0
  } catch (err) {
    console.error('Failed to load dashboard stats:', err)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page {
  padding: 24px;
}

.page h1 {
  margin-bottom: 32px;
  color: #111827;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #3B82F6;
}
</style>
