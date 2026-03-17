<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧：钱包余额 -->
      <el-col :span="8">
        <el-card class="wallet-card">
          <template #header>
            <div class="card-header">
              <span>我的钱包</span>
              <el-button type="primary" link icon="Refresh" @click="refreshBalance">刷新</el-button>
            </div>
          </template>
          <div class="wallet-balance">
            <div class="balance-label">总资产 (USD)</div>
            <div class="balance-value">${{ totalBalance }}</div>
            <div class="balance-change" :class="balanceChange >= 0 ? 'up' : 'down'">
              <el-icon><CaretTop v-if="balanceChange >= 0" /><CaretBottom v-else /></el-icon>
              {{ Math.abs(balanceChange) }}% (24h)
            </div>
          </div>
          <el-divider />
          <div class="wallet-currencies">
            <div class="currency-item" v-for="item in currencyList" :key="item.currency">
              <div class="currency-info">
                <div class="currency-name">{{ item.currency }}</div>
                <div class="currency-amount">{{ item.balance }}</div>
              </div>
              <div class="currency-action">
                <el-button type="primary" link @click="handleRecharge(item.currency)">充值</el-button>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 快捷入口 -->
        <el-card style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>快捷入口</span>
            </div>
          </template>
          <div class="quick-links">
            <el-button type="primary" plain @click="$router.push('/merchant/card')">卡片管理</el-button>
            <el-button type="success" plain @click="$router.push('/merchant/transaction')">交易记录</el-button>
            <el-button type="warning" plain @click="$router.push('/merchant/recharge')">充值中心</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：动账明细 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>动账明细</span>
              <div class="header-actions">
                <el-radio-group v-model="filterType" size="small" @change="handleFilterChange">
                  <el-radio-button label="all">全部</el-radio-button>
                  <el-radio-button label="income">收入</el-radio-button>
                  <el-radio-button label="expense">支出</el-radio-button>
                </el-radio-group>
                <el-button type="primary" link icon="Download" @click="handleExport">导出</el-button>
              </div>
            </div>
          </template>

          <el-timeline v-loading="loading">
            <el-timeline-item
              v-for="(item, index) in transactionList"
              :key="index"
              :type="item.type"
              :timestamp="parseTime(item.time)"
            >
              <div class="transaction-item">
                <div class="transaction-icon" :class="item.type">
                  <el-icon v-if="item.type === 'success'"><Plus /></el-icon>
                  <el-icon v-else-if="item.type === 'danger'"><Minus /></el-icon>
                  <el-icon v-else><Refresh /></el-icon>
                </div>
                <div class="transaction-info">
                  <div class="transaction-title">{{ item.title }}</div>
                  <div class="transaction-desc">{{ item.description }}</div>
                </div>
                <div class="transaction-amount" :class="item.amount > 0 ? 'income' : 'expense'">
                  {{ item.amount > 0 ? '+' : '' }}${{ Math.abs(item.amount).toFixed(2) }}
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>

          <el-empty v-if="transactionList.length === 0" description="暂无动账记录" />

          <pagination
            v-show="total > 0"
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="getList"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 充值对话框 -->
    <el-dialog title="充值" v-model="rechargeOpen" width="500px" append-to-body>
      <el-form ref="rechargeRef" :model="rechargeForm" :rules="rechargeRules" label-width="100px">
        <el-form-item label="充值币种">
          <el-input v-model="rechargeForm.currency" disabled />
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="rechargeForm.amount" :min="10" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="rechargeForm.payType">
            <el-radio label="usdt">USDT (TRC20)</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitRecharge">确 定</el-button>
          <el-button @click="rechargeOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Wallet">
import { getUserAccount } from '@/api/recharge'
import { listRecharge } from '@/api/recharge'
import { listTransaction } from '@/api/transaction'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const total = ref(0)
const filterType = ref('all')
const rechargeOpen = ref(false)

const totalBalance = ref('0.00')
const balanceChange = ref(0)

const currencyList = ref([
  { currency: 'USD', balance: '0.00' },
  { currency: 'USDT', balance: '0.00' }
])

const transactionList = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const rechargeForm = ref({
  currency: 'USD',
  amount: 100,
  payType: 'usdt'
})

const rechargeRules = {
  amount: [{ required: true, message: '请输入充值金额', trigger: 'blur' }]
}

