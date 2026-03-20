<template>
  <div class="app-container">
    <div class="filter-container">
      <el-select v-model="listQuery.type" placeholder="交易类型" clearable style="width: 150px; margin-right: 10px;">
        <el-option label="消费" value="SALE" />
        <el-option label="退款" value="REFUND" />
      </el-select>
      <el-button type="primary" @click="handleFilter">筛选</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="交易 ID" />
      <el-table-column prop="cardNumber" label="卡号" />
      <el-table-column prop="amount" label="金额" />
      <el-table-column prop="type" label="类型" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="createdAt" label="时间" />
    </el-table>

    <div class="pagination-container" style="margin-top: 20px" v-if="total > 0">
      <el-pagination background layout="prev, pager, next" :total="total" v-model:current-page="listQuery.page" :page-size="listQuery.limit" @current-change="getList" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import client from '@/api/client'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const listQuery = ref({ page: 1, limit: 20, type: '' })

const getList = async () => {
  loading.value = true
  try {
    const res = await client.get('/admin/transactions', { params: listQuery.value })
    list.value = res.data?.items || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleFilter = () => { listQuery.value.page = 1; getList() }

onMounted(getList)
</script>
