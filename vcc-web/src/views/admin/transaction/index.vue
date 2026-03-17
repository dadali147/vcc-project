<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :sm="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-label">今日交易笔数</div>
            <div class="stat-value">{{ transStats.todayCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :sm="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-label">今日交易金额</div>
            <div class="stat-value">{{ transStats.todayAmount || '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :sm="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-label">成功笔数</div>
            <div class="stat-value text-success">{{ transStats.successCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :sm="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-label">失败笔数</div>
            <div class="stat-value text-danger">{{ transStats.failCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="用户名" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户名" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="卡号" prop="cardNo">
        <el-input v-model="queryParams.cardNo" placeholder="请输入卡号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="成功" value="1" />
          <el-option label="失败" value="2" />
          <el-option label="处理中" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="交易时间" prop="dateRange">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="-"
          start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" />
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
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table :data="transList" v-loading="loading">
      <el-table-column label="交易时间" prop="transTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.transTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="用户" prop="userName" width="100" />
      <el-table-column label="卡号" prop="cardNo" width="200" />
      <el-table-column label="商户名称" prop="merchantName" show-overflow-tooltip />
      <el-table-column label="交易金额" prop="amount" width="130" align="right">
        <template #default="scope">
          {{ scope.row.amount }} {{ scope.row.currency }}
        </template>
      </el-table-column>
      <el-table-column label="结算金额" prop="settleAmount" width="130" align="right">
        <template #default="scope">
          {{ scope.row.settleAmount }} USD
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="80" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '1' ? 'success' : scope.row.status === '2' ? 'danger' : 'info'" size="small">
            {{ scope.row.status === '1' ? '成功' : scope.row.status === '2' ? '失败' : '处理中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 交易详情对话框 -->
    <el-dialog title="交易详情" v-model="openDetail" width="600px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="交易ID">{{ detail.transId }}</el-descriptions-item>
        <el-descriptions-item label="交易时间">{{ parseTime(detail.transTime) }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="卡号">{{ detail.cardNo }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ detail.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="商户类别">{{ detail.mcc }}</el-descriptions-item>
        <el-descriptions-item label="交易金额">{{ detail.amount }} {{ detail.currency }}</el-descriptions-item>
        <el-descriptions-item label="结算金额">{{ detail.settleAmount }} USD</el-descriptions-item>
        <el-descriptions-item label="手续费">{{ detail.fee }} USD</el-descriptions-item>
        <el-descriptions-item label="授权码">{{ detail.authCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === '1' ? 'success' : detail.status === '2' ? 'danger' : 'info'" size="small">
            {{ detail.status === '1' ? '成功' : detail.status === '2' ? '失败' : '处理中' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="失败原因" v-if="detail.status === '2'">{{ detail.failReason }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="openDetail = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminTransaction">
import { listTransaction, getTransaction, getTransactionStats } from "@/api/admin/transaction"

const { proxy } = getCurrentInstance()

const transList = ref([])
const transStats = ref({})
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const openDetail = ref(false)
const detail = ref({})
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    cardNo: undefined,
    status: undefined
  }
})
const { queryParams } = toRefs(data)

function loadStats() {
  getTransactionStats().then(response => {
    transStats.value = response.data || {}
  })
}

function getList() {
  loading.value = true
  listTransaction(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    transList.value = response.rows
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

function handleDetail(row) {
  getTransaction(row.transId).then(response => {
    detail.value = response.data
    openDetail.value = true
  })
}

function handleExport() {
  proxy.download('/admin/transaction/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, '交易记录_' + new Date().getTime() + '.xlsx')
}

onMounted(() => {
  loadStats()
  getList()
})
</script>

<style scoped>
.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  margin-bottom: 16px;
}
.stat-item {
  text-align: center;
  padding: 8px 0;
}
.stat-label {
  font-size: 14px;
  color: #909399;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-top: 8px;
}
.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }
</style>
