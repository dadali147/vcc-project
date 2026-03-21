<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1>{{ t('cardApplications.title') }}</h1>
        <p>{{ t('cardApplications.pageDescription') }}</p>
      </div>
      <button class="primary-button" @click="$router.push('/card-apply')">{{ t('cardApplication.apply') }}</button>
    </div>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('common.filter') }}</h2>
        <button class="link-button" @click="resetFilters">{{ t('common.reset') }}</button>
      </div>
      <div class="filter-grid">
        <div class="form-group">
          <label>{{ t('common.search') }}</label>
          <input v-model="filters.keyword" type="text" :placeholder="t('cardApplications.searchPlaceholder')" @input="handleSearch" />
        </div>
        <div class="form-group">
          <label>{{ t('cardApplications.status') }}</label>
          <select v-model="filters.status" @change="loadApplications">
            <option value="">{{ t('common.all') }}</option>
            <option value="pending">{{ t('cardApplications.statusPending') }}</option>
            <option value="approved">{{ t('cardApplications.statusApproved') }}</option>
            <option value="rejected">{{ t('cardApplications.statusRejected') }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>{{ t('cardApplications.appliedAt') }}</label>
          <input v-model="filters.dateRange" type="text" :placeholder="t('common.dateRangePlaceholder')" />
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('cardApplications.list') }}</h2>
        <span class="meta">{{ pagination.total }}</span>
      </div>
      <div v-if="loading" class="loading-state">{{ t('common.loading') }}</div>
      <div v-else-if="applications.length" class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>{{ t('cardApplications.applicationId') }}</th>
              <th>{{ t('cardApplications.cardholder') }}</th>
              <th>{{ t('cardApplications.cardType') }}</th>
              <th>{{ t('cardApplications.status') }}</th>
              <th>{{ t('cardApplications.reason') }}</th>
              <th>{{ t('cardApplications.appliedAt') }}</th>
              <th>{{ t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in applications" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.cardholderName || item.cardholder }}</td>
              <td>{{ getCardTypeLabel(item.cardType) }}</td>
              <td>
                <span class="status-pill" :class="item.status">
                  {{ getStatusLabel(item.status) }}
                </span>
              </td>
              <td>{{ item.reason || item.rejectReason || '-' }}</td>
              <td>{{ item.appliedAt || item.createdAt }}</td>
              <td>
                <div class="action-row">
                  <button class="table-button" @click="viewDetail(item)">{{ t('cards.cardDetails') }}</button>
                </div>
              </td>
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
        <strong>{{ t('cardApplications.emptyTitle') }}</strong>
        <p>{{ t('cardApplications.noApplications') }}</p>
        <button class="primary-button primary-button--small" @click="$router.push('/card-apply')">{{ t('cardApplication.apply') }}</button>
      </div>
    </section>

    <!-- Detail Dialog -->
    <el-dialog v-model="showDetail" :title="t('cards.cardDetails')" width="520px">
      <div v-if="selectedItem" class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">{{ t('cardApplications.applicationId') }}</span>
          <span class="detail-value">{{ selectedItem.id }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">{{ t('cardApplications.cardholder') }}</span>
          <span class="detail-value">{{ selectedItem.cardholderName || selectedItem.cardholder }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">{{ t('cardApplications.cardType') }}</span>
          <span class="detail-value">{{ getCardTypeLabel(selectedItem.cardType) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">{{ t('cardApplications.status') }}</span>
          <span class="status-pill" :class="selectedItem.status">{{ getStatusLabel(selectedItem.status) }}</span>
        </div>
        <div v-if="selectedItem.reason || selectedItem.rejectReason" class="detail-item full-width">
          <span class="detail-label">{{ t('cardApplications.reason') }}</span>
          <span class="detail-value">{{ selectedItem.reason || selectedItem.rejectReason }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">{{ t('cardApplications.appliedAt') }}</span>
          <span class="detail-value">{{ selectedItem.appliedAt || selectedItem.createdAt }}</span>
        </div>
        <div v-if="selectedItem.completedAt" class="detail-item">
          <span class="detail-label">{{ t('cardApplications.completedAt') }}</span>
          <span class="detail-value">{{ selectedItem.completedAt }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { cardApi } from '@/api'

const { t } = useI18n()

const loading = ref(false)
const applications = ref([])
const showDetail = ref(false)
const selectedItem = ref(null)

const filters = reactive({
  keyword: '',
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
    loadApplications()
  }, 500)
}

const resetFilters = () => {
  filters.keyword = ''
  filters.status = ''
  filters.dateRange = ''
  pagination.page = 1
  loadApplications()
}

const loadApplications = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: filters.keyword,
      status: filters.status
    }
    const res = await cardApi.getApplications(params)
    applications.value = res.data?.items || res.data || []
    pagination.total = res.data?.total || res.total || 0
  } catch (err) {
    ElMessage.error(err.response?.data?.message || t('common.loadFailed'))
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  pagination.page = page
  loadApplications()
}

const viewDetail = (item) => {
  selectedItem.value = item
  showDetail.value = true
}

const getCardTypeLabel = (type) => {
  const labels = { code: t('cards.cardTypeCode'), budget: t('cards.cardTypeBudget') }
  return labels[type] || type
}

const getStatusLabel = (status) => {
  const labels = {
    pending: t('cardApplications.statusPending'),
    approved: t('cardApplications.statusApproved'),
    rejected: t('cardApplications.statusRejected')
  }
  return labels[status] || status
}

onMounted(() => {
  loadApplications()
})
</script>

<style scoped>
.page-shell { display: flex; flex-direction: column; gap: 20px; }
.page-header, .panel-header { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.page-header h1, .panel-header h2 { margin: 0; color: #111827; }
.page-header p { margin: 8px 0 0; color: #6b7280; }
.panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 16px; padding: 20px; }
.filter-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group input, .form-group select { height: 40px; border: 1px solid #d1d5db; border-radius: 8px; padding: 0 12px; }
.table-wrapper { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 14px 12px; border-bottom: 1px solid #f3f4f6; text-align: left; }
th { color: #6b7280; font-weight: 600; }
.status-pill { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.status-pill.pending { background: #fef3c7; color: #92400e; }
.status-pill.approved { background: #d1fae5; color: #065f46; }
.status-pill.rejected { background: #fee2e2; color: #991b1b; }
.empty-state, .loading-state { margin-top: 20px; border: 1px dashed #d1d5db; border-radius: 12px; padding: 24px; text-align: center; color: #6b7280; }
.action-row { display: flex; gap: 8px; }
.primary-button, .table-button, .link-button { border: none; border-radius: 8px; cursor: pointer; font-weight: 600; }
.primary-button { background: #2563eb; color: white; padding: 0 16px; height: 40px; }
.primary-button--small { height: 36px; }
.table-button { background: #f3f4f6; color: #374151; padding: 6px 10px; }
.link-button { background: transparent; color: #2563eb; }
.meta { background: #f3f4f6; border-radius: 999px; padding: 4px 10px; color: #6b7280; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 20px; padding-top: 20px; border-top: 1px solid #f3f4f6; }
.pagination button { padding: 8px 16px; border: 1px solid #d1d5db; border-radius: 8px; background: white; cursor: pointer; }
.pagination button:disabled { opacity: 0.5; cursor: not-allowed; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.detail-item { display: flex; flex-direction: column; gap: 4px; }
.full-width { grid-column: 1 / -1; }
.detail-label { font-size: 12px; color: #6b7280; font-weight: 600; text-transform: uppercase; }
.detail-value { font-size: 14px; color: #111827; }
@media (max-width: 900px) {
  .filter-grid, .detail-grid { grid-template-columns: 1fr; }
  .page-header, .panel-header { flex-direction: column; align-items: flex-start; }
}
</style>
