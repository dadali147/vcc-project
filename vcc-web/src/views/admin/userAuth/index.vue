<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="用户名" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table :data="userAuthList" v-loading="loading">
      <el-table-column label="用户ID" prop="userId" width="80" align="center" />
      <el-table-column label="用户名" prop="userName" min-width="120" />
      <el-table-column label="手机号" prop="phone" min-width="130" />
      <el-table-column label="2FA状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.twoFaEnabled ? 'success' : 'info'">
            {{ scope.row.twoFaEnabled ? '已启用' : '未启用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="3DS状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.activeOtp ? 'warning' : 'info'">
            {{ scope.row.activeOtp ? '有活跃OTP' : '无' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后登录时间" prop="lastLoginTime" min-width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.lastLoginTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView2fa(scope.row)">2FA详情</el-button>
          <el-button link type="warning" icon="RefreshRight" @click="handleReset2fa(scope.row)">重置2FA</el-button>
          <el-button link type="danger" icon="Delete" @click="handleForceUnbind(scope.row)">强制解绑</el-button>
          <el-button link type="info" icon="Key" @click="handleView3dsOtp(scope.row)">3DS OTP</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 2FA详情对话框 -->
    <el-dialog title="2FA详情" v-model="faDetailOpen" width="500px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ faDetail.userName }}</el-descriptions-item>
        <el-descriptions-item label="2FA状态">
          <el-tag :type="faDetail.enabled ? 'success' : 'info'">{{ faDetail.enabled ? '已启用' : '未启用' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="绑定时间">{{ parseTime(faDetail.bindTime) }}</el-descriptions-item>
        <el-descriptions-item label="绑定设备">{{ faDetail.device || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="faDetailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 3DS OTP查询对话框 -->
    <el-dialog title="查询3DS OTP" v-model="otpQueryOpen" width="500px" append-to-body>
      <el-alert type="warning" :closable="false" style="margin-bottom: 16px">
        此操作为管理员紧急查询，请填写查询原因。
      </el-alert>
      <el-form ref="otpFormRef" :model="otpForm" :rules="otpRules" label-width="100px">
        <el-form-item label="查询原因" prop="reason">
          <el-input v-model="otpForm.reason" type="textarea" :rows="3" placeholder="请输入查询原因" />
        </el-form-item>
      </el-form>
      <div v-if="otpResult" style="margin-top: 16px">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="OTP码">
            <span style="font-size: 18px; font-weight: bold; color: #e6a23c">{{ otpResult.otp }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="有效期至">{{ parseTime(otpResult.expireTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="otpQueryOpen = false">关 闭</el-button>
        <el-button type="primary" @click="submitOtpQuery" v-if="!otpResult">查 询</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminUserAuth">
import { listUserAuth, getUser2faDetail, resetUser2fa, getUser3dsOtp, forceUnbind2fa } from "@/api/admin/userAuth"

const { proxy } = getCurrentInstance()

const userAuthList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phone: undefined
  }
})
const { queryParams } = toRefs(data)

// 2FA详情
const faDetailOpen = ref(false)
const faDetail = reactive({
  userName: '',
  enabled: false,
  bindTime: null,
  device: ''
})

// 3DS OTP查询
const otpQueryOpen = ref(false)
const otpForm = reactive({
  userId: undefined,
  reason: ''
})
const otpResult = ref(null)
const otpRules = {
  reason: [{ required: true, message: '请输入查询原因', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listUserAuth(queryParams.value).then(response => {
    userAuthList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleView2fa(row) {
  getUser2faDetail(row.userId).then(response => {
    faDetail.userName = row.userName
    faDetail.enabled = response.data.enabled
    faDetail.bindTime = response.data.bindTime
    faDetail.device = response.data.device
    faDetailOpen.value = true
  })
}

function handleReset2fa(row) {
  proxy.$modal.confirm('确认重置用户 "' + row.userName + '" 的2FA？此操作不可恢复。').then(() => {
    return resetUser2fa(row.userId)
  }).then(() => {
    proxy.$modal.msgSuccess("重置成功")
    getList()
  }).catch(() => {})
}

function handleForceUnbind(row) {
  proxy.$modal.confirm('确认强制解绑用户 "' + row.userName + '" 的2FA？此操作不可恢复。').then(() => {
    return forceUnbind2fa(row.userId)
  }).then(() => {
    proxy.$modal.msgSuccess("解绑成功")
    getList()
  }).catch(() => {})
}

function handleView3dsOtp(row) {
  otpForm.userId = row.userId
  otpForm.reason = ''
  otpResult.value = null
  otpQueryOpen.value = true
}

function submitOtpQuery() {
  proxy.$refs["otpFormRef"].validate(valid => {
    if (valid) {
      proxy.$modal.confirm('确认查询该用户的3DS OTP？此操作将被记录审计日志。').then(() => {
        return getUser3dsOtp(otpForm.userId, otpForm.reason)
      }).then(response => {
        otpResult.value = response.data
      }).catch(() => {})
    }
  })
}

onMounted(() => {
  getList()
})
</script>
