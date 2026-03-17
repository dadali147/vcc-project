<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 用户充值费率 -->
      <el-tab-pane label="用户充值费率" name="user">
        <el-form :model="userQueryParams" ref="userQueryRef" :inline="true" v-show="showSearch">
          <el-form-item label="用户名" prop="userName">
            <el-input v-model="userQueryParams.userName" placeholder="请输入用户名" clearable @keyup.enter="handleUserQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleUserQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetUserQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Edit" :disabled="userMultipleSelection.length === 0" @click="handleBatchEditUser">批量修改</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getUserList" />
        </el-row>

        <el-table :data="userFeeList" v-loading="userLoading" @selection-change="handleUserSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="用户ID" prop="userId" width="80" align="center" />
          <el-table-column label="用户名" prop="userName" min-width="120" />
          <el-table-column label="充值费率(%)" prop="rechargeRate" width="120" align="center" />
          <el-table-column label="开卡费(USD)" prop="openCardFee" width="120" align="center" />
          <el-table-column label="月费(USD)" prop="monthlyFee" width="120" align="center" />
          <el-table-column label="交易手续费(USD)" prop="transactionFee" width="140" align="center" />
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleEditUser(scope.row)">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="userTotal > 0" :total="userTotal" v-model:page="userQueryParams.pageNum" v-model:limit="userQueryParams.pageSize" @pagination="getUserList" />
      </el-tab-pane>

      <!-- 卡BIN费率 -->
      <el-tab-pane label="卡BIN费率" name="cardBin">
        <el-row :gutter="10" class="mb8">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getCardBinList" />
        </el-row>

        <el-table :data="cardBinFeeList" v-loading="cardBinLoading">
          <el-table-column label="卡BIN ID" prop="cardBinId" width="80" align="center" />
          <el-table-column label="卡BIN" prop="cardBin" min-width="120" />
          <el-table-column label="充值费率(%)" prop="rechargeRate" width="120" align="center" />
          <el-table-column label="开卡费(USD)" prop="openCardFee" width="120" align="center" />
          <el-table-column label="月费(USD)" prop="monthlyFee" width="120" align="center" />
          <el-table-column label="交易手续费(USD)" prop="transactionFee" width="140" align="center" />
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleEditCardBin(scope.row)">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="cardBinTotal > 0" :total="cardBinTotal" v-model:page="cardBinQueryParams.pageNum" v-model:limit="cardBinQueryParams.pageSize" @pagination="getCardBinList" />
      </el-tab-pane>
    </el-tabs>

    <!-- 用户费率编辑对话框 -->
    <el-dialog :title="editTitle" v-model="editOpen" width="500px" append-to-body>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="130px">
        <el-form-item label="充值费率(%)" prop="rechargeRate">
          <el-input-number v-model="editForm.rechargeRate" :min="0" :max="100" :precision="2" />
        </el-form-item>
        <el-form-item label="开卡费(USD)" prop="openCardFee">
          <el-input-number v-model="editForm.openCardFee" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="月费(USD)" prop="monthlyFee">
          <el-input-number v-model="editForm.monthlyFee" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="交易手续费(USD)" prop="transactionFee">
          <el-input-number v-model="editForm.transactionFee" :min="0" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitEdit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 批量修改对话框 -->
    <el-dialog title="批量修改用户费率" v-model="batchEditOpen" width="500px" append-to-body>
      <el-form ref="batchFormRef" :model="batchForm" :rules="editRules" label-width="130px">
        <el-form-item label="充值费率(%)" prop="rechargeRate">
          <el-input-number v-model="batchForm.rechargeRate" :min="0" :max="100" :precision="2" />
        </el-form-item>
        <el-form-item label="开卡费(USD)" prop="openCardFee">
          <el-input-number v-model="batchForm.openCardFee" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="月费(USD)" prop="monthlyFee">
          <el-input-number v-model="batchForm.monthlyFee" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="交易手续费(USD)" prop="transactionFee">
          <el-input-number v-model="batchForm.transactionFee" :min="0" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchEditOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitBatchEdit">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminFeeConfig">
import { listUserFeeConfig, updateUserFeeConfig, listCardBinFeeConfig, updateCardBinFeeConfig } from "@/api/admin/feeConfig"

const { proxy } = getCurrentInstance()

const activeTab = ref('user')
const showSearch = ref(true)

