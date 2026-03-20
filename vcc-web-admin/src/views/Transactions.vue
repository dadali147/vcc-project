<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.keyword" placeholder="交易ID/卡号" style="width: 200px;" @keyup.enter="handleFilter" />
      <el-select v-model="listQuery.type" placeholder="交易类型" clearable style="width: 150px; margin-left: 10px;">
        <el-option label="消费" value="SALE" />
        <el-option label="退款" value="REFUND" />
        <el-option label="充值" value="DEPOSIT" />
      </el-select>
      <el-select v-model="listQuery.status" placeholder="交易状态" clearable style="width: 150px; margin-left: 10px;">
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAILED" />
        <el-option label="处理中" value="PENDING" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px; margin-left: 10px;" />
      <el-button type="primary" @click="handleFilter" style="margin-left: 10px;">筛选</el-button>
      <el-button @click="handleExport" style="margin-left: 10px;">导出</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 20px;" @sort-change="handleSortChange">
      <el-table-column prop="id" label="交易 ID" width="180" />
      <el-table-column prop="cardNumber" label="卡号" width="180" />
      <el-table-column prop="amount" label="金额" sortable="custom" />
      <el-table-column prop="type" label="类型">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 'SALE'" type="danger">消费</el-tag>
          <el-tag v-else-if="scope.row.type === 'REFUND'" type="success">退款</el-tag>
          <el-tag v-else type="primary">充值</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'SUCCESS'" type="success">成功</el-tag>
          <el-tag v-else-if="scope.row.status === 'FAILED'" type="danger">失败</el-tag>
          <el-tag v-else type="warning">处理中</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="merchant" label="商户" />
      <el-table-column prop="createdAt" label="时间" sortable="custom" width="180" />
    </el-table>

    <div class="pagination-container" style="margin-top: 20px" v-if="total > 0">
      <el-pagination background layout="total, prev, pager, next" :total="total" v-model:current-page="listQuery.page" :page-size="listQuery.limit" @current-change="getList" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const dateRange = ref([])
const listQuery = ref({ page: 1, limit: 20, type: '', status: '', keyword: '', sortBy: '', sortOrder: '' })

const getList = async () => {
  loading.value = true
  try {
    const params = { ...listQuery.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0].toISOString()
      params.endDate = dateRange.value[1].toISOString()
    }
    const res = await client.get('/admin/transactions', { params })
    list.value = res.data?.items || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('获取交易记录失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  listQuery.value.page = 1
  getList()
}

const handleSortChange = ({ prop, order }) => {
  listQuery.value.sortBy = prop || ''
  listQuery.value.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  getList()
}

const handleExport = async () => {
  try {
    const params = { ...listQuery.value, export: true }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0].toISOString()
      params.endDate = dateRange.value[1].toISOString()
    }
    const res = await client.get('/admin/transactions/export', { params, responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `transactions_${Date.now()}.csv`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

onMounted(getList)
</script>
