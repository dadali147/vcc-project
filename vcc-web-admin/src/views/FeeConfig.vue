<template>
  <div class="app-container">
    <el-table :data="list" v-loading="loading" border style="width: 100%;">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="value" label="费率" />
      <el-table-column prop="description" label="说明" />
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="编辑费率" width="500px">
      <el-form :model="temp" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="temp.name" disabled></el-input>
        </el-form-item>
        <el-form-item label="费率值" prop="value">
          <el-input v-model="temp.value" placeholder="例如: 0.03 表示 3%">
            <template #append>%</template>
          </el-input>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="temp.description" type="textarea" :rows="3" disabled></el-input>
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
import { ElMessage } from 'element-plus'
import client from '@/api/client'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const temp = ref({})
const formRef = ref(null)
const rules = {
  value: [
    { required: true, message: '请输入费率值', trigger: 'blur' },
    { pattern: /^(0|[1-9]\d*)(\.\d+)?$/, message: '请输入有效的数字', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      const num = parseFloat(value)
      if (num < 0 || num > 100) {
        callback(new Error('费率值应在 0-100 之间'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await client.get('/admin/fee-config')
    list.value = res.data?.items || []
  } catch (e) {
    ElMessage.error('获取费率配置失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = (row) => {
  temp.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    await client.put(`/admin/fee-config/${temp.value.id}`, temp.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('保存失败')
  }
}

onMounted(getList)
</script>
