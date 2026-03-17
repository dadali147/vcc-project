<template>
  <div class="app-container">
    <!-- 账户余额卡片 -->
    <el-row :gutter="16" class="wallet-cards">
      <el-col :sm="8" :xs="24">
        <el-card shadow="hover" class="balance-card">
          <div class="balance-label">USD 余额</div>
          <div class="balance-value">$ {{ wallet.usdBalance || '0.00' }}</div>
          <div class="balance-footer">可用余额</div>
        </el-card>
      </el-col>
      <el-col :sm="8" :xs="24">
        <el-card shadow="hover" class="balance-card">
          <div class="balance-label">USDT 余额</div>
          <div class="balance-value">{{ wallet.usdtBalance || '0.00' }} USDT</div>
          <div class="balance-footer">可用余额</div>
        </el-card>
      </el-col>
      <el-col :sm="8" :xs="24">
        <el-card shadow="hover" class="balance-card">
          <div class="balance-label">冻结金额</div>
          <div class="balance-value">$ {{ wallet.frozenBalance || '0.00' }}</div>
          <div class="balance-footer">
            <el-button type="primary" size="small" @click="$router.push('/merchant/recharge')">去充值</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 动账明细 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>动账明细</span>
        </div>
      </template>

      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="变动类型" prop="changeType">
          <el-select v-model="queryParams.changeType" placeholder="全部" clearable style="width: 160px">
            <el-option label="充值" value="1" />
            <el-option label="开卡扣费" value="2" />
            <el-option label="卡片消费" value="3" />
            <el-option label="退款" value="4" />
            <el-option label="手续费" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围" prop="dateRange">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="-"
            start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logList" v-loading="loading">
        <el-table-column label="变动时间" prop="createTime" width="170">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="变动类型" prop="changeTypeName" width="120" />
        <el-table-column label="变动金额" prop="amount" width="150" align="right">
          <template #default="scope">
            <span :style="{ color: scope.row.amount >= 0 ? '#67c23a' : '#f56c6c' }">
              {{ scope.row.amount >= 0 ? '+' : '' }}{{ scope.row.amount }} {{ scope.row.currency }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动后余额" prop="balanceAfter" width="150" align="right">
          <template #default="scope">
            <span>{{ scope.row.balanceAfter }} {{ scope.row.currency }}</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="MerchantWallet">
import { getWallet, listWalletLog } from "@/api/user"

const { proxy } = getCurrentInstance()

const wallet = ref({})
const logList = ref([])
const loading = ref(true)
const total = ref(0)
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    changeType: undefined
  }
})
const { queryParams } = toRefs(data)

function loadWallet() {
  getWallet().then(response => {
    wallet.value = response.data || {}
  })
}

function getList() {
  loading.value = true
  listWalletLog(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    logList.value = response.rows
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
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

onMounted(() => {
  loadWallet()
  getList()
})
</script>

<style scoped>
.wallet-cards {
  margin-bottom: 16px;
}
.balance-card {
  text-align: center;
  margin-bottom: 16px;
}
.balance-label {
  font-size: 14px;
  color: #909399;
}
.balance-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 12px 0;
}
.balance-footer {
  font-size: 12px;
  color: #c0c4cc;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
