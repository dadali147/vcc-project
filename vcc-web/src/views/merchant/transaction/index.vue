<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="卡号" prop="cardNo">
        <el-input
          v-model="queryParams.cardNo"
          placeholder="请输入卡号后四位"
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
        <el-select v-model="queryParams.status" placeholder="交易状态" clearable style="width: 200px">
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
          <span :class="scope.row.txnType === 'AUTH' ? 'text-danger' : 'text-success'">
            {{ scope.row.txnType === 'AUTH' ? '-' : '+' }}{{ scope.row.amount }} {{ scope.row.currency }}
          </span>
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

    <!-- 交易详情对话框 -->
    <el-dialog title="交易详情" v-model="open" width="600px" append-to-body>
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="交易ID" :span="2">{{ detail.txnId }}</el-descriptions-item>
        <el-descriptions-item label="卡号">{{ detail.cardNoMask }}</el-descriptions-item>
        <el-descriptions-item label="交易类型">{{ detail.txnType }}</el-descriptions-item>
        <el-descriptions-item label="交易金额">{{ detail.amount }} {{ detail.currency }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ detail.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="商户MCC">{{ detail.merchantMcc }}</el-descriptions-item>
        <el-descriptions-item label="商户国家">{{ detail.merchantCountry }}</el-descriptions-item>
        <el-descriptions-item label="授权码">{{ detail.authCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status === 1 ? '成功' : '失败' }}</el-descriptions-item>
        <el-descriptions-item label="失败原因" :span="2" v-if="detail.failReason">{{ detail.failReason }}</el-descriptions-item>
        <el-descriptions-item label="交易时间">{{ parseTime(detail.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="Transaction">
import { listTransaction, getTransaction } from '@/api/transaction'

const { proxy } = getCurrentInstance()

const transactionList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const detail = ref(null)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    cardNo: null,
    txnType: null,
    status: null
  }
})

const { queryParams } = toRefs(data)

/** 查询交易记录 */
function getList() {
  loading.value = true
  listTransaction(queryParams.value).then(response => {
    transactionList.value = response.rows
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
  getTransaction(row.id).then(response => {
    detail.value = response.data
    open.value = true
  })
}

/** 导出按钮 */
function handleExport() {
  proxy.$modal.msg('导出功能开发中')
}

getList()
</script>

<style scoped>
.text-danger {
  color: #F56C6C;
}
.text-success {
  color: #67C23A;
}
</style>
