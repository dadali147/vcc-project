<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="申请单号" prop="applyNo">
        <el-input v-model="queryParams.applyNo" placeholder="请输入申请单号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="成功" value="SUCCESS" />
          <el-option label="部分成功" value="PARTIAL_SUCCESS" />
          <el-option label="失败" value="FAIL" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />

    <el-table :data="list" v-loading="loading">
      <el-table-column label="申请单号" prop="applyNo" width="200" show-overflow-tooltip />
      <el-table-column label="卡产品" prop="cardProductName" width="160" />
      <el-table-column label="持卡人" prop="holderName" width="120" />
      <el-table-column label="申请数量" prop="cardCount" width="100" align="center" />
      <el-table-column label="成功/失败" width="100" align="center">
        <template #default="scope">
          {{ scope.row.successCount || 0 }} / {{ scope.row.failCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="充值金额" prop="rechargeAmount" width="120" align="right" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)" size="small">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" prop="submitTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.submitTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="完成时间" prop="completeTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.completeTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 详情弹窗 -->
    <el-dialog title="开卡记录详情" v-model="detailVisible" width="700px">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="申请单号">{{ detail.applyNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="卡产品">{{ detail.cardProductName }}</el-descriptions-item>
        <el-descriptions-item label="持卡人">{{ detail.holderName }}</el-descriptions-item>
        <el-descriptions-item label="申请数量">{{ detail.cardCount }}</el-descriptions-item>
        <el-descriptions-item label="充值金额">{{ detail.rechargeAmount }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ detail.completeTime || '—' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 开卡明细 -->
      <h4 style="margin: 16px 0 8px">开卡明细</h4>
      <el-table :data="detail.items" border size="small">
        <el-table-column label="卡号" prop="cardNoMask" />
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="失败原因" prop="failReason" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { listIssueRequests, getIssueRequestDetail } from '@/api/merchant/cardIssue'

const loading = ref(false)
const showSearch = ref(true)
const list = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detail = ref(null)

const queryParams = ref({ pageNum: 1, pageSize: 10, applyNo: '', status: '' })

const statusMap = {
  PROCESSING: { label: '处理中', tag: '' },
  SUCCESS: { label: '成功', tag: 'success' },
  PARTIAL_SUCCESS: { label: '部分成功', tag: 'warning' },
  FAIL: { label: '失败', tag: 'danger' }
}

function statusLabel(s) { return statusMap[s]?.label || s }
function statusTag(s) { return statusMap[s]?.tag || 'info' }

function getList() {
  loading.value = true
  listIssueRequests(queryParams.value).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => loading.value = false)
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value = { pageNum: 1, pageSize: 10, applyNo: '', status: '' }; getList() }

function handleDetail(row) {
  getIssueRequestDetail(row.id).then(res => {
    detail.value = res.data
    detailVisible.value = true
  })
}

getList()
</script>
