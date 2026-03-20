<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.keyword" placeholder="用户ID/邮箱" style="width: 200px;" class="filter-item" @keyup.enter="handleFilter" />
      <el-button class="filter-item" type="primary" @click="handleFilter">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 16px;">
      <el-table-column prop="id" label="用户 ID" width="180" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="createdAt" label="注册时间" />
      <el-table-column prop="kycStatus" label="KYC 状态" />
      <el-table-column prop="balance" label="账户余额" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">查看</el-button>
          <el-button size="small" type="danger" @click="handleDisable(scope.row)">禁用</el-button>
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
const listQuery = ref({
  page: 1,
  limit: 20,
  keyword: ''
})

const getList = async () => {
  loading.value = true
  try {
    const res = await client.get('/admin/merchants', { params: listQuery.value })
    list.value = res.data?.items || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  listQuery.value.page = 1
  getList()
}

const handleView = (row) => { console.log('view', row) }
const handleDisable = (row) => { console.log('disable', row) }

onMounted(getList)
</script>
