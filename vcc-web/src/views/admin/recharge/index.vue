<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="用户名" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户名" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="待审核" value="0" />
          <el-option label="已到账" value="1" />
          <el-option label="已拒绝" value="2" />
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

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleManual">手动充值</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table :data="rechargeList" v-loading="loading">
      <el-table-column label="用户名" prop="userName" width="120" />
      <el-table-column label="充值金额" prop="amount" width="140" align="right">
        <template #default="scope">
          <span class="text-primary">{{ scope.row.amount }} USDT</span>
        </template>
      </el-table-column>
      <el-table-column label="到账金额" prop="receivedAmount" width="140" align="right">
        <template #default="scope">
          {{ scope.row.receivedAmount }} USD
        </template>
      </el-table-column>
      <el-table-column label="交易哈希" prop="txHash" show-overflow-tooltip />
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '1' ? 'success' : scope.row.status === '2' ? 'danger' : 'warning'" size="small">
            {{ scope.row.status === '0' ? '待审核' : scope.row.status === '1' ? '已到账' : '已拒绝' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="充值时间" prop="createTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="scope">
          <el-button link type="primary" v-if="scope.row.status === '0'" @click="handleAudit(scope.row)">审核</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 充值审核对话框 -->
    <el-dialog title="充值审核" v-model="openAudit" width="480px" append-to-body>
      <el-descriptions :column="1" border style="margin-bottom: 20px">
        <el-descriptions-item label="用户">{{ auditRow.userName }}</el-descriptions-item>
        <el-descriptions-item label="充值金额">{{ auditRow.amount }} USDT</el-descriptions-item>
        <el-descriptions-item label="交易哈希">{{ auditRow.txHash }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="auditForm" :rules="auditRules" ref="auditRef" label-width="80px">
        <el-form-item label="审核结果" prop="result">
          <el-radio-group v-model="auditForm.result">
            <el-radio value="1">通过</el-radio>
            <el-radio value="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="到账金额" v-if="auditForm.result === '1'" prop="receivedAmount">
          <el-input-number v-model="auditForm.receivedAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark" :rules="auditForm.result === '2' ? [{ required: true, message: '拒绝时备注不能为空', trigger: 'blur' }] : []">
          <el-input v-model="auditForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="openAudit = false">取 消</el-button>
        <el-button type="primary" @click="submitAudit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 手动充值对话框 -->
    <el-dialog title="手动充值" v-model="openManual" width="480px" append-to-body>
      <el-form :model="manualForm" :rules="manualRules" ref="manualRef" label-width="100px">
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="manualForm.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="充值币种" prop="currency">
          <el-radio-group v-model="manualForm.currency">
            <el-radio value="USD">USD</el-radio>
            <el-radio value="USDT">USDT</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="manualForm.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="manualForm.remark" type="textarea" placeholder="请输入充值原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="openManual = false">取 消</el-button>
        <el-button type="primary" @click="submitManual">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminRecharge">
import { listRecharge, auditRecharge, manualRecharge } from "@/api/admin/recharge"

const { proxy } = getCurrentInstance()

const rechargeList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])
const openAudit = ref(false)
const openManual = ref(false)
const auditRow = ref({})

const auditForm = reactive({
  rechargeId: undefined,
  result: '1',
  receivedAmount: undefined,
  remark: ''
})

const manualForm = reactive({
  userName: '',
  currency: 'USD',
  amount: undefined,
  remark: ''
})

const manualRules = {
  userName: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  amount: [{ required: true, message: "请输入充值金额", trigger: "blur" }],
  remark: [{ required: true, message: "请输入充值原因", trigger: "blur" }]
}

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    status: undefined
  }
})
const { queryParams } = toRefs(data)

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

function handleAudit(row) {
  auditRow.value = row
  auditForm.rechargeId = row.rechargeId
  auditForm.result = '1'
  auditForm.receivedAmount = row.amount
  auditForm.remark = ''
  openAudit.value = true
}

const auditRules = {
  result: [{ required: true, message: "请选择审核结果", trigger: "change" }]
}

function submitAudit() {
  proxy.$refs["auditRef"].validate(valid => {
    if (!valid) return
    auditRecharge(auditForm).then(() => {
      proxy.$modal.msgSuccess("审核成功")
      openAudit.value = false
      getList()
    })
  })
}

function handleManual() {
  manualForm.userName = ''
  manualForm.currency = 'USD'
  manualForm.amount = undefined
  manualForm.remark = ''
  openManual.value = true
}

function submitManual() {
  proxy.$refs["manualRef"].validate(valid => {
    if (valid) {
      manualRecharge(manualForm).then(() => {
        proxy.$modal.msgSuccess("充值成功")
        openManual.value = false
        getList()
      })
    }
  })
}

function handleExport() {
  proxy.download('/admin/recharge/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, '充值记录_' + new Date().getTime() + '.xlsx')
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.text-primary {
  color: #409eff;
  font-weight: 600;
}
</style>
