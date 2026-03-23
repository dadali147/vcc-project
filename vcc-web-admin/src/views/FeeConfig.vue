<template>
  <div class="app-container">
    <el-alert type="info" show-icon :closable="false" style="margin-bottom: 16px;">
      <template #title>费率配置将在下一次业务操作时生效，请谨慎修改。费率值范围 0-100（%）。</template>
    </el-alert>

    <el-table :data="list" v-loading="loading" border style="width: 100%;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="费率名称" min-width="160" />
      <el-table-column prop="configKey" label="配置 Key" min-width="160" />
      <el-table-column prop="value" label="费率值" width="120">
        <template #default="scope">
          <span>{{ scope.row.value }}%</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
      <el-table-column prop="updatedAt" label="最后更新" width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="编辑费率配置" width="520px">
      <el-form :model="temp" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="费率名称">
          <el-input v-model="temp.name" disabled></el-input>
        </el-form-item>
        <el-form-item label="配置 Key">
          <el-input v-model="temp.configKey" disabled></el-input>
        </el-form-item>
        <el-form-item label="费率值" prop="value">
          <el-input-number
            v-model="temp.value"
            :min="0"
            :max="100"
            :precision="4"
            :step="0.01"
            style="width: 100%;"
            placeholder="请输入费率 (0-100)"
          />
          <div style="margin-top: 4px; color: #909399; font-size: 12px;">
            当前值: {{ temp.value }}%（即 {{ (temp.value / 100).toFixed(4) }} 的小数费率）
          </div>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="temp.description" type="textarea" :rows="3" disabled></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const temp = ref({})
const formRef = ref(null)
const rules = {
  value: [
    { required: true, message: '请输入费率值', trigger: 'blur' },
    { type: 'number', message: '请输入有效的数字', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value === null || value === undefined || value === '') {
        callback(new Error('请输入费率值'))
      } else if (value < 0 || value > 100) {
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
    const res = await client.get('/admin/fee/user/list')
    list.value = res.rows || res.data?.items || res.data?.rows || (Array.isArray(res.data) ? res.data : [])
  } catch (e) {
    ElMessage.error('获取费率配置失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = (row) => {
  temp.value = { ...row, value: parseFloat(row.value) || 0 }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    await client.put(`/admin/fee/user/${temp.value.userId}`, { value: temp.value.value })
    ElMessage.success('费率配置保存成功')
    dialogVisible.value = false
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(getList)
</script>
