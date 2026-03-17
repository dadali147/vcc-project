<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="卡号" prop="cardNo">
        <el-input v-model="queryParams.cardNo" placeholder="请输入卡号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="卡片状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 160px">
          <el-option label="正常" value="0" />
          <el-option label="冻结" value="1" />
          <el-option label="已注销" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="卡BIN" prop="cardBinId">
        <el-select v-model="queryParams.cardBinId" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="item in cardBinList" :key="item.binId" :label="item.binName" :value="item.binId" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">开卡</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table :data="cardList" v-loading="loading">
      <el-table-column label="卡号" prop="cardNo" width="200">
        <template #default="scope">
          {{ scope.row.cardNo ? scope.row.cardNo.replace(/^(\d{4})\d{8}(\d{4})$/, '$1 **** **** $2') : '' }}
        </template>
      </el-table-column>
      <el-table-column label="卡BIN" prop="cardBinName" width="120" />
      <el-table-column label="持卡人" prop="holderName" width="120" />
      <el-table-column label="卡类型" prop="cardType" width="100">
        <template #default="scope">
          <el-tag size="small">{{ scope.row.cardType === '1' ? '储值卡' : '预算卡' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="余额" prop="balance" width="120" align="right">
        <template #default="scope">
          <span>{{ scope.row.balance }} {{ scope.row.currency }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="80" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : scope.row.status === '1' ? 'warning' : 'info'" size="small">
            {{ scope.row.status === '0' ? '正常' : scope.row.status === '1' ? '冻结' : '已注销' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开卡时间" prop="createTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" v-if="scope.row.status === '0'" @click="handleFreeze(scope.row)">冻结</el-button>
          <el-button link type="success" v-if="scope.row.status === '1'" @click="handleUnfreeze(scope.row)">解冻</el-button>
          <el-button link type="danger" v-if="scope.row.status !== '2'" @click="handleCancel(scope.row)">注销</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 开卡对话框 -->
    <el-dialog title="开卡" v-model="openAdd" width="520px" append-to-body>
      <el-form :model="form" :rules="rules" ref="cardRef" label-width="100px">
        <el-form-item label="持卡人" prop="holderId">
          <el-select v-model="form.holderId" placeholder="请选择持卡人" style="width: 100%">
            <el-option v-for="item in holderList" :key="item.holderId" :label="item.holderName" :value="item.holderId" />
          </el-select>
        </el-form-item>
        <el-form-item label="卡BIN" prop="cardBinId">
          <el-select v-model="form.cardBinId" placeholder="请选择卡BIN" style="width: 100%">
            <el-option v-for="item in cardBinList" :key="item.binId" :label="item.binName + ' (' + item.brand + ')'" :value="item.binId" />
          </el-select>
        </el-form-item>
        <el-form-item label="卡类型" prop="cardType">
          <el-radio-group v-model="form.cardType">
            <el-radio value="1">储值卡</el-radio>
            <el-radio value="2">预算卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="初始额度" prop="initialAmount">
          <el-input-number v-model="form.initialAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="openAdd = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 卡片详情对话框 -->
    <el-dialog title="卡片详情" v-model="openDetail" width="520px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="卡号">{{ detail.cardNo }}</el-descriptions-item>
        <el-descriptions-item label="CVV">
          <span v-if="showSecret">{{ detail.cvv }}</span>
          <el-button v-else link type="primary" @click="loadSecret">点击查看</el-button>
        </el-descriptions-item>
        <el-descriptions-item label="有效期">
          <span v-if="showSecret">{{ detail.expMonth }}/{{ detail.expYear }}</span>
          <span v-else>***</span>
        </el-descriptions-item>
        <el-descriptions-item label="持卡人">{{ detail.holderName }}</el-descriptions-item>
        <el-descriptions-item label="卡BIN">{{ detail.cardBinName }}</el-descriptions-item>
        <el-descriptions-item label="卡类型">{{ detail.cardType === '1' ? '储值卡' : '预算卡' }}</el-descriptions-item>
        <el-descriptions-item label="余额">{{ detail.balance }} {{ detail.currency }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === '0' ? 'success' : detail.status === '1' ? 'warning' : 'info'" size="small">
            {{ detail.status === '0' ? '正常' : detail.status === '1' ? '冻结' : '已注销' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开卡时间" :span="2">{{ parseTime(detail.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="openDetail = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MerchantCard">
import { listCard, getCard, getCardSecret, addCard, freezeCard, unfreezeCard, cancelCard, listAvailableCardBin } from "@/api/card"
import { listCardHolder } from "@/api/cardHolder"

const { proxy } = getCurrentInstance()

const cardList = ref([])
const cardBinList = ref([])
const holderList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const openAdd = ref(false)
const openDetail = ref(false)
const showSecret = ref(false)
const detail = ref({})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    cardNo: undefined,
    status: undefined,
    cardBinId: undefined
  },
  rules: {
    holderId: [{ required: true, message: "请选择持卡人", trigger: "change" }],
    cardBinId: [{ required: true, message: "请选择卡BIN", trigger: "change" }],
    cardType: [{ required: true, message: "请选择卡类型", trigger: "change" }],
    initialAmount: [{ required: true, message: "请输入初始额度", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCard(queryParams.value).then(response => {
    cardList.value = response.rows
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

function reset() {
  form.value = {
    holderId: undefined,
    cardBinId: undefined,
    cardType: '1',
    initialAmount: undefined,
    remark: undefined
  }
  proxy.resetForm("cardRef")
}

function handleAdd() {
  reset()
  loadCardBins()
  loadHolders()
  openAdd.value = true
}

function loadCardBins() {
  listAvailableCardBin().then(response => {
    cardBinList.value = response.data || []
  })
}

function loadHolders() {
  listCardHolder({ pageNum: 1, pageSize: 999 }).then(response => {
    holderList.value = response.rows || []
  })
}

function submitForm() {
  proxy.$refs["cardRef"].validate(valid => {
    if (valid) {
      addCard(form.value).then(() => {
        proxy.$modal.msgSuccess("开卡成功")
        openAdd.value = false
        getList()
      })
    }
  })
}

let secretTimer = null

function handleDetail(row) {
  showSecret.value = false
  clearTimeout(secretTimer)
  getCard(row.cardId).then(response => {
    detail.value = response.data
    openDetail.value = true
  })
}

function loadSecret() {
  proxy.$modal.confirm('查看卡密信息需要二次确认，是否继续？').then(() => {
    getCardSecret(detail.value.cardId).then(response => {
      detail.value.cvv = response.data.cvv
      detail.value.expMonth = response.data.expMonth
      detail.value.expYear = response.data.expYear
      showSecret.value = true
      clearTimeout(secretTimer)
      secretTimer = setTimeout(() => {
        detail.value.cvv = ''
        detail.value.expMonth = ''
        detail.value.expYear = ''
        showSecret.value = false
      }, 60000)
    })
  }).catch(() => {})
}

function handleFreeze(row) {
  proxy.$modal.confirm('确认冻结卡片 "' + row.cardNo + '" ?').then(() => {
    return freezeCard(row.cardId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("冻结成功")
  }).catch(() => {})
}

function handleUnfreeze(row) {
  proxy.$modal.confirm('确认解冻卡片 "' + row.cardNo + '" ?').then(() => {
    return unfreezeCard(row.cardId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("解冻成功")
  }).catch(() => {})
}

function handleCancel(row) {
  proxy.$modal.confirm('确认注销卡片 "' + row.cardNo + '" ? 此操作不可恢复！').then(() => {
    return cancelCard(row.cardId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("注销成功")
  }).catch(() => {})
}

onMounted(() => {
  getList()
  loadCardBins()
})
</script>
