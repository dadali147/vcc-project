<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.keyword" placeholder="用户ID/邮箱" style="width: 200px;" class="filter-item" @keyup.enter="handleFilter" />
      <el-select v-model="listQuery.kycStatus" placeholder="KYC状态" clearable style="width: 150px; margin-left: 10px;">
        <el-option label="未认证" value="PENDING" />
        <el-option label="已认证" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
      <el-button class="filter-item" type="primary" @click="handleFilter" style="margin-left: 10px;">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 16px;">
      <el-table-column prop="id" label="用户 ID" width="180" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="createdAt" label="注册时间" />
      <el-table-column prop="kycStatus" label="KYC 状态">
        <template #default="scope">
          <el-tag v-if="scope.row.kycStatus === 'APPROVED'" type="success">已认证</el-tag>
          <el-tag v-else-if="scope.row.kycStatus === 'REJECTED'" type="danger">已拒绝</el-tag>
          <el-tag v-else type="info">未认证</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="balance" label="账户余额" />
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'ACTIVE'" type="success">正常</el-tag>
          <el-tag v-else type="danger">已禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">查看</el-button>
          <el-button size="small" :type="scope.row.status === 'ACTIVE' ? 'danger' : 'success'" @click="handleToggleStatus(scope.row)">
            {{ scope.row.status === 'ACTIVE' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container" style="margin-top: 20px" v-if="total > 0">
      <el-pagination background layout="prev, pager, next" :total="total" v-model:current-page="listQuery.page" :page-size="listQuery.limit" @current-change="getList" />
    </div>

    <el-dialog v-model="dialogVisible" title="用户详情" width="600px">
      <el-descriptions v-if="currentUser" :column="2" border>
        <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentUser.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="KYC状态">{{ currentUser.kycStatus }}</el-descriptions-item>
        <el-descriptions-item label="账户余额">{{ currentUser.balance }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentUser.status }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ currentUser.lastLoginAt || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import client from '@/api/client'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const listQuery = ref({
  page: 1,
  limit: 20,
  keyword: '',
  kycStatus: ''
})
const dialogVisible = ref(false)
const currentUser = ref(null)

const getList = async () => {
  loading.value = true
  try {
    const res = await client.get('/admin/merchants', { params: listQuery.value })
    list.value = res.data?.items || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  listQuery.value.page = 1
  getList()
}

const handleView = (row) => {
  currentUser.value = row
  dialogVisible.value = true
}

const handleToggleStatus = async (row) => {
  const action = row.status === 'ACTIVE' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}该用户？`, '提示', { type: 'warning' })
    await client.put(`/admin/merchants/${row.id}/status`, {
      status: row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
    })
    ElMessage.success(`${action}成功`)
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(`${action}失败`)
  }
}

onMounted(getList)
</script>
