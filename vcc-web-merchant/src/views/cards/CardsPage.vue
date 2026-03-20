<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1>{{ t('cards.title') }}</h1>
        <p>{{ t('cards.pageDescription') }}</p>
      </div>
      <div class="header-actions">
        <button class="secondary-button" @click="handleExport">{{ t('common.export') }}</button>
        <button class="primary-button" @click="$router.push('/card-apply')">{{ t('cards.applyCard') }}</button>
      </div>
    </div>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('common.filter') }}</h2>
        <button class="link-button" @click="resetFilters">{{ t('common.reset') }}</button>
      </div>
      <div class="filter-grid">
        <div class="form-group">
          <label>{{ t('common.search') }}</label>
          <input v-model="filters.keyword" type="text" :placeholder="t('cards.searchPlaceholder')" @input="handleSearch" />
        </div>
        <div class="form-group">
          <label>{{ t('cards.cardType') }}</label>
          <select v-model="filters.cardType" @change="loadCards">
            <option value="">{{ t('common.all') }}</option>
            <option value="code">{{ t('cards.cardTypeCode') }}</option>
            <option value="budget">{{ t('cards.cardTypeBudget') }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>{{ t('cards.status') }}</label>
          <select v-model="filters.status" @change="loadCards">
            <option value="">{{ t('common.all') }}</option>
            <option value="active">{{ t('cards.statusActive') }}</option>
            <option value="frozen">{{ t('cards.statusFrozen') }}</option>
            <option value="cancelled">{{ t('cards.statusCancelled') }}</option>
          </select>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('cards.list') }}</h2>
        <span class="meta">{{ pagination.total }}</span>
      </div>
      <div v-if="loading" class="loading-state">加载中...</div>
      <div v-else-if="cards.length" class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>{{ t('cards.cardNumber') }}</th>
              <th>{{ t('cards.cardholder') }}</th>
              <th>{{ t('cards.cardType') }}</th>
              <th>{{ t('cards.balance') }}</th>
              <th>{{ t('cards.dailyLimit') }}</th>
              <th>{{ t('cards.status') }}</th>
              <th>{{ t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in cards" :key="item.id">
              <td>{{ item.cardNumber }}</td>
              <td>{{ item.cardholder }}</td>
              <td>{{ getCardTypeLabel(item.cardType) }}</td>
              <td>{{ formatCurrency(item.balance) }}</td>
              <td>{{ formatCurrency(item.dailyLimit) }}</td>
              <td><span class="status-pill" :class="item.status">{{ getStatusLabel(item.status) }}</span></td>
              <td>
                <div class="action-row">
                  <button class="table-button" @click="$router.push(`/cards/${item.id}`)">{{ t('cards.cardDetails') }}</button>
                  <button v-if="item.status === 'active'" class="table-button" @click="handleFreeze(item.id)">{{ t('cards.freeze') }}</button>
                  <button v-if="item.status === 'frozen'" class="table-button" @click="handleUnfreeze(item.id)">{{ t('cards.unfreeze') }}</button>
                  <button class="table-button danger" @click="handleCancel(item.id)">{{ t('cards.cancel') }}</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="pagination">
          <button :disabled="pagination.page === 1" @click="changePage(pagination.page - 1)">上一页</button>
          <span>第 {{ pagination.page }} / {{ Math.ceil(pagination.total / pagination.pageSize) }} 页</span>
          <button :disabled="pagination.page >= Math.ceil(pagination.total / pagination.pageSize)" @click="changePage(pagination.page + 1)">下一页</button>
        </div>
      </div>
      <div v-else class="empty-state">
        <strong>{{ t('cards.emptyTitle') }}</strong>
        <p>{{ t('cards.noCards') }}</p>
        <button class="primary-button primary-button--small" @click="$router.push('/card-apply')">{{ t('cards.applyCard') }}</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cardApi } from '@/api'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const cards = ref([])

const filters = reactive({
  keyword: '',
  cardType: '',
  status: ''
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
    loadCards()
  }, 500)
}

const resetFilters = () => {
  filters.keyword = ''
  filters.cardType = ''
  filters.status = ''
  pagination.page = 1
  loadCards()
}

const loadCards = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: filters.keyword,
      cardType: filters.cardType,
      status: filters.status
    }
    const res = await cardApi.list(params)
    cards.value = res.data || []
    pagination.total = res.total || 0
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  pagination.page = page
  loadCards()
}

const handleFreeze = async (id) => {
  try {
    await ElMessageBox.confirm('确定要冻结该卡片吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cardApi.freeze(id)
    ElMessage.success('冻结成功')
    loadCards()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.message || '冻结失败')
    }
  }
}

const handleUnfreeze = async (id) => {
  try {
    await cardApi.unfreeze(id)
    ElMessage.success('解冻成功')
    loadCards()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '解冻失败')
  }
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要注销该卡片吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cardApi.cancel(id)
    ElMessage.success('注销成功')
    loadCards()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.message || '注销失败')
    }
  }
}

const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

const getCardTypeLabel = (type) => {
  const labels = {
    code: t('cards.cardTypeCode'),
    budget: t('cards.cardTypeBudget')
  }
  return labels[type] || type
}

const getStatusLabel = (status) => {
  const labels = {
    active: t('cards.statusActive'),
    frozen: t('cards.statusFrozen'),
    cancelled: t('cards.statusCancelled')
  }
  return labels[status] || status
}

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(amount || 0)
}

onMounted(() => {
  loadCards()
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
.status-pill.active { background: #d1fae5; color: #065f46; }
.status-pill.frozen { background: #dbeafe; color: #1e40af; }
.status-pill.cancelled { background: #fee2e2; color: #991b1b; }
.empty-state, .loading-state { margin-top: 20px; border: 1px dashed #d1d5db; border-radius: 12px; padding: 24px; text-align: center; color: #6b7280; }
.header-actions, .action-row { display: flex; gap: 12px; }
.primary-button, .secondary-button, .table-button, .link-button { border: none; border-radius: 8px; cursor: pointer; font-weight: 600; }
.primary-button { background: #2563eb; color: white; padding: 0 16px; height: 40px; }
.primary-button--small { height: 36px; }
.secondary-button { background: #eef2ff; color: #4338ca; padding: 0 16px; height: 40px; }
.table-button { background: #f3f4f6; color: #374151; padding: 6px 10px; }
.table-button.danger { color: #b91c1c; }
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
