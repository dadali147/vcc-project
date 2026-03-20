<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="操作日志" name="operation"></el-tab-pane>
      <el-tab-pane label="登录日志" name="login"></el-tab-pane>
    </el-tabs>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="userId" label="操作人" />
      <el-table-column prop="action" label="动作" />
      <el-table-column prop="ip" label="IP" />
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

const activeTab = ref('operation')
const loading = ref(false)
const list = ref([])
const total = ref(0)
const listQuery = ref({ page: 1, limit: 20 })

const getList = async () => {
  loading.value = true
  try {
    const url = activeTab.value === 'operation' ? '/admin/logs/operation' : '/admin/logs/login'
    const res = await client.get(url, { params: listQuery.value })
    list.value = res.data?.items || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => { listQuery.value.page = 1; getList() }

onMounted(getList)
</script>
