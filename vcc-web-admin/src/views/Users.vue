<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.keyword" placeholder="用户ID/邮箱/公司名" style="width: 220px;" class="filter-item" @keyup.enter="handleFilter" />
      <el-select v-model="listQuery.kycStatus" placeholder="KYC状态" clearable style="width: 150px; margin-left: 10px;">
        <el-option label="未认证" value="NOT_SUBMITTED" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
      <el-select v-model="listQuery.status" placeholder="账号状态" clearable style="width: 130px; margin-left: 10px;">
        <el-option label="正常" value="1" />
        <el-option label="已禁用" value="0" />
      </el-select>
      <el-button class="filter-item" type="primary" @click="handleFilter" style="margin-left: 10px;">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 16px;">
      <el-table-column prop="id" label="用户 ID" width="100" />
      <el-table-column prop="companyName" label="公司名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
      <el-table-column prop="contactName" label="联系人" width="100" />
      <el-table-column prop="createdAt" label="注册时间" width="160" />
      <el-table-column prop="kycStatus" label="KYC 状态" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.kycStatus === 'APPROVED'" type="success" size="small">已通过</el-tag>
          <el-tag v-else-if="scope.row.kycStatus === 'REJECTED'" type="danger" size="small">已拒绝</el-tag>
          <el-tag v-else-if="scope.row.kycStatus === 'PENDING'" type="warning" size="small">待审核</el-tag>
          <el-tag v-else type="info" size="small">未提交</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="balance" label="账户余额" width="120">
        <template #default="scope">
          ${{ (scope.row.balance || 0).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '1' || scope.row.status === 1 || scope.row.status === 'ACTIVE'" type="success" size="small">正常</el-tag>
          <el-tag v-else type="danger" size="small">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">详情</el-button>
          <el-button
            size="small"
            :type="(scope.row.status === '1' || scope.row.status === 1 || scope.row.status === 'ACTIVE') ? 'danger' : 'success'"
            @click="handleToggleStatus(scope.row)"
          >
            {{ (scope.row.status === '1' || scope.row.status === 1 || scope.row.status === 'ACTIVE') ? '禁用' : '启用' }}
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

    <el-dialog v-model="dialogVisible" title="用户详情" width="660px">
      <el-descriptions v-if="currentUser" :column="2" border>
        <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
        <el-descriptions-item label="公司名称">{{ currentUser.companyName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentUser.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ currentUser.businessType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentUser.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ currentUser.lastLoginAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="KYC状态">{{ kycStatusLabel(currentUser.kycStatus) }}</el-descriptions-item>
        <el-descriptions-item label="账号状态">{{ (currentUser.status === '1' || currentUser.status === 1 || currentUser.status === 'ACTIVE') ? '正常' : '已禁用' }}</el-descriptions-item>
        <el-descriptions-item label="账户余额" :span="2">${{ (currentUser.balance || 0).toFixed(2) }}</el-descriptions-item>
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
  kycStatus: '',
  status: ''
})
const dialogVisible = ref(false)
const currentUser = ref(null)

const isActive = (row) => row.status === '1' || row.status === 1 || row.status === 'ACTIVE'

const kycStatusLabel = (status) => {
  const map = { APPROVED: '已通过', REJECTED: '已拒绝', PENDING: '待审核', NOT_SUBMITTED: '未提交' }
  return map[status] || status || '未提交'
}

const getList = async () => {
  loading.value = true
  try {
    const res = await client.get('/admin/merchant/list', { params: listQuery.value })
    list.value = res.data?.items || res.data?.rows || []
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
  const active = isActive(row)
  const action = active ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}该用户？`, '提示', { type: 'warning' })
    await client.put(`/admin/merchant/${row.id}/status`, {
      status: active ? '0' : '1'
    })
    ElMessage.success(`${action}成功`)
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(`${action}失败`)
  }
}

onMounted(getList)
</script>
