<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.keyword" placeholder="规则名称" style="width: 180px;" @keyup.enter="handleFilter" />
      <el-select v-model="listQuery.type" placeholder="规则类型" clearable style="width: 150px; margin-left: 10px;">
        <el-option label="单笔限额" value="SINGLE_LIMIT" />
        <el-option label="日累计限额" value="DAILY_LIMIT" />
        <el-option label="月累计限额" value="MONTHLY_LIMIT" />
        <el-option label="频率限制" value="FREQUENCY_LIMIT" />
      </el-select>
      <el-select v-model="listQuery.status" placeholder="状态" clearable style="width: 110px; margin-left: 10px;">
        <el-option label="启用" value="ACTIVE" />
        <el-option label="禁用" value="INACTIVE" />
      </el-select>
      <el-button type="primary" @click="handleFilter" style="margin-left: 10px;">筛选</el-button>
      <el-button type="success" @click="handleCreate" style="margin-left: 10px;">新增规则</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 16px;">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="规则名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="scope">
          <el-tag type="primary" size="small">{{ getRuleTypeLabel(scope.row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="threshold" label="阈值" width="120" />
      <el-table-column prop="cardType" label="卡类型" width="90">
        <template #default="scope">
          {{ scope.row.cardType === 'ALL' ? '全部' : scope.row.cardType === 'VIRTUAL' ? '虚拟卡' : '实体卡' }}
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'ACTIVE'" type="success" size="small">启用</el-tag>
          <el-tag v-else type="info" size="small">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" :type="scope.row.status === 'ACTIVE' ? 'warning' : 'success'" @click="handleToggleStatus(scope.row)">
            {{ scope.row.status === 'ACTIVE' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container" style="margin-top: 20px" v-if="total > 0">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="listQuery.page"
        v-model:page-size="listQuery.limit"
        :page-sizes="[10, 20, 50]"
        @current-change="getList"
        @size-change="handleFilter"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="temp" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="temp.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="type">
          <el-select v-model="temp.type" placeholder="请选择规则类型" style="width: 100%;">
            <el-option label="单笔限额" value="SINGLE_LIMIT" />
            <el-option label="日累计限额" value="DAILY_LIMIT" />
            <el-option label="月累计限额" value="MONTHLY_LIMIT" />
            <el-option label="频率限制" value="FREQUENCY_LIMIT" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值" prop="threshold">
          <el-input v-model="temp.threshold" placeholder="请输入阈值" />
        </el-form-item>
        <el-form-item label="卡类型" prop="cardType">
          <el-select v-model="temp.cardType" placeholder="请选择卡类型" style="width: 100%;">
            <el-option label="全部" value="ALL" />
            <el-option label="虚拟卡" value="VIRTUAL" />
            <el-option label="实体卡" value="PHYSICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="temp.priority" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="temp.status">
            <el-radio label="ACTIVE">启用</el-radio>
            <el-radio label="INACTIVE">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import client from '@/api/client'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const listQuery = ref({ page: 1, limit: 20, keyword: '', type: '', status: '' })
const dialogVisible = ref(false)
const temp = ref({})
const formRef = ref(null)
const isEdit = ref(false)

const dialogTitle = computed(() => isEdit.value ? '编辑规则' : '新增规则')

const rules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  threshold: [
    { required: true, message: '请输入阈值', trigger: 'blur' },
    { pattern: /^(0|[1-9]\d*)(\.\d+)?$/, message: '请输入有效的数字', trigger: 'blur' }
  ],
  cardType: [{ required: true, message: '请选择卡类型', trigger: 'change' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getRuleTypeLabel = (type) => {
  const map = {
    SINGLE_LIMIT: '单笔限额',
    DAILY_LIMIT: '日累计限额',
    MONTHLY_LIMIT: '月累计限额',
    FREQUENCY_LIMIT: '频率限制'
  }
  return map[type] || type
}

const getList = async () => {
  loading.value = true
  try {
    const res = await client.get('/admin/business/risk/list')
    list.value = res.rows || res.data?.items || res.data?.rows || (Array.isArray(res.data) ? res.data : [])
    total.value = res.total || res.data?.total || list.value.length
  } catch (e) {
    ElMessage.error('获取风控规则失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  listQuery.value.page = 1
  getList()
}

const handleCreate = () => {
  isEdit.value = false
  temp.value = { name: '', type: '', threshold: '', cardType: 'ALL', priority: 1, status: 'ACTIVE' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  temp.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await client.put(`/admin/business/risk/${temp.value.id}/handle`, temp.value)
    } else {
      ElMessage.warning('当前不支持新增风控规则，请联系后端开发')
      return
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('保存失败')
  }
}

const handleToggleStatus = async (row) => {
  const action = row.status === 'ACTIVE' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}该规则？`, '提示', { type: 'warning' })
    await client.put(`/admin/business/risk/${row.id}/handle`, {
      status: row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    })
    ElMessage.success(`${action}成功`)
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(`${action}失败`)
  }
}

onMounted(getList)
</script>
