<template>
  <div class="app-container">
    <el-alert type="warning" show-icon :closable="false" style="margin-bottom: 16px;">
      <template #title>系统配置修改后立即生效，敏感配置（如 API 密钥）请谨慎操作。</template>
    </el-alert>

    <el-table :data="list" v-loading="loading" border style="width: 100%;">
      <el-table-column prop="configKey" label="配置 Key" min-width="200" />
      <el-table-column prop="configValue" label="配置值" min-width="200">
        <template #default="scope">
          <span v-if="isSensitive(scope.row.configKey)">{{ '•'.repeat(12) }}</span>
          <span v-else>{{ scope.row.configValue }}</span>
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

    <el-dialog v-model="dialogVisible" title="编辑系统配置" width="520px">
      <el-form :model="temp" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="配置 Key">
          <el-input v-model="temp.configKey" disabled></el-input>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="temp.description" disabled></el-input>
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input
            v-model="temp.configValue"
            :type="isSensitive(temp.configKey) ? 'password' : 'textarea'"
            :rows="4"
            :show-password="isSensitive(temp.configKey)"
            placeholder="请输入配置值"
          ></el-input>
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
import client from '@/api/client'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const temp = ref({})
const formRef = ref(null)
const rules = {
  configValue: [{ required: true, message: '配置值不能为空', trigger: 'blur' }]
}

const SENSITIVE_KEYS = ['aes_key', 'rsa_private_key', 'rsa_public_key', 'api_secret', 'webhook_secret', 'password']

const isSensitive = (key) => {
  if (!key) return false
  return SENSITIVE_KEYS.some(k => key.toLowerCase().includes(k))
}

const getList = async () => {
  loading.value = true
  try {
    const res = await client.get('/admin/system-config/list')
    list.value = res.data?.items || res.data?.rows || (Array.isArray(res.data) ? res.data : [])
  } catch (e) {
    ElMessage.error('获取系统配置失败')
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
    if (isSensitive(temp.value.configKey)) {
      await ElMessageBox.confirm('您正在修改敏感配置项，确认继续？', '安全确认', { type: 'warning' })
    }
    saving.value = true
    await client.post('/admin/system-config/set', {
      configKey: temp.value.configKey,
      configValue: temp.value.configValue
    })
    ElMessage.success('保存成功')
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
