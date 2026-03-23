<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="操作日志" name="operation"></el-tab-pane>
      <el-tab-pane label="登录日志" name="login"></el-tab-pane>
    </el-tabs>

    <div class="filter-container" style="margin-top: 16px;">
      <el-input v-model="listQuery.keyword" placeholder="用户ID/操作人/邮箱" style="width: 200px;" @keyup.enter="handleFilter" />
      <el-select v-if="activeTab === 'operation'" v-model="listQuery.action" placeholder="操作类型" clearable style="width: 150px; margin-left: 10px;">
        <el-option label="创建" value="CREATE" />
        <el-option label="更新" value="UPDATE" />
        <el-option label="删除" value="DELETE" />
        <el-option label="审核" value="REVIEW" />
      </el-select>
      <el-select v-if="activeTab === 'login'" v-model="listQuery.status" placeholder="登录状态" clearable style="width: 130px; margin-left: 10px;">
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAILED" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px; margin-left: 10px;" />
      <el-button type="primary" @click="handleFilter" style="margin-left: 10px;">筛选</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="操作人 ID" width="100" />
      <el-table-column prop="username" label="用户名/邮箱" min-width="150" show-overflow-tooltip />
      <template v-if="activeTab === 'operation'">
        <el-table-column prop="action" label="操作类型" width="100">
          <template #default="scope">
            <el-tag
              :type="{ CREATE: 'success', UPDATE: 'primary', DELETE: 'danger', REVIEW: 'warning' }[scope.row.action] || 'info'"
              size="small"
            >{{ scope.row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resource" label="资源" width="120" />
        <el-table-column prop="resourceId" label="资源 ID" width="100" />
        <el-table-column prop="detail" label="操作详情" min-width="160" show-overflow-tooltip />
      </template>
      <template v-if="activeTab === 'login'">
        <el-table-column prop="status" label="登录状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" label="失败原因" min-width="160" show-overflow-tooltip />
      </template>
      <el-table-column prop="ip" label="IP 地址" width="130" />
      <el-table-column prop="userAgent" label="User Agent" min-width="160" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="时间" width="160" />
    </el-table>

    <div class="pagination-container" style="margin-top: 20px" v-if="total > 0">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="listQuery.page"
        v-model:page-size="listQuery.limit"
        :page-sizes="[20, 50, 100]"
        @current-change="getList"
        @size-change="handleFilter"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'

const activeTab = ref('operation')
const loading = ref(false)
const list = ref([])
const total = ref(0)
const dateRange = ref([])
const listQuery = ref({ page: 1, limit: 20, keyword: '', action: '', status: '' })

const getList = async () => {
  loading.value = true
  try {
    const url = activeTab.value === 'operation' ? '/monitor/operlog/list' : '/monitor/logininfor/list'
    const params = { ...listQuery.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0].toISOString()
      params.endDate = dateRange.value[1].toISOString()
    }
    const res = await client.get(url, { params })
    list.value = res.rows || res.data?.items || res.data?.rows || []
    total.value = res.total || res.data?.total || 0
  } catch (e) {
    ElMessage.error('获取日志失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  listQuery.value.page = 1
  listQuery.value.keyword = ''
  listQuery.value.action = ''
  listQuery.value.status = ''
  dateRange.value = []
  getList()
}

const handleFilter = () => {
  listQuery.value.page = 1
  getList()
}

onMounted(getList)
</script>
