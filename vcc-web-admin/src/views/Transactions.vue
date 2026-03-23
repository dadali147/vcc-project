<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.keyword" placeholder="交易ID/卡号" style="width: 200px;" @keyup.enter="handleFilter" />
      <el-select v-model="listQuery.type" placeholder="交易类型" clearable style="width: 150px; margin-left: 10px;">
        <el-option label="消费" value="SALE" />
        <el-option label="退款" value="REFUND" />
        <el-option label="充值" value="RECHARGE" />
      </el-select>
      <el-select v-model="listQuery.status" placeholder="交易状态" clearable style="width: 150px; margin-left: 10px;">
        <el-option label="授权成功" value="AUTHORIZED" />
        <el-option label="已结算" value="SETTLED" />
        <el-option label="已拒绝" value="DECLINED" />
        <el-option label="已退款" value="REFUNDED" />
        <el-option label="已撤销" value="REVERSED" />
        <el-option label="争议中" value="DISPUTED" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px; margin-left: 10px;" />
      <el-button type="primary" @click="handleFilter" style="margin-left: 10px;">筛选</el-button>
      <el-button @click="handleExport" style="margin-left: 10px;">导出</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 20px;" @sort-change="handleSortChange">
      <el-table-column prop="id" label="交易 ID" width="120" />
      <el-table-column prop="cardNumber" label="卡号" width="170" />
      <el-table-column prop="merchantName" label="商户" min-width="120" show-overflow-tooltip />
      <el-table-column prop="amount" label="金额" width="120" sortable="custom">
        <template #default="scope">
          ${{ (scope.row.amount || 0).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="currency" label="币种" width="70" />
      <el-table-column prop="type" label="类型" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 'SALE'" type="danger" size="small">消费</el-tag>
          <el-tag v-else-if="scope.row.type === 'REFUND'" type="success" size="small">退款</el-tag>
          <el-tag v-else-if="scope.row.type === 'RECHARGE'" type="primary" size="small">充值</el-tag>
          <el-tag v-else type="info" size="small">{{ scope.row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'AUTHORIZED' || scope.row.status === 'SETTLED'" type="success" size="small">成功</el-tag>
          <el-tag v-else-if="scope.row.status === 'DECLINED'" type="danger" size="small">失败</el-tag>
          <el-tag v-else-if="scope.row.status === 'DISPUTED'" type="warning" size="small">争议</el-tag>
          <el-tag v-else type="info" size="small">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="130" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="时间" sortable="custom" width="160" />
    </el-table>

    <div class="pagination-container" style="margin-top: 20px" v-if="total > 0">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="listQuery.page"
        v-model:page-size="listQuery.limit"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="getList"
        @size-change="handleFilter"
      />
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
    const res = await client.get('/admin/transaction/list', { params })
    list.value = res.rows || res.data?.items || res.data?.rows || []
    total.value = res.total || res.data?.total || 0
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
    const params = { ...listQuery.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0].toISOString()
      params.endDate = dateRange.value[1].toISOString()
    }
    const blob = await client.get('/admin/transaction/export', {
      params,
      responseType: 'blob'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `transactions_${Date.now()}.csv`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

onMounted(getList)
</script>
