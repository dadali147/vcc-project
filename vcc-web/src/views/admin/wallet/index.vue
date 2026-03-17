<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="用户名" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户名" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table :data="walletList" v-loading="loading">
      <el-table-column label="用户名" prop="userName" width="120" />
      <el-table-column label="昵称" prop="nickName" width="120" />
      <el-table-column label="USD余额" prop="usdBalance" width="150" align="right">
        <template #default="scope">
          <span style="font-weight: 600">{{ scope.row.usdBalance }}</span> USD
        </template>
      </el-table-column>
      <el-table-column label="USDT余额" prop="usdtBalance" width="150" align="right">
        <template #default="scope">
          <span style="font-weight: 600">{{ scope.row.usdtBalance }}</span> USDT
        </template>
      </el-table-column>
      <el-table-column label="冻结金额" prop="frozenBalance" width="150" align="right">
        <template #default="scope">
          {{ scope.row.frozenBalance }} USD
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="handleAdjust(scope.row)">余额调整</el-button>
          <el-button link type="primary" @click="handleLog(scope.row)">动账明细</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 余额调整对话框 -->
    <el-dialog title="余额调整" v-model="openAdjust" width="480px" append-to-body>
      <el-form :model="adjustForm" :rules="adjustRules" ref="adjustRef" label-width="100px">
        <el-form-item label="用户">
          <el-input :model-value="adjustForm.userName" disabled />
        </el-form-item>
        <el-form-item label="调整币种" prop="currency">
          <el-radio-group v-model="adjustForm.currency">
            <el-radio value="USD">USD</el-radio>
            <el-radio value="USDT">USDT</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整类型" prop="adjustType">
          <el-radio-group v-model="adjustForm.adjustType">
            <el-radio value="1">增加</el-radio>
            <el-radio value="2">扣减</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="adjustForm.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="adjustForm.remark" type="textarea" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="openAdjust = false">取 消</el-button>
        <el-button type="primary" @click="submitAdjust">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 动账明细对话框 -->
    <el-dialog title="动账明细" v-model="openLog" width="800px" append-to-body>
      <el-table :data="logList" v-loading="logLoading" size="small">
        <el-table-column label="时间" prop="createTime" width="170">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" prop="changeTypeName" width="100" />
        <el-table-column label="变动金额" prop="amount" width="140" align="right">
          <template #default="scope">
            <span :style="{ color: scope.row.amount >= 0 ? '#67c23a' : '#f56c6c' }">
              {{ scope.row.amount >= 0 ? '+' : '' }}{{ scope.row.amount }} {{ scope.row.currency }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动后余额" prop="balanceAfter" width="140" align="right" />
        <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      </el-table>
      <pagination v-show="logTotal > 0" :total="logTotal" v-model:page="logQuery.pageNum"
        v-model:limit="logQuery.pageSize" @pagination="loadLog" />
    </el-dialog>
  </div>
</template>

<script setup name="AdminWallet">
import { listWallet, adjustBalance, listWalletLog } from "@/api/admin/wallet"

const { proxy } = getCurrentInstance()

const walletList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const openAdjust = ref(false)
const openLog = ref(false)
const logList = ref([])
const logLoading = ref(false)
const logTotal = ref(0)

const logQuery = reactive({
  userId: undefined,
  pageNum: 1,
  pageSize: 10
})

const adjustForm = reactive({
  userId: undefined,
  userName: '',
  currency: 'USD',
  adjustType: '1',
  amount: undefined,
  remark: ''
})

const adjustRules = {
  currency: [{ required: true, message: "请选择币种", trigger: "change" }],
  adjustType: [{ required: true, message: "请选择调整类型", trigger: "change" }],
  amount: [{ required: true, message: "请输入金额", trigger: "blur" }],
  remark: [{ required: true, message: "请输入调整原因", trigger: "blur" }]
}

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined
  }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listWallet(queryParams.value).then(response => {
    walletList.value = response.rows
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
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleAdjust(row) {
  adjustForm.userId = row.userId
  adjustForm.userName = row.userName
  adjustForm.currency = 'USD'
  adjustForm.adjustType = '1'
  adjustForm.amount = undefined
  adjustForm.remark = ''
  openAdjust.value = true
}

function submitAdjust() {
  proxy.$refs["adjustRef"].validate(valid => {
    if (valid) {
      adjustBalance(adjustForm).then(() => {
        proxy.$modal.msgSuccess("调整成功")
        openAdjust.value = false
        getList()
      })
    }
  })
}

function handleLog(row) {
  logQuery.userId = row.userId
  logQuery.pageNum = 1
  openLog.value = true
  loadLog()
}

function loadLog() {
  logLoading.value = true
  listWalletLog(logQuery).then(response => {
    logList.value = response.rows
    logTotal.value = response.total
    logLoading.value = false
  })
}

onMounted(() => {
  getList()
})
</script>
