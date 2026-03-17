<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 账户余额卡片 -->
      <el-col :span="8">
        <el-card class="balance-card">
          <template #header>
            <div class="card-header">
              <span>账户余额 (USD)</span>
            </div>
          </template>
          <div class="balance-amount">${{ accountBalance }}</div>
          <div class="balance-actions">
            <el-button type="primary" @click="handleRecharge">充值</el-button>
            <el-button @click="handleWithdraw">提现</el-button>
          </div>
        </el-card>
      </el-col>
      
      <!-- 快捷操作 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" plain icon="Plus" @click="$router.push('/merchant/card')">开卡</el-button>
            <el-button type="success" plain icon="List" @click="$router.push('/merchant/transaction')">交易记录</el-button>
            <el-button type="info" plain icon="User" @click="$router.push('/merchant/cardHolder')">持卡人管理</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 充值记录 -->
    <el-card class="recharge-list">
      <template #header>
        <div class="card-header">
          <span>充值记录</span>
        </div>
      </template>
      
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="充值状态" clearable style="width: 200px">
            <el-option label="处理中" :value="0" />
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="rechargeList">
        <el-table-column label="订单号" align="center" prop="orderNo" width="180" />
        <el-table-column label="充值金额" align="center" width="120">
          <template #default="scope">
            <span>{{ scope.row.amount }} {{ scope.row.currency }}</span>
          </template>
        </el-table-column>
        <el-table-column label="手续费" align="center" width="100">
          <template #default="scope">
            <span>{{ scope.row.fee }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实际到账" align="center" width="120">
          <template #default="scope">
            <span>{{ scope.row.actualAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="warning">处理中</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">成功</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createdAt" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="完成时间" align="center" prop="completedAt" width="180">
          <template #default="scope">
            <span>{{ scope.row.completedAt ? parseTime(scope.row.completedAt) : '-' }}</span>
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
    </el-card>

    <!-- 充值对话框 -->
    <el-dialog title="账户充值" v-model="open" width="500px" append-to-body>
      <el-form ref="rechargeRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="form.amount" :min="10" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-select v-model="form.currency" placeholder="请选择币种" style="width: 100%">
            <el-option label="USD" value="USD" />
            <el-option label="USDT" value="USDT" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="form.payType">
            <el-radio label="usdt">USDT 转账</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.payType === 'usdt'">
          <el-alert type="info" :closable="false">
            <template #default>
              <div>请将 USDT 转账至以下地址：</div>
              <div class="usdt-address">TRC20: TJnZ...8XzP</div>
              <div class="usdt-tip">转账完成后，系统将在确认到账后自动充值到您的账户</div>
            </template>
          </el-alert>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Recharge">
import { listRecharge, submitRecharge } from '@/api/recharge'
import { getUserAccount } from '@/api/recharge'

const { proxy } = getCurrentInstance()

const rechargeList = ref([])
const loading = ref(true)
const total = ref(0)
const open = ref(false)
const accountBalance = ref('0.00')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    status: null
  },
  rules: {
    amount: [{ required: true, message: '请输入充值金额', trigger: 'blur' }],
    currency: [{ required: true, message: '请选择币种', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询充值记录 */
function getList() {
  loading.value = true
  listRecharge(queryParams.value).then(response => {
    rechargeList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 查询账户余额 */
function getBalance() {
  getUserAccount('USD').then(response => {
    accountBalance.value = response.data?.balance || '0.00'
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

/** 充值按钮 */
function handleRecharge() {
  reset()
  open.value = true
}

/** 提现按钮 */
function handleWithdraw() {
  proxy.$modal.msg('提现功能开发中')
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs['rechargeRef'].validate(valid => {
    if (valid) {
      proxy.$modal.msg('USDT 充值请直接转账到指定地址，系统将自动到账')
      open.value = false
    }
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    amount: 100,
    currency: 'USDT',
    payType: 'usdt'
  }
  proxy.resetForm('rechargeRef')
}

getList()
getBalance()
</script>

<style scoped>
.balance-card {
  margin-bottom: 20px;
}
.balance-amount {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  text-align: center;
  padding: 20px 0;
}
.balance-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}
.quick-actions {
  display: flex;
  gap: 10px;
  padding: 10px;
}
.recharge-list {
  margin-top: 20px;
}
.usdt-address {
  font-family: monospace;
  background: #f5f7fa;
  padding: 5px 10px;
  margin: 10px 0;
  border-radius: 4px;
}
.usdt-tip {
  font-size: 12px;
  color: #909399;
}
</style>
