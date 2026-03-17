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
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="walletList">
      <el-table-column label="用户ID" align="center" prop="userId" width="80" />
      <el-table-column label="用户名" align="center" prop="userName" width="120" />
      <el-table-column label="昵称" align="center" prop="nickName" width="120" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="USD余额" align="center" width="120">
        <template #default="scope">
          <span class="balance">${{ scope.row.usdBalance || '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="USDT余额" align="center" width="120">
        <template #default="scope">
          <span class="balance">{{ scope.row.usdtBalance || '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="累计充值" align="center" width="120">
        <template #default="scope">
          <span>${{ scope.row.totalRecharge || '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="累计消费" align="center" width="120">
        <template #default="scope">
          <span>${{ scope.row.totalSpend || '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="warning" icon="Edit" @click="handleAdjust(scope.row)">调整余额</el-button>
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

    <!-- 钱包详情对话框 -->
    <el-dialog title="钱包详情" v-model="detailOpen" width="800px" append-to-body>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>用户信息</span>
              </div>
            </template>
            <el-descriptions :column="1" v-if="currentWallet">
              <el-descriptions-item label="用户名">{{ currentWallet.userName }}</el-descriptions-item>
              <el-descriptions-item label="昵称">{{ currentWallet.nickName }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ currentWallet.phone }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ currentWallet.email }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>余额信息</span>
              </div>
            </template>
            <el-descriptions :column="1" v-if="currentWallet">
              <el-descriptions-item label="USD余额">${{ currentWallet.usdBalance || '0.00' }}</el-descriptions-item>
              <el-descriptions-item label="USDT余额">{{ currentWallet.usdtBalance || '0.00' }}</el-descriptions-item>
              <el-descriptions-item label="累计充值">${{ currentWallet.totalRecharge || '0.00' }}</el-descriptions-item>
              <el-descriptions-item label="累计消费">${{ currentWallet.totalSpend || '0.00' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px">
        <template #header>
          <div class="card-header">
            <span>最近动账明细</span>
          </div>
        </template>
        <el-table :data="transactionList" size="small">
          <el-table-column label="时间" prop="createdAt" width="160">
            <template #default="scope">
              {{ parseTime(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="类型" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.type === 'RECHARGE'" type="success">充值</el-tag>
              <el-tag v-else-if="scope.row.type === 'WITHDRAW'" type="warning">提现</el-tag>
              <el-tag v-else-if="scope.row.type === 'CARD_CHARGE'" type="danger">开卡扣费</el-tag>
              <el-tag v-else type="info">其他</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120">
            <template #default="scope">
              <span :class="scope.row.amount > 0 ? 'text-success' : 'text-danger'">
                {{ scope.row.amount > 0 ? '+' : '' }}${{ Math.abs(scope.row.amount).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" />
        </el-table>
      </el-card>
    </el-dialog>

    <!-- 调整余额对话框 -->
    <el-dialog title="调整余额" v-model="adjustOpen" width="500px" append-to-body>
      <el-form ref="adjustRef" :model="adjustForm" :rules="adjustRules" label-width="100px">
        <el-form-item label="用户">
          <el-input v-model="adjustForm.userName" disabled />
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-select v-model="adjustForm.currency" placeholder="请选择币种" style="width: 100%">
            <el-option label="USD" value="USD" />
            <el-option label="USDT" value="USDT" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整类型" prop="adjustType">
          <el-radio-group v-model="adjustForm.adjustType">
            <el-radio label="add">增加</el-radio>
            <el-radio label="subtract">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整金额" prop="amount">
          <el-input-number v-model="adjustForm.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input v-model="adjustForm.reason" type="textarea" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAdjust">确 定</el-button>
          <el-button @click="adjustOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WalletAdmin">
import { listWalletAdmin, getWalletAdmin, adjustBalance, listWalletTransactions } from '@/api/admin/wallet'

const { proxy } = getCurrentInstance()

const walletList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const detailOpen = ref(false)
const adjustOpen = ref(false)
const currentWallet = ref(null)
const transactionList = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: null,
    phone: null
  },
  adjustForm: {},
  adjustRules: {
    currency: [{ required: true, message: '请选择币种', trigger: 'change' }],
    adjustType: [{ required: true, message: '请选择调整类型', trigger: 'change' }],
    amount: [{ required: true, message: '请输入调整金额', trigger: 'blur' }],
    reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
  }
})

const { queryParams, adjustForm, adjustRules } = toRefs(data)

/** 查询钱包列表 */
function getList() {
  loading.value = true
  listWalletAdmin(queryParams.value).then(response => {
    walletList.value = response.rows
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
  getWalletAdmin(row.userId).then(response => {
    currentWallet.value = response.data
    detailOpen.value = true
    // 加载动账明细
    listWalletTransactions(row.userId, { pageNum: 1, pageSize: 10 }).then(res => {
      transactionList.value = res.rows || []
    })
  })
}

/** 调整余额 */
function handleAdjust(row) {
  adjustForm.value = {
    userId: row.userId,
    userName: row.userName,
    currency: 'USD',
    adjustType: 'add',
    amount: 0,
    reason: ''
  }
  adjustOpen.value = true
}

/** 提交调整 */
function submitAdjust() {
  proxy.$refs['adjustRef'].validate(valid => {
    if (valid) {
      adjustBalance(adjustForm.value.userId, {
        currency: adjustForm.value.currency,
        amount: adjustForm.value.adjustType === 'add' ? adjustForm.value.amount : -adjustForm.value.amount,
        reason: adjustForm.value.reason
      }).then(() => {
        proxy.$modal.msgSuccess('余额调整成功')
        adjustOpen.value = false
        getList()
      })
    }
  })
}

getList()
</script>

<style scoped>
.balance {
  font-weight: bold;
  color: #409EFF;
}
.card-header {
  font-weight: bold;
}
.text-success {
  color: #52c41a;
}
.text-danger {
  color: #ff4d4f;
}
</style>
