<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.keyword" placeholder="卡号/持卡人" style="width: 200px;" class="filter-item" @keyup.enter="handleFilter" />
      <el-button class="filter-item" type="primary" @click="handleFilter">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="cardNumber" label="卡号" />
      <el-table-column prop="cardholderName" label="持卡人" />
      <el-table-column prop="cardType" label="卡类型" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="balance" label="余额" />
      <el-table-column prop="createdAt" label="创建时间" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" type="warning" @click="handleFreeze(scope.row)">冻结</el-button>
          <el-button size="small" type="danger" @click="handleCancel(scope.row)">销卡</el-button>
        </template>
      </el-table-column>
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
const listQuery = ref({ page: 1, limit: 20, keyword: '' })

const getList = async () => {
  loading.value = true
  try {
    const res = await client.get('/admin/cards', { params: listQuery.value })
    list.value = res.data?.items || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleFilter = () => { listQuery.value.page = 1; getList() }
const handleFreeze = (row) => { console.log('freeze', row) }
const handleCancel = (row) => { console.log('cancel', row) }

onMounted(getList)
</script>
