<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户名" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户名"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入手机号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="审核状态" prop="auditStatus">
        <el-select v-model="queryParams.auditStatus" placeholder="审核状态" clearable style="width: 200px">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userList">
      <el-table-column label="用户ID" align="center" prop="userId" width="80" />
      <el-table-column label="用户名" align="center" prop="userName" width="120" />
      <el-table-column label="昵称" align="center" prop="nickName" width="120" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="邮箱" align="center" prop="email" width="180" />
      <el-table-column label="KYC状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.kycStatus === 1" type="success">已认证</el-tag>
          <el-tag v-else-if="scope.row.kycStatus === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.kycStatus === 2" type="danger">已拒绝</el-tag>
          <el-tag v-else type="info">未认证</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="账户状态" align="center" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="注册时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button v-if="scope.row.kycStatus === 0" link type="success" icon="Check" @click="handleAudit(scope.row)">审核</el-button>
          <el-button link type="primary" icon="Setting" @click="handleFeeConfig(scope.row)">费率</el-button>
          <el-button link type="warning" icon="RefreshLeft" @click="handleReset2fa(scope.row)">重置2FA</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 用户详情对话框 -->
    <el-dialog title="用户详情" v-model="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border v-if="currentUser">
        <el-descriptions-item label="用户ID">{{ currentUser.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.userName }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentUser.nickName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ parseTime(currentUser.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="KYC状态">
          <el-tag v-if="currentUser.kycStatus === 1" type="success">已认证</el-tag>
          <el-tag v-else-if="currentUser.kycStatus === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="currentUser.kycStatus === 2" type="danger">已拒绝</el-tag>
          <el-tag v-else type="info">未认证</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="账户状态">
          <el-tag v-if="currentUser.status === 1" type="success">正常</el-tag>
          <el-tag v-else type="danger">禁用</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider content-position="left">KYC信息</el-divider>
      <el-descriptions :column="2" border v-if="currentUser?.kycInfo">
        <el-descriptions-item label="真实姓名">{{ currentUser.kycInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="证件类型">{{ currentUser.kycInfo.idType === 1 ? '身份证' : '护照' }}</el-descriptions-item>
        <el-descriptions-item label="证件号码">{{ currentUser.kycInfo.idCard }}</el-descriptions-item>
        <el-descriptions-item label="认证时间">{{ parseTime(currentUser.kycInfo.verifiedAt) }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无KYC信息" />

      <el-divider content-position="left">2FA信息</el-divider>
      <el-descriptions :column="1" border v-if="currentUser?.twoFactor">
        <el-descriptions-item label="2FA状态">
          <el-tag v-if="currentUser.twoFactor.enabled" type="success">已开启</el-tag>
          <el-tag v-else type="info">未开启</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="绑定时间" v-if="currentUser.twoFactor.enabled">
          {{ parseTime(currentUser.twoFactor.boundAt) }}
        </el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="未开启2FA" />
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog title="KYC审核" v-model="auditOpen" width="500px" append-to-body>
      <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
        <el-form-item label="用户">
          <el-input v-model="auditForm.userName" disabled />
        </el-form-item>
        <el-form-item label="审核结果" prop="result">
          <el-radio-group v-model="auditForm.result">
            <el-radio label="approve">通过</el-radio>
            <el-radio label="reject">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注" prop="remark">
          <el-input v-model="auditForm.remark" type="textarea" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAudit">确 定</el-button>
          <el-button @click="auditOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 费率配置对话框 -->
    <el-dialog title="用户费率配置" v-model="feeOpen" width="500px" append-to-body>
      <el-form ref="feeRef" :model="feeForm" label-width="120px">
        <el-form-item label="用户名">
          <el-input v-model="feeForm.userName" disabled />
        </el-form-item>
        <el-form-item label="充值费率" prop="rechargeRate">
          <el-input-number v-model="feeForm.rechargeRate" :min="0" :max="1" :precision="4" style="width: 100%">
            <template #append>%</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="开卡费" prop="openCardFee">
          <el-input-number v-model="feeForm.openCardFee" :min="0" :precision="2" style="width: 100%">
            <template #append>USD</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="交易手续费" prop="transactionFee">
          <el-input-number v-model="feeForm.transactionFee" :min="0" :precision="2" style="width: 100%">
            <template #append>USD</template>
          </el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFeeForm">确 定</el-button>
          <el-button @click="feeOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="UserAudit">
import { listUser, auditUser, changeUserStatus, resetUser2fa } from '@/api/admin/user'

const { proxy } = getCurrentInstance()

const userList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const detailOpen = ref(false)
const auditOpen = ref(false)
const feeOpen = ref(false)
const currentUser = ref(null)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: null,
    phone: null,
    auditStatus: null
  },
  auditForm: {},
  feeForm: {},
  auditRules: {
    result: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
    remark: [{ required: true, message: '请输入审核备注', trigger: 'blur' }]
  }
})

const { queryParams, auditForm, feeForm, auditRules } = toRefs(data)

/** 查询用户列表 */
function getList() {
  loading.value = true
  listUser(queryParams.value).then(response => {
    userList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 查看详情 */
function handleView(row) {
  currentUser.value = row
  detailOpen.value = true
}

/** 审核按钮 */
function handleAudit(row) {
  auditForm.value = {
    userId: row.userId,
    userName: row.userName,
    result: 'approve',
    remark: ''
  }
  auditOpen.value = true
}

/** 提交审核 */
function submitAudit() {
  proxy.$refs['auditRef'].validate(valid => {
    if (valid) {
      auditUser(auditForm.value.userId, {
        result: auditForm.value.result,
        remark: auditForm.value.remark
      }).then(() => {
        proxy.$modal.msgSuccess('审核完成')
        auditOpen.value = false
        getList()
      })
    }
  })
}

/** 费率配置 */
function handleFeeConfig(row) {
  feeForm.value = {
    userId: row.userId,
    userName: row.userName,
    rechargeRate: row.rechargeRate || 0.01,
    openCardFee: row.openCardFee || 0,
    transactionFee: row.transactionFee || 0.5
  }
  feeOpen.value = true
}

/** 提交费率配置 */
function submitFeeForm() {
  proxy.$modal.msgSuccess('费率配置保存成功')
  feeOpen.value = false
}

/** 状态变更 */
function handleStatusChange(row) {
  const text = row.status === 1 ? '启用' : '禁用'
  proxy.$modal.confirm('确认要"' + text + '"该用户账户吗？').then(() => {
    changeUserStatus(row.userId, row.status).then(() => {
      proxy.$modal.msgSuccess(text + '成功')
    })
  }).catch(() => {
    row.status = row.status === 1 ? 0 : 1
  })
}

/** 重置2FA */
function handleReset2fa(row) {
  proxy.$modal.confirm('确认重置该用户的2FA吗？重置后用户需要重新绑定。').then(() => {
    resetUser2fa(row.userId).then(() => {
      proxy.$modal.msgSuccess('2FA重置成功')
    })
  })
}

getList()
</script>
