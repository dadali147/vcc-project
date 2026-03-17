<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="用户名" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户名" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="KYC状态" prop="kycStatus">
        <el-select v-model="queryParams.kycStatus" placeholder="全部" clearable style="width: 140px">
          <el-option label="未认证" value="0" />
          <el-option label="已认证" value="1" />
          <el-option label="审核中" value="2" />
          <el-option label="认证失败" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="账户状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="正常" value="0" />
          <el-option label="禁用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table :data="userList" v-loading="loading">
      <el-table-column label="用户名" prop="userName" width="120" />
      <el-table-column label="昵称" prop="nickName" width="120" />
      <el-table-column label="手机号" prop="phonenumber" width="140" />
      <el-table-column label="邮箱" prop="email" width="180" show-overflow-tooltip />
      <el-table-column label="KYC状态" prop="kycStatus" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.kycStatus === '1' ? 'success' : scope.row.kycStatus === '2' ? 'warning' : scope.row.kycStatus === '3' ? 'danger' : 'info'" size="small">
            {{ scope.row.kycStatus === '0' ? '未认证' : scope.row.kycStatus === '1' ? '已认证' : scope.row.kycStatus === '2' ? '审核中' : '认证失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="账户状态" prop="status" width="90" align="center">
        <template #default="scope">
          <el-switch v-model="scope.row.status" active-value="0" inactive-value="1"
            @change="handleStatusChange(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="注册时间" prop="createTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" align="center">
        <template #default="scope">
          <el-button link type="primary" v-if="scope.row.kycStatus === '2'" @click="handleAudit(scope.row)">KYC审核</el-button>
          <el-button link type="primary" @click="handleRate(scope.row)">费率配置</el-button>
          <el-button link type="warning" @click="handleReset2fa(scope.row)">重置2FA</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- KYC审核对话框 -->
    <el-dialog title="KYC审核" v-model="openAudit" width="500px" append-to-body>
      <el-descriptions :column="1" border style="margin-bottom: 20px">
        <el-descriptions-item label="用户名">{{ auditUser.userName }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ auditUser.realName }}</el-descriptions-item>
        <el-descriptions-item label="证件类型">{{ auditUser.idType === '1' ? '身份证' : '护照' }}</el-descriptions-item>
        <el-descriptions-item label="证件号码">{{ auditUser.idNumber ? auditUser.idNumber.replace(/^(.{4})(.*)(.{4})$/, (m, p1, p2, p3) => p1 + '*'.repeat(p2.length) + p3) : '' }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="auditForm" :rules="auditRules" ref="auditRef" label-width="80px">
        <el-form-item label="审核结果" prop="result">
          <el-radio-group v-model="auditForm.result">
            <el-radio value="1">通过</el-radio>
            <el-radio value="3">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注" prop="remark" :rules="auditForm.result === '3' ? [{ required: true, message: '拒绝时备注不能为空', trigger: 'blur' }] : []">
          <el-input v-model="auditForm.remark" type="textarea" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="openAudit = false">取 消</el-button>
        <el-button type="primary" @click="submitAudit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 费率配置对话框 -->
    <el-dialog title="费率配置" v-model="openRate" width="500px" append-to-body>
      <el-form :model="rateForm" :rules="rateRules" ref="rateRef" label-width="120px">
        <el-form-item label="开卡费折扣 %" prop="openFeeDiscount">
          <el-input-number v-model="rateForm.openFeeDiscount" :min="0" :max="100" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="交易手续费 %" prop="transRate">
          <el-input-number v-model="rateForm.transRate" :min="0" :max="100" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="充值手续费 %" prop="rechargeRate">
          <el-input-number v-model="rateForm.rechargeRate" :min="0" :max="100" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="openRate = false">取 消</el-button>
        <el-button type="primary" @click="submitRate">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminUser">
import { listUser, getUser, auditKyc, updateUserRate, reset2fa, changeUserStatus } from "@/api/admin/user"

const { proxy } = getCurrentInstance()

const userList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const openAudit = ref(false)
const openRate = ref(false)
const auditUser = ref({})

const auditForm = reactive({
  userId: undefined,
  result: '1',
  remark: ''
})

const rateForm = reactive({
  userId: undefined,
  openFeeDiscount: 100,
  transRate: 0,
  rechargeRate: 0
})

const auditRules = {
  result: [{ required: true, message: "请选择审核结果", trigger: "change" }]
}

const rateValidator = (rule, value, callback) => {
  if (value < 0 || value > 100) {
    callback(new Error("数值范围应在 0~100 之间"))
  } else {
    callback()
  }
}

const rateRules = {
  openFeeDiscount: [{ required: true, message: "请输入开卡费折扣", trigger: "blur" }, { validator: rateValidator, trigger: "blur" }],
  transRate: [{ required: true, message: "请输入交易手续费", trigger: "blur" }, { validator: rateValidator, trigger: "blur" }],
  rechargeRate: [{ required: true, message: "请输入充值手续费", trigger: "blur" }, { validator: rateValidator, trigger: "blur" }]
}

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    kycStatus: undefined,
    status: undefined
  }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listUser(queryParams.value).then(response => {
    userList.value = response.rows
    total.value = response.total
  }).finally(() => {
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

function handleAudit(row) {
  getUser(row.userId).then(response => {
    auditUser.value = response.data
    auditForm.userId = row.userId
    auditForm.result = '1'
    auditForm.remark = ''
    openAudit.value = true
  })
}

function submitAudit() {
  proxy.$refs["auditRef"].validate(valid => {
    if (!valid) return
    auditKyc(auditForm).then(() => {
      proxy.$modal.msgSuccess("审核成功")
      openAudit.value = false
      getList()
    })
  })
}

function handleRate(row) {
  getUser(row.userId).then(response => {
    const userData = response.data
    rateForm.userId = row.userId
    rateForm.openFeeDiscount = userData.openFeeDiscount || 100
    rateForm.transRate = userData.transRate || 0
    rateForm.rechargeRate = userData.rechargeRate || 0
    openRate.value = true
  })
}

function submitRate() {
  proxy.$refs["rateRef"].validate(valid => {
    if (!valid) return
    updateUserRate(rateForm).then(() => {
      proxy.$modal.msgSuccess("配置成功")
      openRate.value = false
    })
  })
}

function handleReset2fa(row) {
  proxy.$modal.confirm('确认重置用户 "' + row.userName + '" 的2FA?').then(() => {
    return reset2fa(row.userId)
  }).then(() => {
    proxy.$modal.msgSuccess("重置成功")
  }).catch(() => {})
}

function handleStatusChange(row) {
  const text = row.status === "0" ? "启用" : "禁用"
  proxy.$modal.confirm('确认' + text + '用户 "' + row.userName + '" ?').then(() => {
    return changeUserStatus(row.userId, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(() => {
    row.status = row.status === "0" ? "1" : "0"
  })
}

onMounted(() => {
  getList()
})
</script>
