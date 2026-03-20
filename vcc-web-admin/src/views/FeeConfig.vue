<template>
  <div class="app-container">
    <el-table :data="list" v-loading="loading" border style="width: 100%;">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="value" label="费率" />
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="编辑费率">
      <el-form :model="temp" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="temp.name" disabled></el-input>
        </el-form-item>
        <el-form-item label="费率值">
          <el-input v-model="temp.value"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import client from '@/api/client'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const temp = ref({})

const getList = async () => {
  loading.value = true
  try {
    const res = await client.get('/admin/fee-config')
    list.value = res.data?.items || []
  } finally {
    loading.value = false
  }
}

const handleEdit = (row) => {
  temp.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  await client.put(`/admin/fee-config/${temp.value.id}`, temp.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  getList()
}

onMounted(getList)
</script>
