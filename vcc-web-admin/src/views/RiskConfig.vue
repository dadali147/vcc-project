<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button type="primary" @click="dialogVisible = true">新增规则</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="name" label="规则名称" />
      <el-table-column prop="type" label="类型" />
      <el-table-column prop="threshold" label="阈值" />
      <el-table-column prop="cardType" label="卡类型" />
      <el-table-column prop="priority" label="优先级" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="规则编辑">
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
    const res = await client.get('/admin/risk-rules')
    list.value = res.data?.items || []
  } finally {
    loading.value = false
  }
}

const handleEdit = (row) => { console.log('edit', row) }

onMounted(getList)
</script>
