<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="卡号" prop="cardNoMask">
        <el-input
          v-model="queryParams.cardNoMask"
          placeholder="请输入卡号"
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
      <el-form-item label="卡BIN" prop="cardBinId">
        <el-select v-model="queryParams.cardBinId" placeholder="卡BIN" clearable style="width: 200px">
          <el-option
            v-for="item in cardBinList"
            :key="item.cardBinId"
            :label="item.cardBinName"
            :value="item.cardBinId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 200px">
          <el-option label="正常" :value="1" />
          <el-option label="冻结" :value="2" />
          <el-option label="已注销" :value="3" />
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

    <el-table v-loading="loading" :data="cardList">
      <el-table-column label="卡片ID" align="center" prop="id" width="80" />
      <el-table-column label="卡号" align="center" prop="cardNoMask" width="160" />
      <el-table-column label="用户名" align="center" prop="userName" width="120" />
      <el-table-column label="持卡人" align="center" prop="holderName" width="120" />
      <el-table-column label="卡BIN" align="center" prop="cardBinName" width="120" />
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
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog title="卡片详情" v-model="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border v-if="currentCard">
        <el-descriptions-item label="卡片ID">{{ currentCard.id }}</el-descriptions-item>
        <el-descriptions-item label="上游卡片ID">{{ currentCard.upstreamCardId }}</el-descriptions-item>
        <el-descriptions-item label="卡号">{{ currentCard.cardNoMask }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentCard.userName }}</el-descriptions-item>
        <el-descriptions-item label="持卡人">{{ currentCard.holderName }}</el-descriptions-item>
        <el-descriptions-item label="卡BIN">{{ currentCard.cardBinName }}</el-descriptions-item>
        <el-descriptions-item label="卡类型">{{ currentCard.cardType === 1 ? '储值卡' : '预算卡' }}</el-descriptions-item>
        <el-descriptions-item label="币种">{{ currentCard.currency }}</el-descriptions-item>
        <el-descriptions-item label="余额">{{ currentCard.balance }}</el-descriptions-item>
        <el-descriptions-item label="预算额度">{{ currentCard.budgetAmount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentCard.status === 1" type="success">正常</el-tag>
          <el-tag v-else-if="currentCard.status === 2" type="warning">冻结</el-tag>
          <el-tag v-else type="danger">已注销</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="激活时间">{{ currentCard.activatedAt ? parseTime(currentCard.activatedAt) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ parseTime(currentCard.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="CardAdmin">
import { listCardAdmin, getCardAdmin, changeCardStatus, exportCardAdmin } from '@/api/admin/card'
import { listCardBin } from '@/api/admin/cardBin'

const { proxy } = getCurrentInstance()

const cardList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const detailOpen = ref(false)
const currentCard = ref(null)
const cardBinList = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  cardNoMask: null,
  userName: null,
  cardBinId: null,
  status: null
})

/** 查询卡片列表 */
function getList() {
  loading.value = true
  listCardAdmin(queryParams.value).then(response => {
    cardList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 加载卡BIN列表 */
function loadCardBinList() {
  listCardBin({ pageNum: 1, pageSize: 100 }).then(response => {
    cardBinList.value = response.rows
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
  getCardAdmin(row.id).then(response => {
    currentCard.value = response.data
    detailOpen.value = true
  })
}

/** 冻结卡片 */
function handleFreeze(row) {
  proxy.$modal.confirm('确认冻结该卡片吗？').then(() => {
    changeCardStatus(row.id, 2).then(() => {
      proxy.$modal.msgSuccess('冻结成功')
      getList()
    })
  })
}

/** 解冻卡片 */
function handleUnfreeze(row) {
  proxy.$modal.confirm('确认解冻该卡片吗？').then(() => {
    changeCardStatus(row.id, 1).then(() => {
      proxy.$modal.msgSuccess('解冻成功')
      getList()
    })
  })
}

/** 注销卡片 */
function handleCancel(row) {
  proxy.$modal.confirm('确认注销该卡片吗？注销后不可恢复！').then(() => {
    changeCardStatus(row.id, 3).then(() => {
      proxy.$modal.msgSuccess('注销成功')
      getList()
    })
  })
}

/** 导出 */
function handleExport() {
  proxy.$modal.confirm('确认导出所有卡片数据吗？').then(() => {
    exportCardAdmin(queryParams).then(response => {
      proxy.download(response, 'cards_' + new Date().getTime() + '.xlsx')
    })
  })
}

getList()
loadCardBinList()
</script>
