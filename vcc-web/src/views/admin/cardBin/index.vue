<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="卡BIN" prop="cardBin">
        <el-input
          v-model="queryParams.cardBin"
          placeholder="请输入卡BIN"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="卡组织" prop="cardBrand">
        <el-select v-model="queryParams.cardBrand" placeholder="卡组织" clearable style="width: 200px">
          <el-option label="Visa" value="VISA" />
          <el-option label="Mastercard" value="MASTERCARD" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 200px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增卡BIN</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="cardBinList">
      <el-table-column label="卡BIN" align="center" prop="cardBin" width="120" />
      <el-table-column label="卡BIN名称" align="center" prop="cardBinName" width="150" />
      <el-table-column label="卡组织" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.cardBrand === 'VISA'" type="primary">Visa</el-tag>
          <el-tag v-else-if="scope.row.cardBrand === 'MASTERCARD'" type="danger">Mastercard</el-tag>
          <span v-else>{{ scope.row.cardBrand }}</span>
        </template>
      </el-table-column>
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="卡类型" align="center" width="100">
        <template #default="scope">
          <span>{{ scope.row.cardType === 1 ? '储值卡' : '预算卡' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="开卡费" align="center" width="100">
        <template #default="scope">
          <span>${{ scope.row.openCardFee }}</span>
        </template>
      </el-table-column>
      <el-table-column label="月费" align="center" width="100">
        <template #default="scope">
          <span>${{ scope.row.monthlyFee }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="primary" icon="Setting" @click="handleFeeConfig(scope.row)">费率</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 添加/修改卡BIN对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="cardBinRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="卡BIN" prop="cardBin">
          <el-input v-model="form.cardBin" placeholder="请输入卡BIN（如：424242）" maxlength="8" />
        </el-form-item>
        <el-form-item label="卡BIN名称" prop="cardBinName">
          <el-input v-model="form.cardBinName" placeholder="请输入卡BIN名称" />
        </el-form-item>
        <el-form-item label="卡组织" prop="cardBrand">
          <el-radio-group v-model="form.cardBrand">
            <el-radio label="VISA">Visa</el-radio>
            <el-radio label="MASTERCARD">Mastercard</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-select v-model="form.currency" placeholder="请选择币种" style="width: 100%">
            <el-option label="USD" value="USD" />
            <el-option label="EUR" value="EUR" />
            <el-option label="GBP" value="GBP" />
          </el-select>
        </el-form-item>
        <el-form-item label="卡类型" prop="cardType">
          <el-radio-group v-model="form.cardType">
            <el-radio :label="1">储值卡</el-radio>
            <el-radio :label="2">预算卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开卡费" prop="openCardFee">
          <el-input-number v-model="form.openCardFee" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="月费" prop="monthlyFee">
          <el-input-number v-model="form.monthlyFee" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 费率配置对话框 -->
    <el-dialog title="卡BIN费率配置" v-model="feeOpen" width="500px" append-to-body>
      <el-form ref="feeRef" :model="feeForm" label-width="120px">
        <el-form-item label="卡BIN">
          <el-input v-model="feeForm.cardBin" disabled />
        </el-form-item>
        <el-form-item label="充值费率" prop="rechargeRate">
          <el-input-number v-model="feeForm.rechargeRate" :min="0" :max="1" :precision="4" style="width: 100%">
            <template #append>%</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="交易手续费" prop="transactionFee">
          <el-input-number v-model="feeForm.transactionFee" :min="0" :precision="2" style="width: 100%">
            <template #append>USD</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="最低手续费" prop="minFee">
          <el-input-number v-model="feeForm.minFee" :min="0" :precision="2" style="width: 100%">
            <template #append>USD</template>
          </el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFeeForm">确 定</el-button>
          <el-button @click="feeOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CardBin">
import { listCardBin, addCardBin, updateCardBin, delCardBin, changeCardBinStatus } from '@/api/admin/cardBin'

const { proxy } = getCurrentInstance()

const cardBinList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref('')
const open = ref(false)
const feeOpen = ref(false)
const isEdit = ref(false)

const data = reactive({
  form: {},
  feeForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    cardBin: null,
    cardBrand: null,
    status: null
  },
  rules: {
    cardBin: [{ required: true, message: '请输入卡BIN', trigger: 'blur' }],
    cardBinName: [{ required: true, message: '请输入卡BIN名称', trigger: 'blur' }],
    cardBrand: [{ required: true, message: '请选择卡组织', trigger: 'change' }],
    currency: [{ required: true, message: '请选择币种', trigger: 'change' }],
    cardType: [{ required: true, message: '请选择卡类型', trigger: 'change' }]
  }
})

const { queryParams, form, feeForm, rules } = toRefs(data)

/** 查询卡BIN列表 */
function getList() {
  loading.value = true
  listCardBin(queryParams.value).then(response => {
    cardBinList.value = response.rows
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

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '新增卡BIN'
  isEdit.value = false
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  form.value = { ...row }
  open.value = true
  title.value = '修改卡BIN'
  isEdit.value = true
}

/** 费率配置 */
function handleFeeConfig(row) {
  feeForm.value = {
    cardBinId: row.cardBinId,
    cardBin: row.cardBin,
    rechargeRate: row.rechargeRate || 0.01,
    transactionFee: row.transactionFee || 0.5,
    minFee: row.minFee || 0.1
  }
  feeOpen.value = true
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs['cardBinRef'].validate(valid => {
    if (valid) {
      if (isEdit.value) {
        updateCardBin(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addCardBin(form.value).then(() => {
          proxy.$modal.msgSuccess('添加成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 提交费率配置 */
function submitFeeForm() {
  proxy.$modal.msgSuccess('费率配置保存成功')
  feeOpen.value = false
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    cardBinId: null,
    cardBin: null,
    cardBinName: null,
    cardBrand: 'VISA',
    currency: 'USD',
    cardType: 1,
    openCardFee: 0,
    monthlyFee: 0,
    status: 1,
    description: null
  }
  proxy.resetForm('cardBinRef')
}

/** 状态变更 */
function handleStatusChange(row) {
  const text = row.status === 1 ? '启用' : '禁用'
  proxy.$modal.confirm('确认要"' + text + '""' + row.cardBin + '"卡BIN吗？').then(() => {
    changeCardBinStatus(row.cardBinId, row.status).then(() => {
      proxy.$modal.msgSuccess(text + '成功')
    })
  }).catch(() => {
    row.status = row.status === 1 ? 0 : 1
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('确认删除该卡BIN吗？').then(() => {
    delCardBin(row.cardBinId).then(() => {
      proxy.$modal.msgSuccess('删除成功')
      getList()
    })
  })
}

getList()
</script>
