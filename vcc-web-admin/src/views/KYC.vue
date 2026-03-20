<template>
  <div class="app-container">
    <el-table :data="list" v-loading="loading" border style="width: 100%">
      <el-table-column prop="id" label="申请 ID" />
      <el-table-column prop="email" label="用户邮箱" />
      <el-table-column prop="type" label="认证类型" />
      <el-table-column prop="createdAt" label="提交时间" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">查看</el-button>
          <el-button size="small" type="success" @click="handleApprove(scope.row)">通过</el-button>
          <el-button size="small" type="danger" @click="handleReject(scope.row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container" style="margin-top: 20px" v-if="total > 0">
      <el-pagination background layout="prev, pager, next" :total="total" v-model:current-page="listQuery.page" :page-size="listQuery.limit" @current-change="getList" />
    </div>

    <el-dialog v-model="dialogVisible" title="KYC 详情">
      <div v-if="currentRow">
        <p>邮箱: {{ currentRow.email }}</p>
        <p>证件图片:</p>
        <img v-if="currentRow.idCardImage" :src="currentRow.idCardImage" style="max-width: 100%" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import client from '@/api/client'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const listQuery = ref({ page: 1, limit: 20 })
const dialogVisible = ref(false)
const currentRow = ref(null)

const getList = async () => {
  loading.value = true
  try {
    const res = await client.get('/admin/kyc/list', { params: listQuery.value })
    list.value = res.data?.items || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleView = (row) => {
  currentRow.value = row
  dialogVisible.value = true
}

const handleApprove = async (row) => {
  await client.post(`/admin/kyc/${row.id}/approve`)
  ElMessage.success('已通过')
  getList()
}

const handleReject = async (row) => {
  await client.post(`/admin/kyc/${row.id}/reject`)
  ElMessage.success('已拒绝')
  getList()
}

onMounted(getList)
</script>
