<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="操作日志" name="operation"></el-tab-pane>
      <el-tab-pane label="登录日志" name="login"></el-tab-pane>
    </el-tabs>

    <div class="filter-container" style="margin-top: 16px;">
      <el-input v-model="listQuery.keyword" placeholder="用户ID/操作人" style="width: 200px;" @keyup.enter="handleFilter" />
      <el-select v-if="activeTab === 'operation'" v-model="listQuery.action" placeholder="操作类型" clearable style="width: 150px; margin-left: 10px;">
        <el-option label="创建" value="CREATE" />
        <el-option label="更新" value="UPDATE" />
        <el-option label="删除" value="DELETE" />
        <el-option label="审核" value="REVIEW" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px; margin-left: 10px;" />
      <el-button type="primary" @click="handleFilter" style="margin-left: 10px;">筛选</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="userId" label="操作人" />
      <el-table-column v-if="activeTab === 'operation'" prop="action" label="动作" />
      <el-table-column v-if="activeTab === 'operation'" prop="resource" label="资源" />
      <el-table-column prop="ip" label="IP" />
      <el-table-column prop="userAgent" label="User Agent" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="时间" width="180" />
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

const activeTab = ref('operation')
const loading = ref(false)
const list = ref([])
const total = ref(0)
const dateRange = ref([])
const listQuery = ref({ page: 1, limit: 20, keyword: '', action: '' })

const getList = async () => {
  loading.value = true
  try {
    const url = activeTab.value === 'operation' ? '/admin/logs/operation' : '/admin/logs/login'
    const params = { ...listQuery.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0].toISOString()
      params.endDate = dateRange.value[1].toISOString()
    }
    const res = await client.get(url, { params })
    list.value = res.data?.items || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('获取日志失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  listQuery.value.page = 1
  listQuery.value.keyword = ''
  listQuery.value.action = ''
  dateRange.value = []
  getList()
}

const handleFilter = () => {
  listQuery.value.page = 1
  getList()
}

onMounted(getList)
</script>
