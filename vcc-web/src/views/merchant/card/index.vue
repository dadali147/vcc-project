<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="卡号" prop="cardNoMask">
        <el-input
          v-model="queryParams.cardNoMask"
          placeholder="请输入卡号后四位"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="卡片状态" clearable style="width: 200px">
          <el-option label="正常" :value="1" />
          <el-option label="冻结" :value="2" />
          <el-option label="已注销" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="卡类型" prop="cardType">
        <el-select v-model="queryParams.cardType" placeholder="卡类型" clearable style="width: 200px">
          <el-option label="储值卡" :value="1" />
          <el-option label="预算卡" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleOpenCard">开卡</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="cardList">
      <el-table-column label="卡号" align="center" prop="cardNoMask" width="180" />
      <el-table-column label="持卡人" align="center" prop="holderName" width="120" />
      <el-table-column label="卡类型" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.cardType === 1" type="success">储值卡</el-tag>
          <el-tag v-else type="warning">预算卡</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="余额/额度" align="center" width="120">
        <template #default="scope">
          <span v-if="scope.row.cardType === 1">{{ scope.row.balance }}</span>
          <span v-else>{{ scope.row.budgetAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="warning">冻结</el-tag>
          <el-tag v-else type="danger">已注销</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Key" @click="handleViewKeyInfo(scope.row)">查看卡密</el-button>
          <el-button v-if="scope.row.status === 1" link type="warning" icon="Lock" @click="handleFreeze(scope.row)">冻结</el-button>
          <el-button v-if="scope.row.status === 2" link type="success" icon="Unlock" @click="handleUnfreeze(scope.row)">解冻</el-button>
          <el-button v-if="scope.row.status !== 3" link type="danger" icon="Delete" @click="handleCancel(scope.row)">注销</el-button>
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

    <!-- 开卡对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="cardRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="选择持卡人" prop="holderId">
          <el-select v-model="form.holderId" placeholder="请选择持卡人" style="width: 100%">
            <el-option
              v-for="item in holderList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="卡BIN" prop="cardBinId">
          <el-select v-model="form.cardBinId" placeholder="请选择卡BIN" style="width: 100%">
            <el-option
              v-for="item in cardBinList"
              :key="item.cardBinId"
              :label="item.cardBinName"
              :value="item.cardBinId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="卡类型" prop="cardType">
          <el-radio-group v-model="form.cardType">
            <el-radio :label="1">储值卡</el-radio>
            <el-radio :label="2">预算卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-select v-model="form.currency" placeholder="请选择币种" style="width: 100%">
            <el-option label="USD" value="USD" />
            <el-option label="EUR" value="EUR" />
            <el-option label="GBP" value="GBP" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.cardType === 1" label="充值金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.cardType === 2" label="预算额度" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 卡密查看对话框 -->
    <el-dialog title="查看卡密" v-model="keyInfoOpen" width="500px" append-to-body>
      <el-descriptions :column="1" border v-if="keyInfo">
        <el-descriptions-item label="卡号">{{ keyInfo.cardNumber }}</el-descriptions-item>
        <el-descriptions-item label="CVV">{{ keyInfo.cvv }}</el-descriptions-item>
        <el-descriptions-item label="有效期">{{ keyInfo.expiryDate }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="keyInfoOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Card">
import { listCard, openCard, getCardKeyInfo, freezeCard, unfreezeCard, cancelCard, listCardBin } from '@/api/card'
import { listCardHolder } from '@/api/cardHolder'

const { proxy } = getCurrentInstance()

const cardList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref('')
const open = ref(false)
const keyInfoOpen = ref(false)
const keyInfo = ref(null)
const holderList = ref([])
const cardBinList = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    cardNoMask: null,
    status: null,
    cardType: null
  },
  rules: {
    holderId: [{ required: true, message: '请选择持卡人', trigger: 'change' }],
    cardBinId: [{ required: true, message: '请选择卡BIN', trigger: 'change' }],
    cardType: [{ required: true, message: '请选择卡类型', trigger: 'change' }],
    currency: [{ required: true, message: '请选择币种', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询卡片列表 */
function getList() {
  loading.value = true
  listCard(queryParams.value).then(response => {
    cardList.value = response.rows
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

/** 开卡按钮操作 */
function handleOpenCard() {
  reset()
  open.value = true
  title.value = '申请开卡'
  // 加载持卡人和卡BIN列表
  loadHolderList()
  loadCardBinList()
}

/** 加载持卡人列表 */
function loadHolderList() {
  listCardHolder({ pageNum: 1, pageSize: 100 }).then(response => {
    holderList.value = response.rows
  })
}

/** 加载卡BIN列表 */
function loadCardBinList() {
  listCardBin({ pageNum: 1, pageSize: 100 }).then(response => {
    cardBinList.value = response.rows
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs['cardRef'].validate(valid => {
    if (valid) {
      openCard(form.value).then(response => {
        proxy.$modal.msgSuccess('开卡申请已提交，请稍后查询结果')
        open.value = false
        getList()
      })
    }
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    holderId: null,
    cardBinId: null,
    cardType: 1,
    currency: 'USD',
    amount: null
  }
  proxy.resetForm('cardRef')
}

/** 查看详情 */
function handleView(row) {
  proxy.$modal.msg('查看详情功能开发中')
}

/** 查看卡密 */
function handleViewKeyInfo(row) {
  getCardKeyInfo(row.id).then(response => {
    keyInfo.value = response.data
    keyInfoOpen.value = true
  })
}

/** 冻结卡片 */
function handleFreeze(row) {
  proxy.$modal.confirm('确认冻结该卡片吗？').then(() => {
    freezeCard(row.id).then(() => {
      proxy.$modal.msgSuccess('冻结成功')
      getList()
    })
  })
}

/** 解冻卡片 */
function handleUnfreeze(row) {
  proxy.$modal.confirm('确认解冻该卡片吗？').then(() => {
    unfreezeCard(row.id).then(() => {
      proxy.$modal.msgSuccess('解冻成功')
      getList()
    })
  })
}

/** 注销卡片 */
function handleCancel(row) {
  proxy.$modal.confirm('确认注销该卡片吗？注销后不可恢复！').then(() => {
    cancelCard(row.id).then(() => {
      proxy.$modal.msgSuccess('注销成功')
      getList()
    })
  })
}

getList()
</script>