/** 刷新余额 */
function refreshBalance() {
  getBalance()
  proxy.$modal.msgSuccess('余额已刷新')
}

/** 获取余额 */
function getBalance() {
  getUserAccount('USD').then(response => {
    const balance = response.data?.balance || '0.00'
    totalBalance.value = balance
    currencyList.value[0].balance = balance
  })
}

/** 获取动账明细 */
function getList() {
  loading.value = true
  
  // 合并充值和交易记录作为动账明细
  const transactions = []
  
  // 获取充值记录
  listRecharge({ pageNum: 1, pageSize: 50 }).then(rechargeRes => {
    const recharges = rechargeRes.rows || []
    recharges.forEach(r => {
      if (r.status === 1) { // 成功的充值
        transactions.push({
          type: 'success',
          time: r.completedAt || r.createdAt,
          title: '账户充值',
          description: `充值 ${r.amount} ${r.currency}`,
          amount: parseFloat(r.amount)
        })
      }
    })
    
    // 获取交易记录（消费）
    listTransaction({ pageNum: 1, pageSize: 50 }).then(txnRes => {
      const txns = txnRes.rows || []
      txns.forEach(t => {
        if (t.status === 1) {
          transactions.push({
            type: 'danger',
            time: t.createdAt,
            title: '卡片消费',
            description: `${t.merchantName || '商户消费'}`,
            amount: -parseFloat(t.amount)
          })
        }
      })
      
      // 按时间排序
      transactions.sort((a, b) => new Date(b.time) - new Date(a.time))
      
      // 过滤
      let filtered = transactions
      if (filterType.value === 'income') {
        filtered = transactions.filter(t => t.amount > 0)
      } else if (filterType.value === 'expense') {
        filtered = transactions.filter(t => t.amount < 0)
      }
      
      // 分页
      const start = (queryParams.pageNum - 1) * queryParams.pageSize
      const end = start + queryParams.pageSize
      transactionList.value = filtered.slice(start, end)
      total.value = filtered.length
      
      loading.value = false
    })
  })
}

/** 筛选变化 */
function handleFilterChange() {
  queryParams.pageNum = 1
  getList()
}

/** 充值 */
function handleRecharge(currency) {
  rechargeForm.value.currency = currency
  rechargeForm.value.amount = 100
  rechargeOpen.value = true
}

/** 提交充值 */
function submitRecharge() {
  proxy.$refs['rechargeRef'].validate(valid => {
    if (valid) {
      proxy.$modal.msg('请使用 USDT 转账到指定地址完成充值')
      rechargeOpen.value = false
    }
  })
}

/** 导出 */
function handleExport() {
  proxy.$modal.msg('导出功能开发中')
}

getBalance()
getList()
</script>

<style scoped>
.wallet-card {
  text-align: center;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.wallet-balance {
  padding: 20px 0;
}
.balance-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 10px;
}
.balance-value {
  font-size: 36px;
  font-weight: bold;
  color: #262626;
}
.balance-change {
  margin-top: 10px;
  font-size: 14px;
}
.balance-change.up {
  color: #52c41a;
}
.balance-change.down {
  color: #ff4d4f;
}

.wallet-currencies {
  text-align: left;
}
.currency-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.currency-item:last-child {
  border-bottom: none;
}
.currency-name {
  font-size: 14px;
  color: #8c8c8c;
}
.currency-amount {
  font-size: 18px;
  font-weight: bold;
  color: #262626;
}

.quick-links {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.transaction-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #fafafa;
  border-radius: 8px;
}
.transaction-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 18px;
}
.transaction-icon.success {
  background: #f6ffed;
  color: #52c41a;
}
.transaction-icon.danger {
  background: #fff2f0;
  color: #ff4d4f;
}
.transaction-icon.warning {
  background: #fffbe6;
  color: #faad14;
}
.transaction-info {
  flex: 1;
}
.transaction-title {
  font-weight: bold;
  margin-bottom: 5px;
}
.transaction-desc {
  font-size: 12px;
  color: #8c8c8c;
}
.transaction-amount {
  font-size: 16px;
  font-weight: bold;
}
.transaction-amount.income {
  color: #52c41a;
}
.transaction-amount.expense {
  color: #ff4d4f;
}
</style>
