<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.keyword" placeholder="卡号/持卡人" style="width: 200px;" class="filter-item" @keyup.enter="handleFilter" />
      <el-select v-model="listQuery.status" placeholder="卡状态" clearable style="width: 150px; margin-left: 10px;">
        <el-option label="正常" value="ACTIVE" />
        <el-option label="冻结" value="FROZEN" />
        <el-option label="已销卡" value="CANCELLED" />
      </el-select>
      <el-select v-model="listQuery.cardType" placeholder="卡类型" clearable style="width: 150px; margin-left: 10px;">
        <el-option label="虚拟卡" value="VIRTUAL" />
        <el-option label="实体卡" value="PHYSICAL" />
      </el-select>
      <el-button class="filter-item" type="primary" @click="handleFilter" style="margin-left: 10px;">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="卡 ID" width="80" />
      <el-table-column prop="cardNumber" label="卡号" width="180" />
      <el-table-column prop="cardholderName" label="持卡人" width="110" />
      <el-table-column prop="merchantName" label="所属商户" min-width="130" show-overflow-tooltip />
      <el-table-column prop="cardType" label="卡类型" width="80">
        <template #default="scope">
          <el-tag type="primary" size="small">{{ scope.row.cardType === 'VIRTUAL' ? '虚拟卡' : '实体卡' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'ACTIVE' || scope.row.status === 1" type="success" size="small">正常</el-tag>
          <el-tag v-else-if="scope.row.status === 'FROZEN' || scope.row.status === 2" type="warning" size="small">冻结</el-tag>
          <el-tag v-else-if="scope.row.status === 'CANCELLED' || scope.row.status === 3" type="info" size="small">已销卡</el-tag>
          <el-tag v-else type="info" size="small">未激活</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="balance" label="余额" width="100">
        <template #default="scope">
          ${{ (scope.row.balance || 0).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">详情</el-button>
          <el-button
            v-if="scope.row.status === 'ACTIVE' || scope.row.status === 1"
            size="small" type="warning" @click="handleFreeze(scope.row)"
          >强制冻结</el-button>
          <el-button
            v-if="scope.row.status === 'FROZEN' || scope.row.status === 2"
            size="small" type="success" @click="handleUnfreeze(scope.row)"
          >解冻</el-button>
          <el-button
            v-if="scope.row.status !== 'CANCELLED' && scope.row.status !== 3"
            size="small" type="danger" @click="handleCancel(scope.row)"
          >强制销卡</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container" style="margin-top: 20px" v-if="total > 0">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="listQuery.page"
        v-model:page-size="listQuery.limit"
        :page-sizes="[10, 20, 50]"
        @current-change="getList"
        @size-change="handleFilter"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="卡片详情" width="660px">
      <el-descriptions v-if="currentCard" :column="2" border>
        <el-descriptions-item label="卡 ID">{{ currentCard.id }}</el-descriptions-item>
        <el-descriptions-item label="卡号">{{ currentCard.cardNumber }}</el-descriptions-item>
        <el-descriptions-item label="持卡人">{{ currentCard.cardholderName }}</el-descriptions-item>
        <el-descriptions-item label="所属商户">{{ currentCard.merchantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="卡类型">{{ currentCard.cardType === 'VIRTUAL' ? '虚拟卡' : '实体卡' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentCard.status === 'ACTIVE' || currentCard.status === 1" type="success" size="small">正常</el-tag>
          <el-tag v-else-if="currentCard.status === 'FROZEN' || currentCard.status === 2" type="warning" size="small">冻结</el-tag>
          <el-tag v-else type="info" size="small">已销卡</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="余额">${{ (currentCard.balance || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="有效期">{{ currentCard.expiryDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="CVV">***</el-descriptions-item>
        <el-descriptions-item label="币种">{{ currentCard.currency || 'USD' }}</el-descriptions-item>
        <el-descriptions-item label="卡 BIN">{{ currentCard.cardBin || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentCard.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import client from '@/api/client'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const listQuery = ref({ page: 1, limit: 20, keyword: '', status: '', cardType: '' })
const dialogVisible = ref(false)
const currentCard = ref(null)

const getList = async () => {
  loading.value = true
  try {
    const res = await client.get('/admin/card/list', { params: listQuery.value })
    list.value = res.data?.items || res.data?.rows || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('获取卡片列表失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  listQuery.value.page = 1
  getList()
}

const handleView = (row) => {
  currentCard.value = row
  dialogVisible.value = true
}

const handleFreeze = async (row) => {
  try {
    await ElMessageBox.confirm(`确认强制冻结卡片 ${row.cardNumber}？该操作将记录管理员操作日志。`, '强制冻结确认', { type: 'warning' })
    await client.put(`/admin/business/card/${row.id}/force-freeze`)
    ElMessage.success('冻结成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('冻结失败')
  }
}

const handleUnfreeze = async (row) => {
  try {
    await ElMessageBox.confirm(`确认解冻卡片 ${row.cardNumber}？`, '解冻确认', { type: 'warning' })
    await client.put(`/admin/business/card/${row.id}/force-unfreeze`)
    ElMessage.success('解冻成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('解冻失败')
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认强制销卡 ${row.cardNumber}？此操作不可恢复，销卡后无法重新激活！`,
      '强制销卡警告',
      { type: 'error', confirmButtonText: '确认销卡', cancelButtonText: '取消' }
    )
    await client.put(`/admin/business/card/${row.id}/force-cancel`)
    ElMessage.success('销卡成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('销卡失败')
  }
}

onMounted(getList)
</script>
