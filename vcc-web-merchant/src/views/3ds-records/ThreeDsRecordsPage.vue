<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1>{{ t('threeDsRecords.title') }}</h1>
        <p>{{ t('threeDsRecords.pageDescription') }}</p>
      </div>
    </div>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('common.filter') }}</h2>
        <button class="link-button" @click="resetFilters">{{ t('common.reset') }}</button>
      </div>
      <div class="filter-grid">
        <div class="form-group">
          <label>{{ t('threeDsRecords.cardNumber') }}</label>
          <input v-model="filters.cardNoMask" type="text" :placeholder="t('threeDsRecords.cardPlaceholder')" @input="handleSearch" />
        </div>
        <div class="form-group">
          <label>{{ t('threeDsRecords.status') }}</label>
          <select v-model="filters.status" @change="loadRecords">
            <option value="">{{ t('common.all') }}</option>
            <option value="0">{{ t('threeDsRecords.statusPending') }}</option>
            <option value="1">{{ t('threeDsRecords.statusVerified') }}</option>
            <option value="2">{{ t('threeDsRecords.statusExpired') }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>{{ t('threeDsRecords.dateRange') }}</label>
          <input v-model="filters.dateRange" type="text" :placeholder="t('common.dateRangePlaceholder')" />
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('threeDsRecords.list') }}</h2>
        <span class="meta">{{ pagination.total }}</span>
      </div>
      <div v-if="loading" class="loading-state">{{ t('common.loading') }}</div>
      <div v-else-if="records.length" class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>{{ t('threeDsRecords.time') }}</th>
              <th>{{ t('threeDsRecords.cardNumber') }}</th>
              <th>{{ t('threeDsRecords.otpCode') }}</th>
              <th>{{ t('threeDsRecords.merchant') }}</th>
              <th>{{ t('threeDsRecords.amount') }}</th>
              <th>{{ t('threeDsRecords.status') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in records" :key="item.id">
              <td>{{ item.createdAt }}</td>
              <td>{{ item.cardNoMask }}</td>
              <td><strong>{{ item.otpCode }}</strong></td>
              <td>{{ item.merchantName }}</td>
              <td>{{ formatAmount(item.transactionAmount, item.currency) }}</td>
              <td><span class="status-pill" :class="getStatusClass(item.status)">{{ getStatusLabel(item.status) }}</span></td>
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
        <strong>{{ t('threeDsRecords.emptyTitle') }}</strong>
        <p>{{ t('threeDsRecords.noRecords') }}</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { threeDsApi } from '@/api'

const { t } = useI18n()

const loading = ref(false)
const records = ref([])
const filters = reactive({
  cardNoMask: '',
  status: '',
  dateRange: ''
})
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

let searchTimer = null

const loadRecords = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      cardNoMask: filters.cardNoMask || undefined,
      status: filters.status || undefined
    }
    const res = await threeDsApi.list(params)
    records.value = res.rows || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error(t('common.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pagination.page = 1
    loadRecords()
  }, 500)
}

const resetFilters = () => {
  filters.cardNoMask = ''
  filters.status = ''
  filters.dateRange = ''
  pagination.page = 1
  loadRecords()
}

const changePage = (page) => {
  pagination.page = page
  loadRecords()
}

const getStatusLabel = (status) => {
  const map = {
    0: t('threeDsRecords.statusPending'),
    1: t('threeDsRecords.statusVerified'),
    2: t('threeDsRecords.statusExpired')
  }
  return map[status] || '-'
}

const getStatusClass = (status) => {
  const map = {
    0: 'status-pending',
    1: 'status-success',
    2: 'status-expired'
  }
  return map[status] || ''
}

const formatAmount = (amount, currency) => {
  if (!amount) return '-'
  return `${currency || 'USD'} ${Number(amount).toFixed(2)}`
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.status-pending {
  background-color: #fef3c7;
  color: #92400e;
}

.status-success {
  background-color: #d1fae5;
  color: #065f46;
}

.status-expired {
  background-color: #fee2e2;
  color: #991b1b;
}
</style>
