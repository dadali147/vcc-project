<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="卡号" prop="cardNo">
        <el-input v-model="queryParams.cardNo" placeholder="请输入卡号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="交易状态" prop="transactionStatus">
        <el-select v-model="queryParams.transactionStatus" placeholder="全部" clearable style="width: 140px">
          <el-option label="已授权" value="AUTHORIZED" />
          <el-option label="已拒绝" value="DECLINED" />
          <el-option label="已清算" value="SETTLED" />
          <el-option label="已撤销" value="REVERSED" />
          <el-option label="已退款" value="REFUNDED" />
          <el-option label="争议中" value="DISPUTED" />
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
      <el-table-column label="卡号" prop="cardNo" width="200" />
      <el-table-column label="商户名称" prop="merchantName" show-overflow-tooltip />
      <el-table-column label="交易金额" prop="amount" width="140" align="right">
        <template #default="scope">
          <span>{{ scope.row.amount }} {{ scope.row.currency }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结算金额" prop="settleAmount" width="140" align="right">
        <template #default="scope">
          <span>{{ scope.row.settleAmount }} USD</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="transactionStatus" width="100" align="center">
        <template #default="scope">
          <el-tag :type="txnStatusTagType(scope.row.transactionStatus)" size="small">
            {{ txnStatusLabel(scope.row.transactionStatus) }}
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
    <el-dialog title="交易详情" v-model="openDetail" width="560px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="交易ID">{{ detail.transId }}</el-descriptions-item>
        <el-descriptions-item label="交易时间">{{ parseTime(detail.transTime) }}</el-descriptions-item>
        <el-descriptions-item label="卡号">{{ detail.cardNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="txnStatusTagType(detail.transactionStatus)" size="small">
            {{ txnStatusLabel(detail.transactionStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ detail.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="商户类别">{{ detail.mcc }}</el-descriptions-item>
        <el-descriptions-item label="交易金额">{{ detail.amount }} {{ detail.currency }}</el-descriptions-item>
        <el-descriptions-item label="结算金额">{{ detail.settleAmount }} USD</el-descriptions-item>
        <el-descriptions-item label="手续费">{{ detail.fee }} USD</el-descriptions-item>
        <el-descriptions-item label="授权码">{{ detail.authCode }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="openDetail = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MerchantTransaction">
import { listTransaction, getTransaction } from "@/api/transaction"

const { proxy } = getCurrentInstance()

const transList = ref([])
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
    cardNo: undefined,
    transactionStatus: undefined
  }
})
const { queryParams } = toRefs(data)

/** transactionStatus 枚举文案映射 - 对齐状态字典 01.3 */
const txnStatusMap = {
  AUTHORIZED: { label: '已授权', type: 'warning' },
  DECLINED: { label: '已拒绝', type: 'danger' },
  SETTLED: { label: '已清算', type: 'success' },
  REVERSED: { label: '已撤销', type: 'info' },
  REFUNDED: { label: '已退款', type: 'info' },
  DISPUTED: { label: '争议中', type: 'danger' }
}
function txnStatusLabel(status) { return txnStatusMap[status]?.label || status }
function txnStatusTagType(status) { return txnStatusMap[status]?.type || 'info' }

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
  proxy.download('/merchant/transaction/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, '交易记录_' + new Date().getTime() + '.xlsx')
}

onMounted(() => {
  getList()
})
</script>
