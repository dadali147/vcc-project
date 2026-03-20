<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button type="primary" @click="dialogVisible = true">新增 BIN</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="bin" label="BIN 号" />
      <el-table-column prop="brand" label="卡品牌" />
      <el-table-column prop="type" label="卡类型" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="availableCount" label="可用数量" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleToggleStatus(scope.row)">{{ scope.row.status === 'ACTIVE' ? '禁用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增 BIN">
      <span>TODO Form</span>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import client from '@/api/client'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)

const getList = async () => {
  loading.value = true
  try {
    const res = await client.get('/admin/card-bins')
    list.value = res.data?.items || []
  } finally {
    loading.value = false
  }
}

const handleToggleStatus = (row) => { console.log('toggle', row) }

onMounted(getList)
</script>