// 用户费率
const userFeeList = ref([])
const userLoading = ref(true)
const userTotal = ref(0)
const userMultipleSelection = ref([])
const userQueryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userName: undefined
})

// 卡BIN费率
const cardBinFeeList = ref([])
const cardBinLoading = ref(true)
const cardBinTotal = ref(0)
const cardBinQueryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

// 编辑表单
const editOpen = ref(false)
const editTitle = ref('')
const editForm = reactive({
  id: undefined,
  type: '',
  rechargeRate: 0,
  openCardFee: 0,
  monthlyFee: 0,
  transactionFee: 0
})
const batchEditOpen = ref(false)
const batchForm = reactive({
  rechargeRate: 0,
  openCardFee: 0,
  monthlyFee: 0,
  transactionFee: 0
})

const editRules = {
  rechargeRate: [{ required: true, message: '请输入充值费率', trigger: 'blur' }],
  openCardFee: [{ required: true, message: '请输入开卡费', trigger: 'blur' }],
  monthlyFee: [{ required: true, message: '请输入月费', trigger: 'blur' }],
  transactionFee: [{ required: true, message: '请输入交易手续费', trigger: 'blur' }]
}

function getUserList() {
  userLoading.value = true
  listUserFeeConfig(userQueryParams).then(response => {
    userFeeList.value = response.rows
    userTotal.value = response.total
    userLoading.value = false
  })
}

function getCardBinList() {
  cardBinLoading.value = true
  listCardBinFeeConfig(cardBinQueryParams).then(response => {
    cardBinFeeList.value = response.rows
    cardBinTotal.value = response.total
    cardBinLoading.value = false
  })
}

function handleTabClick(tab) {
  if (tab.props.name === 'user') {
    getUserList()
  } else {
    getCardBinList()
  }
}

function handleUserQuery() {
  userQueryParams.pageNum = 1
  getUserList()
}

function resetUserQuery() {
  proxy.resetForm("userQueryRef")
  handleUserQuery()
}

function handleUserSelectionChange(selection) {
  userMultipleSelection.value = selection
}

function handleEditUser(row) {
  editTitle.value = '修改用户费率 - ' + row.userName
  editForm.id = row.userId
  editForm.type = 'user'
  editForm.rechargeRate = row.rechargeRate
  editForm.openCardFee = row.openCardFee
  editForm.monthlyFee = row.monthlyFee
  editForm.transactionFee = row.transactionFee
  editOpen.value = true
}

function handleEditCardBin(row) {
  editTitle.value = '修改卡BIN费率 - ' + row.cardBin
  editForm.id = row.cardBinId
  editForm.type = 'cardBin'
  editForm.rechargeRate = row.rechargeRate
  editForm.openCardFee = row.openCardFee
  editForm.monthlyFee = row.monthlyFee
  editForm.transactionFee = row.transactionFee
  editOpen.value = true
}

function submitEdit() {
  proxy.$refs["editFormRef"].validate(valid => {
    if (valid) {
      const data = {
        rechargeRate: editForm.rechargeRate,
        openCardFee: editForm.openCardFee,
        monthlyFee: editForm.monthlyFee,
        transactionFee: editForm.transactionFee
      }
      const apiCall = editForm.type === 'user'
        ? updateUserFeeConfig(editForm.id, data)
        : updateCardBinFeeConfig(editForm.id, data)
      apiCall.then(() => {
        proxy.$modal.msgSuccess("修改成功")
        editOpen.value = false
        editForm.type === 'user' ? getUserList() : getCardBinList()
      })
    }
  })
}

function handleBatchEditUser() {
  batchForm.rechargeRate = 0
  batchForm.openCardFee = 0
  batchForm.monthlyFee = 0
  batchForm.transactionFee = 0
  batchEditOpen.value = true
}

function submitBatchEdit() {
  proxy.$refs["batchFormRef"].validate(valid => {
    if (valid) {
      const data = {
        rechargeRate: batchForm.rechargeRate,
        openCardFee: batchForm.openCardFee,
        monthlyFee: batchForm.monthlyFee,
        transactionFee: batchForm.transactionFee
      }
      const userIds = userMultipleSelection.value.map(item => item.userId)
      const promises = userIds.map(userId => updateUserFeeConfig(userId, data))
      Promise.all(promises).then(() => {
        proxy.$modal.msgSuccess("批量修改成功")
        batchEditOpen.value = false
        getUserList()
      })
    }
  })
}

onMounted(() => {
  getUserList()
})
</script>
