<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">今日交易笔数</div>
          <div class="stat-value">{{ stats.todayCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">今日交易金额</div>
          <div class="stat-value">${{ stats.todayAmount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">成功笔数</div>
          <div class="stat-value text-success">{{ stats.successCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">失败笔数</div>
          <div class="stat-value text-danger">{{ stats.failCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px" class="search-form">
      <el-form-item label="交易ID" prop="txnId">
        <el-input
          v-model="queryParams.txnId"
          placeholder="请输入交易ID"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户名" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户名"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="卡号" prop="cardNo">
        <el-input
          v-model="queryParams.cardNo"
          placeholder="请输入卡号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="交易类型" prop="txnType">
        <el-select v-model="queryParams.txnType" placeholder="交易类型" clearable style="width: 200px">
          <el-option label="消费" value="AUTH" />
          <el-option label="退款" value="REFUND" />
          <el-option label="冲正" value="REVERSE" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 200px">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="transactionList">
      <el-table-column label="交易ID" align="center" prop="txnId" width="180" />
      <el-table-column label="用户名" align="center" prop="userName" width="120" />
      <el-table-column label="卡号" align="center" prop="cardNoMask" width="160" />
      <el-table-column label="交易类型" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.txnType === 'AUTH'" type="primary">消费</el-tag>
          <el-tag v-else-if="scope.row.txnType === 'REFUND'" type="success">退款</el-tag>
          <el-tag v-else-if="scope.row.txnType === 'REVERSE'" type="warning">冲正</el-tag>
          <el-tag v-else>{{ scope.row.txnType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="交易金额" align="center" width="120">
        <template #default="scope">
          <span>{{ scope.row.amount }} {{ scope.row.currency }}</span>
        </template>
      </el-table-column>
      <el-table-column label="商户名称" align="center" prop="merchantName" width="150" />
      <el-table-column label="商户国家" align="center" prop="merchantCountry" width="100" />
      <el-table-column label="状态" align="center" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success">成功</el-tag>
          <el-tag v-else type="danger">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="交易时间" align="center" prop="createdAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="100">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog title="交易详情" v-model="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border v-if="currentTransaction">
        <el-descriptions-item label="交易ID" :span="2">{{ currentTransaction.txnId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentTransaction.userName }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentTransaction.userId }}</el-descriptions-item>
        <el-descriptions-item label="卡号">{{ currentTransaction.cardNoMask }}</el-descriptions-item>
        <el-descriptions-item label="卡片ID">{{ currentTransaction.cardId }}</el-descriptions-item>
        <el-descriptions-item label="交易类型">{{ currentTransaction.txnType }}</el-descriptions-item>
        <el-descriptions-item label="交易金额">{{ currentTransaction.amount }} {{ currentTransaction.currency }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ currentTransaction.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="商户MCC">{{ currentTransaction.merchantMcc }}</el-descriptions-item>
        <el-descriptions-item label="商户国家">{{ currentTransaction.merchantCountry }}</el-descriptions-item>
        <el-descriptions-item label="授权码">{{ currentTransaction.authCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentTransaction.status === 1" type="success">成功</el-tag>
          <el-tag v-else type="danger">失败</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="失败原因" :span="2" v-if="currentTransaction.failReason">{{ currentTransaction.failReason }}</el-descriptions-item>
        <el-descriptions-item label="交易时间" :span="2">{{ parseTime(currentTransaction.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="TransactionAdmin">
import { listTransactionAdmin, getTransactionAdmin, getTransactionStats, exportTransaction } from '@/api/admin/transaction'

const { proxy } = getCurrentInstance()

const transactionList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const detailOpen = ref(false)
const currentTransaction = ref(null)

const stats = reactive({
  todayCount: 0,
  todayAmount: '0.00',
  successCount: 0,
  failCount: 0
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  txnId: null,
  userName: null,
  cardNo: null,
  txnType: null,
  status: null
})

/** 查询交易列表 */
function getList() {
  loading.value = true
  listTransactionAdmin(queryParams).then(response => {
    transactionList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 获取统计数据 */
function getStats() {
  getTransactionStats().then(response => {
    const data = response.data || {}
    stats.todayCount = data.todayCount || 0
    stats.todayAmount = data.todayAmount || '0.00'
    stats.successCount = data.successCount || 0
    stats.failCount = data.failCount || 0
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 查看详情 */
function handleView(row) {
  getTransactionAdmin(row.id).then(response => {
    currentTransaction.value = response.data
    detailOpen.value = true
  })
}

/** 导出 */
function handleExport() {
  proxy.$modal.confirm('确认导出所有交易数据吗？').then(() => {
    exportTransaction(queryParams).then(response => {
      proxy.download(response, 'transaction_' + new Date().getTime() + '.xlsx')
    })
  })
}

getList()
getStats()
</script>

<style scoped>
.stats-row {
  margin-bottom: 20px;
}
.stat-card {
  text-align: center;
}
.stat-title {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #262626;
}
.text-success {
  color: #52c41a;
}
.text-danger {
  color: #ff4d4f;
}
.search-form {
  margin-top: 20px;
}
</style>
