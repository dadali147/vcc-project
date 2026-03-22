<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="文件类型" prop="fileType">
        <el-select v-model="queryParams.fileType" placeholder="全部" clearable style="width: 140px">
          <el-option label="交易账单" value="TRANSACTION" />
          <el-option label="对账单" value="STATEMENT" />
          <el-option label="结算单" value="SETTLEMENT" />
          <el-option label="操作日志" value="OPERATION_LOG" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期范围" prop="dateRange">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />

    <el-table :data="list" v-loading="loading">
      <el-table-column label="文件名称" prop="fileName" show-overflow-tooltip />
      <el-table-column label="文件类型" width="120" align="center">
        <template #default="scope">
          <el-tag size="small">{{ fileTypeLabel(scope.row.fileType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" prop="fileSize" width="120" align="right">
        <template #default="scope">
          {{ formatSize(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="生成时间" prop="createTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="过期时间" prop="expireTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.expireTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Download" @click="handleDownload(scope.row)">下载</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup>
import { ref } from 'vue'

const loading = ref(false)
const showSearch = ref(true)
const list = ref([])
const total = ref(0)
const dateRange = ref([])

const queryParams = ref({ pageNum: 1, pageSize: 10, fileType: '' })

const fileTypeLabels = {
  TRANSACTION: '交易账单',
  STATEMENT: '对账单',
  SETTLEMENT: '结算单',
  OPERATION_LOG: '操作日志'
}

function fileTypeLabel(t) { return fileTypeLabels[t] || t }

function formatSize(bytes) {
  if (!bytes) return '—'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function getList() {
  loading.value = true
  // TODO: 调用下载中心列表接口
  // listDownloads(queryParams.value).then(res => { ... })
  list.value = []
  total.value = 0
  loading.value = false
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value = { pageNum: 1, pageSize: 10, fileType: '' }; dateRange.value = []; getList() }

function handleDownload(row) {
  // TODO: 调用下载接口
  window.open(row.downloadUrl || '#', '_blank')
}

getList()
</script>
