<template>
  <div class="app-container">
    <!-- USDT充值地址 -->
    <el-card shadow="hover" class="recharge-card">
      <template #header>
        <span>USDT 充值</span>
      </template>
      <el-row :gutter="20">
        <el-col :sm="12" :xs="24">
          <div class="address-section">
            <div class="address-label">充值网络：<el-tag size="small">TRC20</el-tag></div>
            <div class="address-box">
              <el-input v-model="rechargeAddress" readonly>
                <template #append>
                  <el-button icon="CopyDocument" @click="copyAddress">复制</el-button>
                </template>
              </el-input>
            </div>
            <div class="address-tips">
              <el-icon color="#e6a23c"><WarningFilled /></el-icon>
              <span>请仅向此地址发送 USDT (TRC20)，发送其他币种可能导致资产丢失</span>
            </div>
          </div>
        </el-col>
        <el-col :sm="12" :xs="24">
          <div class="qrcode-section">
            <el-icon :size="120" color="#dcdfe6"><Picture /></el-icon>
            <div class="qrcode-label">扫码充值</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 充值记录 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <template #header>
        <span>充值记录</span>
      </template>

      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 160px">
            <el-option label="待确认" value="0" />
            <el-option label="已到账" value="1" />
            <el-option label="已失败" value="2" />
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

      <el-table :data="rechargeList" v-loading="loading">
        <el-table-column label="充值时间" prop="createTime" width="170">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="充值金额" prop="amount" width="150" align="right">
          <template #default="scope">
            <span class="text-primary">{{ scope.row.amount }} USDT</span>
          </template>
        </el-table-column>
        <el-table-column label="到账金额 (USD)" prop="receivedAmount" width="150" align="right" />
        <el-table-column label="交易哈希" prop="txHash" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === '1' ? 'success' : scope.row.status === '2' ? 'danger' : 'warning'" size="small">
              {{ scope.row.status === '0' ? '待确认' : scope.row.status === '1' ? '已到账' : '已失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="确认时间" prop="confirmTime" width="170">
          <template #default="scope">
            <span>{{ scope.row.confirmTime ? parseTime(scope.row.confirmTime) : '-' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="MerchantRecharge">
import { listRecharge, getRechargeAddress } from "@/api/recharge"

const { proxy } = getCurrentInstance()

const rechargeAddress = ref("")
const rechargeList = ref([])
const loading = ref(true)
const total = ref(0)
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    status: undefined
  }
})
const { queryParams } = toRefs(data)

function loadAddress() {
  getRechargeAddress().then(response => {
    rechargeAddress.value = response.data?.address || ''
  })
}

function copyAddress() {
  if (!rechargeAddress.value) return
  navigator.clipboard.writeText(rechargeAddress.value).then(() => {
    proxy.$modal.msgSuccess("复制成功")
  }).catch(() => {
    proxy.$modal.msgError("复制失败")
  })
}

function getList() {
  loading.value = true
  listRecharge(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    rechargeList.value = response.rows
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
  loadAddress()
  getList()
})
</script>

<style scoped>
.recharge-card {
  margin-bottom: 16px;
}
.address-section {
  padding: 10px 0;
}
.address-label {
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}
.address-box {
  margin-bottom: 12px;
}
.address-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #e6a23c;
}
.qrcode-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.qrcode-label {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
.text-primary {
  color: #409eff;
  font-weight: 600;
}
</style>
