<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
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
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="充值状态" clearable style="width: 200px">
          <el-option label="处理中" :value="0" />
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
        <el-button type="primary" plain icon="Plus" @click="handleManualRecharge">手动充值</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="rechargeList">
      <el-table-column label="订单号" align="center" prop="orderNo" width="180" />
      <el-table-column label="用户名" align="center" prop="userName" width="120" />
      <el-table-column label="充值金额" align="center" width="120">
        <template #default="scope">
          <span>{{ scope.row.amount }} {{ scope.row.currency }}</span>
        </template>
      </el-table-column>
      <el-table-column label="手续费" align="center" width="100">
        <template #default="scope">
          <span>{{ scope.row.fee }}</span>
        </template>
      </el-table-column>
      <el-table-column label="实际到账" align="center" width="120">
        <template #default="scope">
          <span>{{ scope.row.actualAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="warning">处理中</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">成功</el-tag>
          <el-tag v-else type="danger">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="TX Hash" align="center" prop="txHash" width="200">
        <template #default="scope">
          <el-tooltip v-if="scope.row.txHash" :content="scope.row.txHash" placement="top">
            <span class="tx-hash">{{ scope.row.txHash.substring(0, 10) }}...</span>
          </el-tooltip>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status === 0" link type="success" icon="Check" @click="handleAudit(scope.row)">审核</el-button>
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

    <!-- 手动充值对话框 -->
    <el-dialog title="手动充值" v-model="manualOpen" width="500px" append-to-body>
      <el-form ref="manualRef" :model="manualForm" :rules="manualRules" label-width="100px">
        <el-form-item label="用户" prop="userId">
          <el-select v-model="manualForm.userId" placeholder="请选择用户" filterable style="width: 100%">
            <el-option
              v-for="item in userList"
              :key="item.userId"
              :label="item.userName"
              :value="item.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="manualForm.amount" :min="1" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-select v-model="manualForm.currency" placeholder="请选择币种" style="width: 100%">
            <el-option label="USD" value="USD" />
            <el-option label="USDT" value="USDT" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="manualForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitManual">确 定</el-button>
          <el-button @click="manualOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog title="充值审核" v-model="auditOpen" width="500px" append-to-body>
      <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
        <el-form-item label="订单号">
          <el-input v-model="auditForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="充值金额">
          <el-input v-model="auditForm.amount" disabled />
        </el-form-item>
        <el-form-item label="审核结果" prop="result">
          <el-radio-group v-model="auditForm.result">
            <el-radio label="approve">通过</el-radio>
            <el-radio label="reject">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注" prop="remark">
          <el-input v-model="auditForm.remark" type="textarea" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAudit">确 定</el-button>
          <el-button @click="auditOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="充值详情" v-model="detailOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border v-if="currentRecharge">
        <el-descriptions-item label="订单号" :span="2">{{ currentRecharge.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentRecharge.userName }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentRecharge.userId }}</el-descriptions-item>
        <el-descriptions-item label="充值金额">{{ currentRecharge.amount }} {{ currentRecharge.currency }}</el-descriptions-item>
        <el-descriptions-item label="手续费">{{ currentRecharge.fee }}</el-descriptions-item>
        <el-descriptions-item label="实际到账">{{ currentRecharge.actualAmount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentRecharge.status === 0" type="warning">处理中</el-tag>
          <el-tag v-else-if="currentRecharge.status === 1" type="success">成功</el-tag>
          <el-tag v-else type="danger">失败</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="TX Hash" :span="2">{{ currentRecharge.txHash || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(currentRecharge.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentRecharge.completedAt ? parseTime(currentRecharge.completedAt) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="失败原因" :span="2" v-if="currentRecharge.failReason">{{ currentRecharge.failReason }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="RechargeAdmin">
import { listRechargeAdmin, auditRecharge, manualRecharge } from '@/api/admin/recharge'
import { listUser } from '@/api/admin/user'

const { proxy } = getCurrentInstance()

const rechargeList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const manualOpen = ref(false)
const auditOpen = ref(false)
const detailOpen = ref(false)
const currentRecharge = ref(null)
const userList = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderNo: null,
    userName: null,
    status: null
  },
  manualForm: {},
  auditForm: {},
  manualRules: {
    userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
    amount: [{ required: true, message: '请输入充值金额', trigger: 'blur' }],
    currency: [{ required: true, message: '请选择币种', trigger: 'change' }]
  },
  auditRules: {
    result: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
    remark: [{ required: true, message: '请输入审核备注', trigger: 'blur' }]
  }
})

const { queryParams, manualForm, auditForm, manualRules, auditRules } = toRefs(data)

/** 查询充值列表 */
function getList() {
  loading.value = true
  listRechargeAdmin(queryParams.value).then(response => {
    rechargeList.value = response.rows
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

/** 手动充值 */
function handleManualRecharge() {
  manualForm.value = {
    userId: null,
    amount: 100,
    currency: 'USD',
    remark: ''
  }
  loadUserList()
  manualOpen.value = true
}

/** 加载用户列表 */
function loadUserList() {
  listUser({ pageNum: 1, pageSize: 100 }).then(response => {
    userList.value = response.rows
  })
}

/** 提交手动充值 */
function submitManual() {
  proxy.$refs['manualRef'].validate(valid => {
    if (valid) {
      manualRecharge(manualForm.value).then(() => {
        proxy.$modal.msgSuccess('手动充值成功')
        manualOpen.value = false
        getList()
      })
    }
  })
}

/** 审核按钮 */
function handleAudit(row) {
  auditForm.value = {
    rechargeId: row.id,
    orderNo: row.orderNo,
    amount: row.amount,
    result: 'approve',
    remark: ''
  }
  auditOpen.value = true
}

/** 提交审核 */
function submitAudit() {
  proxy.$refs['auditRef'].validate(valid => {
    if (valid) {
      auditRecharge(auditForm.value.rechargeId, {
        result: auditForm.value.result,
        remark: auditForm.value.remark
      }).then(() => {
        proxy.$modal.msgSuccess('审核完成')
        auditOpen.value = false
        getList()
      })
    }
  })
}

/** 查看详情 */
function handleView(row) {
  currentRecharge.value = row
  detailOpen.value = true
}

/** 导出 */
function handleExport() {
  proxy.$modal.msg('导出功能开发中')
}

getList()
</script>

<style scoped>
.tx-hash {
  font-family: monospace;
  color: #409EFF;
  cursor: pointer;
}
</style>
