<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="用户" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户名" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="卡号" prop="cardNo">
        <el-input v-model="queryParams.cardNo" placeholder="请输入卡号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="卡BIN" prop="cardBinId">
        <el-select v-model="queryParams.cardBinId" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="item in cardBinList" :key="item.binId" :label="item.binName" :value="item.binId" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="正常" value="0" />
          <el-option label="冻结" value="1" />
          <el-option label="已注销" value="2" />
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
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table :data="cardList" v-loading="loading">
      <el-table-column label="所属用户" prop="userName" width="120" />
      <el-table-column label="卡号" prop="cardNo" width="200" />
      <el-table-column label="卡BIN" prop="cardBinName" width="120" />
      <el-table-column label="持卡人" prop="holderName" width="100" />
      <el-table-column label="卡类型" prop="cardType" width="80">
        <template #default="scope">
          <el-tag size="small">{{ scope.row.cardType === '1' ? '储值卡' : '预算卡' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="余额" prop="balance" width="120" align="right">
        <template #default="scope">
          {{ scope.row.balance }} {{ scope.row.currency }}
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
      <el-table-column label="操作" width="230" align="center">
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

    <!-- 卡片详情对话框 -->
    <el-dialog title="卡片详情" v-model="openDetail" width="560px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="卡号">{{ detail.cardNo }}</el-descriptions-item>
        <el-descriptions-item label="所属用户">{{ detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="持卡人">{{ detail.holderName }}</el-descriptions-item>
        <el-descriptions-item label="卡BIN">{{ detail.cardBinName }}</el-descriptions-item>
        <el-descriptions-item label="卡类型">{{ detail.cardType === '1' ? '储值卡' : '预算卡' }}</el-descriptions-item>
        <el-descriptions-item label="余额">{{ detail.balance }} {{ detail.currency }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === '0' ? 'success' : detail.status === '1' ? 'warning' : 'info'" size="small">
            {{ detail.status === '0' ? '正常' : detail.status === '1' ? '冻结' : '已注销' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开卡时间">{{ parseTime(detail.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="openDetail = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminCard">
import { listCard, getCard, freezeCard, unfreezeCard, cancelCard, exportCard } from "@/api/admin/card"
import { listCardBin } from "@/api/admin/cardBin"

const { proxy } = getCurrentInstance()

const cardList = ref([])
const cardBinList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const openDetail = ref(false)
const detail = ref({})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    cardNo: undefined,
    cardBinId: undefined,
    status: undefined
  }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listCard(queryParams.value).then(response => {
    cardList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function loadCardBins() {
  listCardBin({ pageNum: 1, pageSize: 999 }).then(response => {
    cardBinList.value = response.rows || []
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

function handleDetail(row) {
  getCard(row.cardId).then(response => {
    detail.value = response.data
    openDetail.value = true
  })
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
  proxy.$modal.confirm('确认注销卡片 "' + row.cardNo + '" ?').then(() => {
    return cancelCard(row.cardId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("注销成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download('/admin/card/export', { ...queryParams.value }, '卡片列表_' + new Date().getTime() + '.xlsx')
}

onMounted(() => {
  getList()
  loadCardBins()
})
</script>
