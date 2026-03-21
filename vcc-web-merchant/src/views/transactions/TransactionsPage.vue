<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1>{{ t('transactions.title') }}</h1>
        <p>{{ t('transactions.pageDescription') }}</p>
      </div>
      <button class="primary-button" @click="handleExport">{{ t('transactions.export') }}</button>
    </div>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('common.filter') }}</h2>
        <button class="link-button" @click="resetFilters">{{ t('common.reset') }}</button>
      </div>
      <div class="filter-grid">
        <div class="form-group">
          <label>{{ t('transactions.card') }}</label>
          <input v-model="filters.cardNumber" type="text" :placeholder="t('transactions.cardPlaceholder')" @input="handleSearch" />
        </div>
        <div class="form-group">
          <label>{{ t('transactions.typeFilter') }}</label>
          <select v-model="filters.type" @change="loadTransactions">
            <option value="">{{ t('common.all') }}</option>
            <option value="recharge">{{ t('transactions.typeRecharge') }}</option>
            <option value="consumption">{{ t('transactions.typeConsumption') }}</option>
            <option value="refund">{{ t('transactions.typeRefund') }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>{{ t('transactions.status') }}</label>
          <select v-model="filters.status" @change="loadTransactions">
            <option value="">{{ t('common.all') }}</option>
            <option value="success">{{ t('transactions.statusSuccess') }}</option>
            <option value="pending">{{ t('transactions.statusPending') }}</option>
            <option value="failed">{{ t('transactions.statusFailed') }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>{{ t('transactions.dateRange') }}</label>
          <input v-model="filters.dateRange" type="text" :placeholder="t('common.dateRangePlaceholder')" />
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('transactions.list') }}</h2>
        <span class="meta">{{ pagination.total }}</span>
      </div>
      <div v-if="loading" class="loading-state">{{ t('common.loading') }}</div>
      <div v-else-if="transactions.length" class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>{{ t('transactions.transactionId') }}</th>
              <th>{{ t('transactions.card') }}</th>
              <th>{{ t('transactions.type') }}</th>
              <th>{{ t('transactions.amount') }}</th>
              <th>{{ t('transactions.merchant') }}</th>
              <th>{{ t('transactions.status') }}</th>
              <th>{{ t('transactions.date') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in transactions" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.cardNumber }}</td>
              <td>{{ getTypeLabel(item.type) }}</td>
              <td :class="{ 'amount-positive': item.amount > 0, 'amount-negative': item.amount < 0 }">
                {{ formatAmount(item.amount) }}
              </td>
              <td>{{ item.merchant }}</td>
              <td><span class="status-pill" :class="item.status">{{ getStatusLabel(item.status) }}</span></td>
              <td>{{ item.createdAt }}</td>
            </tr>
          </tbody>
        </table>
        <div class="pagination">
          <button :disabled="pagination.page === 1" @click="changePage(pagination.page - 1)">{{ t('common.prev') }}</button>
          <span>{{ pagination.page }} / {{ Math.ceil(pagination.total / pagination.pageSize) || 1 }}</span>
          <button :disabled="pagination.page >= Math.ceil(pagination.total / pagination.pageSize)" @click="changePage(pagination.page + 1)">{{ t('common.next') }}</button>
        </div>
      </div>
      <div v-else class="empty-state">
        <strong>{{ t('transactions.emptyTitle') }}</strong>
        <p>{{ t('transactions.noTransactions') }}</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { transactionApi } from '@/api'

const { t } = useI18n()

const loading = ref(false)
const transactions = ref([])

const filters = reactive({
  cardNumber: '',
  type: '',
  status: '',
  dateRange: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

let searchTimer = null

const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pagination.page = 1
    loadTransactions()
  }, 500)
}

const resetFilters = () => {
  filters.cardNumber = ''
  filters.type = ''
  filters.status = ''
  filters.dateRange = ''
  pagination.page = 1
  loadTransactions()
}

const loadTransactions = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      cardNumber: filters.cardNumber,
      type: filters.type,
      status: filters.status
    }
    const res = await transactionApi.list(params)
    transactions.value = res.data?.items || res.data || []
    pagination.total = res.data?.total || res.total || 0
  } catch (err) {
    ElMessage.error(err.response?.data?.message || t('common.loadFailed'))
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  pagination.page = page
  loadTransactions()
}

const handleExport = async () => {
  try {
    const params = {
      cardNumber: filters.cardNumber,
      type: filters.type,
      status: filters.status
    }
    const blob = await transactionApi.export(params)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `transactions_${Date.now()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success(t('downloads.exportSuccess'))
  } catch (err) {
    ElMessage.error(err.response?.data?.message || t('downloads.exportFailed'))
  }
}

const getTypeLabel = (type) => {
  const labels = {
    recharge: t('transactions.typeRecharge'),
    consumption: t('transactions.typeConsumption'),
    refund: t('transactions.typeRefund')
  }
  return labels[type] || type
}

const getStatusLabel = (status) => {
  const labels = {
    success: t('transactions.statusSuccess'),
    pending: t('transactions.statusPending'),
    failed: t('transactions.statusFailed')
  }
  return labels[status] || status
}

const formatAmount = (amount) => {
  const prefix = amount > 0 ? '+' : ''
  return prefix + new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(amount || 0)
}

onMounted(() => {
  loadTransactions()
})
</script>

<style scoped>
.page-shell { display: flex; flex-direction: column; gap: 20px; }
.page-header, .panel-header { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.page-header h1, .panel-header h2 { margin: 0; color: #111827; }
.page-header p { margin: 8px 0 0; color: #6b7280; }
.panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 16px; padding: 20px; }
.filter-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group input, .form-group select { height: 40px; border: 1px solid #d1d5db; border-radius: 8px; padding: 0 12px; }
.table-wrapper { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 14px 12px; border-bottom: 1px solid #f3f4f6; text-align: left; }
th { color: #6b7280; font-weight: 600; }
.amount-positive { color: #059669; font-weight: 600; }
.amount-negative { color: #dc2626; font-weight: 600; }
.status-pill { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.status-pill.success { background: #d1fae5; color: #065f46; }
.status-pill.pending { background: #fef3c7; color: #92400e; }
.status-pill.failed { background: #fee2e2; color: #991b1b; }
.empty-state, .loading-state { margin-top: 20px; border: 1px dashed #d1d5db; border-radius: 12px; padding: 24px; text-align: center; color: #6b7280; }
.primary-button, .link-button { border: none; border-radius: 8px; cursor: pointer; font-weight: 600; }
.primary-button { background: #2563eb; color: white; padding: 0 16px; height: 40px; }
.link-button { background: transparent; color: #2563eb; }
.meta { background: #f3f4f6; border-radius: 999px; padding: 4px 10px; color: #6b7280; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 20px; padding-top: 20px; border-top: 1px solid #f3f4f6; }
.pagination button { padding: 8px 16px; border: 1px solid #d1d5db; border-radius: 8px; background: white; cursor: pointer; }
.pagination button:disabled { opacity: 0.5; cursor: not-allowed; }
@media (max-width: 900px) {
  .filter-grid { grid-template-columns: 1fr; }
  .page-header, .panel-header { flex-direction: column; align-items: flex-start; }
}
</style>
