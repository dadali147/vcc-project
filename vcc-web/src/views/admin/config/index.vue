<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 系统参数配置 -->
      <el-tab-pane label="系统参数配置" name="config">
        <el-form :model="configQueryParams" ref="configQueryRef" :inline="true" v-show="showSearch">
          <el-form-item label="配置键" prop="configKey">
            <el-input v-model="configQueryParams.configKey" placeholder="请输入配置键" clearable @keyup.enter="handleConfigQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleConfigQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetConfigQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getConfigList" />
        </el-row>

        <el-table :data="configList" v-loading="configLoading">
          <el-table-column label="配置键" prop="configKey" min-width="200" />
          <el-table-column label="配置值" prop="configValue" min-width="150" />
          <el-table-column label="说明" prop="remark" min-width="200" />
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleEditConfig(scope.row)">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="configTotal > 0" :total="configTotal" v-model:page="configQueryParams.pageNum" v-model:limit="configQueryParams.pageSize" @pagination="getConfigList" />
      </el-tab-pane>

      <!-- 角色权限管理 -->
      <el-tab-pane label="角色权限管理" name="role">
        <el-alert type="info" :closable="false" style="margin-bottom: 16px">
          角色权限管理复用系统角色模块，点击下方链接跳转至角色管理页面进行配置。
        </el-alert>
        <el-table :data="roleList">
          <el-table-column label="角色名称" prop="roleName" min-width="150" />
          <el-table-column label="角色标识" prop="roleKey" min-width="150" />
          <el-table-column label="说明" prop="remark" min-width="200" />
        </el-table>
        <div style="margin-top: 16px">
          <router-link to="/system/role">
            <el-button type="primary" plain icon="Link">前往角色管理</el-button>
          </router-link>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 修改配置对话框 -->
    <el-dialog title="修改系统配置" v-model="editOpen" width="500px" append-to-body>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="配置键">
          <el-input v-model="editForm.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-switch v-if="isBooleanConfig(editForm.configKey)" v-model="editForm.configValue" active-value="true" inactive-value="false" />
          <el-input v-else v-model="editForm.configValue" placeholder="请输入配置值" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="editForm.remark" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitEditConfig">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminConfig">
import { listSysConfig, updateSysConfig } from "@/api/admin/config"

const { proxy } = getCurrentInstance()

const activeTab = ref('config')
const showSearch = ref(true)

// 系统配置
const configList = ref([])
const configLoading = ref(true)
const configTotal = ref(0)
const configQueryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  configKey: undefined
})

// 角色列表（静态展示）
const roleList = ref([
  { roleName: '管理员', roleKey: 'admin', remark: '系统管理员，拥有所有权限' },
  { roleName: '商户', roleKey: 'merchant', remark: '商户用户，拥有商户端操作权限' },
  { roleName: '只读', roleKey: 'readonly', remark: '只读用户，仅可查看数据' }
])

// 编辑配置
const editOpen = ref(false)
const editForm = reactive({
  configKey: '',
  configValue: '',
  remark: ''
})
const editRules = {
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }]
}

const booleanKeys = ['risk.recharge.enabled', 'risk.card.open.enabled']

function isBooleanConfig(key) {
  return booleanKeys.includes(key)
}

function getConfigList() {
  configLoading.value = true
  listSysConfig(configQueryParams).then(response => {
    configList.value = response.rows
    configTotal.value = response.total
    configLoading.value = false
  })
}

function handleTabClick() {}

function handleConfigQuery() {
  configQueryParams.pageNum = 1
  getConfigList()
}

function resetConfigQuery() {
  proxy.resetForm("configQueryRef")
  handleConfigQuery()
}

function handleEditConfig(row) {
  editForm.configKey = row.configKey
  editForm.configValue = row.configValue
  editForm.remark = row.remark
  editOpen.value = true
}

function submitEditConfig() {
  proxy.$refs["editFormRef"].validate(valid => {
    if (valid) {
      updateSysConfig(editForm.configKey, editForm.configValue).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        editOpen.value = false
        getConfigList()
      })
    }
  })
}

onMounted(() => {
  getConfigList()
})
</script>
