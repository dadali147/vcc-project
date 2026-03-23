<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1>{{ t('cardholders.title') }}</h1>
        <p>{{ t('cardholders.pageDescription') }}</p>
      </div>
      <div class="header-actions">
        <button class="secondary-button" @click="handleExport">{{ t('common.export') }}</button>
        <button class="primary-button" @click="showAddDialog = true">{{ t('cardholders.add') }}</button>
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
          <input v-model="filters.keyword" type="text" :placeholder="t('cardholders.searchPlaceholder')" @input="handleSearch" />
        </div>
        <div class="form-group">
          <label>{{ t('cardholders.status') }}</label>
          <select v-model="filters.status" @change="loadCardholders">
            <option value="">{{ t('common.all') }}</option>
            <option value="active">{{ t('cardholders.statusActive') }}</option>
            <option value="pending">{{ t('cardholders.statusPending') }}</option>
            <option value="disabled">{{ t('cardholders.statusDisabled') }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>{{ t('cardholders.createdAt') }}</label>
          <input v-model="filters.dateRange" type="text" :placeholder="t('common.dateRangePlaceholder')" />
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('cardholders.list') }}</h2>
        <span class="meta">{{ pagination.total }}</span>
      </div>
      <div v-if="loading" class="loading-state">{{ t('common.loading') }}</div>
      <div v-else-if="cardholders.length" class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>{{ t('cardholders.name') }}</th>
              <th>{{ t('cardholders.email') }}</th>
              <th>Mobile</th>
              <th>Country</th>
              <th>{{ t('cardholders.status') }}</th>
              <th>{{ t('cardholders.createdAt') }}</th>
              <th>{{ t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in cardholders" :key="item.id">
              <td>{{ item.firstName }} {{ item.lastName }}</td>
              <td>{{ item.email }}</td>
              <td>{{ item.mobile }}</td>
              <td>{{ item.country }}</td>
              <td><span class="status-pill" :class="item.status">{{ getStatusLabel(item.status) }}</span></td>
              <td>{{ item.createdAt }}</td>
              <td>
                <div class="action-row">
                  <button class="table-button" @click="handleEdit(item)">{{ t('common.edit') }}</button>
                  <button class="table-button danger" @click="handleDelete(item.id)">{{ t('common.delete') }}</button>
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
        <strong>{{ t('cardholders.emptyTitle') }}</strong>
        <p>{{ t('cardholders.noCardholders') }}</p>
        <button class="primary-button primary-button--small" @click="showAddDialog = true">{{ t('cardholders.add') }}</button>
      </div>
    </section>

    <el-dialog v-model="showAddDialog" :title="editingId ? t('cardholders.edit') : t('cardholders.add')" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item :label="'First Name'" prop="firstName">
          <el-input v-model="form.firstName" placeholder="First Name" />
        </el-form-item>
        <el-form-item :label="'Last Name'" prop="lastName">
          <el-input v-model="form.lastName" placeholder="Last Name" />
        </el-form-item>
        <el-form-item :label="t('cardholders.email')" prop="email">
          <el-input v-model="form.email" :placeholder="t('auth.email')" />
        </el-form-item>
        <el-form-item :label="'Mobile'" prop="mobile">
          <el-input v-model="form.mobile" placeholder="Mobile number" />
        </el-form-item>
        <el-form-item :label="'Country'" prop="country">
          <el-input v-model="form.country" placeholder="Country" />
        </el-form-item>
        <el-form-item :label="'City'" prop="city">
          <el-input v-model="form.city" placeholder="City" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cardholderApi } from '@/api'

const { t } = useI18n()

const loading = ref(false)
const submitting = ref(false)
const showAddDialog = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const cardholders = ref([])
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

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  mobile: '',
  country: '',
  city: ''
})

const rules = {
  firstName: [
    { required: true, message: '请输入名', trigger: 'blur' }
  ],
  lastName: [
    { required: true, message: '请输入姓', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' }
  ]
}

let searchTimer = null

const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pagination.page = 1
    loadCardholders()
  }, 500)
}

const resetFilters = () => {
  filters.keyword = ''
  filters.status = ''
  filters.dateRange = ''
  pagination.page = 1
  loadCardholders()
}

const loadCardholders = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      keyword: filters.keyword,
      status: filters.status
    }
    const res = await cardholderApi.list(params)
    cardholders.value = res.rows || res.data?.items || res.data || []
    pagination.total = res.total || res.data?.total || 0
  } catch (err) {
    ElMessage.error(err.response?.data?.message || t('common.loadFailed'))
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  pagination.page = page
  loadCardholders()
}

const handleEdit = (item) => {
  editingId.value = item.id
  Object.assign(form, {
    firstName: item.firstName,
    lastName: item.lastName,
    email: item.email,
    mobile: item.mobile,
    country: item.country,
    city: item.city
  })
  showAddDialog.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (editingId.value) {
        await cardholderApi.update(editingId.value, form)
        ElMessage.success(t('cardholders.updateSuccess'))
      } else {
        await cardholderApi.create(form)
        ElMessage.success(t('cardholders.addSuccess'))
      }
      showAddDialog.value = false
      resetForm()
      loadCardholders()
    } catch (err) {
      ElMessage.error(err.response?.data?.message || t('common.operationFailed'))
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('cardholders.confirmDelete'), t('common.tip'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })

    await cardholderApi.delete(id)
    ElMessage.success(t('cardholders.deleteSuccess'))
    loadCardholders()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.message || t('common.operationFailed'))
    }
  }
}

const handleExport = async () => {
  try {
    const params = {
      keyword: filters.keyword,
      status: filters.status
    }

    const blob = await cardholderApi.list({ ...params, export: true })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `cardholders_${new Date().getTime()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success(t('downloads.exportSuccess'))
  } catch (err) {
    ElMessage.error(t('downloads.exportFailed'))
  }
}

const resetForm = () => {
  editingId.value = null
  Object.assign(form, {
    firstName: '',
    lastName: '',
    email: '',
    mobile: '',
    country: '',
    city: ''
  })
  formRef.value?.resetFields()
}

const getStatusLabel = (status) => {
  const labels = {
    active: t('cardholders.statusActive'),
    pending: t('cardholders.statusPending'),
    disabled: t('cardholders.statusDisabled')
  }
  return labels[status] || status
}

onMounted(() => {
  loadCardholders()
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
.status-pill.pending { background: #fef3c7; color: #92400e; }
.status-pill.disabled { background: #fee2e2; color: #991b1b; }
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
