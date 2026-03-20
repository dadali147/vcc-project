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
      <el-table-column prop="cardNumber" label="卡号" width="200" />
      <el-table-column prop="cardholderName" label="持卡人" />
      <el-table-column prop="cardType" label="卡类型">
        <template #default="scope">
          {{ scope.row.cardType === 'VIRTUAL' ? '虚拟卡' : '实体卡' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'ACTIVE'" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.status === 'FROZEN'" type="warning">冻结</el-tag>
          <el-tag v-else type="info">已销卡</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="balance" label="余额" />
      <el-table-column prop="createdAt" label="创建时间" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">查看</el-button>
          <el-button v-if="scope.row.status === 'ACTIVE'" size="small" type="warning" @click="handleFreeze(scope.row)">冻结</el-button>
          <el-button v-if="scope.row.status === 'FROZEN'" size="small" type="success" @click="handleUnfreeze(scope.row)">解冻</el-button>
          <el-button v-if="scope.row.status !== 'CANCELLED'" size="small" type="danger" @click="handleCancel(scope.row)">销卡</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container" style="margin-top: 20px" v-if="total > 0">
      <el-pagination background layout="prev, pager, next" :total="total" v-model:current-page="listQuery.page" :page-size="listQuery.limit" @current-change="getList" />
    </div>

    <el-dialog v-model="dialogVisible" title="卡片详情" width="600px">
      <el-descriptions v-if="currentCard" :column="2" border>
        <el-descriptions-item label="卡号">{{ currentCard.cardNumber }}</el-descriptions-item>
        <el-descriptions-item label="持卡人">{{ currentCard.cardholderName }}</el-descriptions-item>
        <el-descriptions-item label="卡类型">{{ currentCard.cardType === 'VIRTUAL' ? '虚拟卡' : '实体卡' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentCard.status }}</el-descriptions-item>
        <el-descriptions-item label="余额">{{ currentCard.balance }}</el-descriptions-item>
        <el-descriptions-item label="有效期">{{ currentCard.expiryDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="CVV">{{ currentCard.cvv || '***' }}</el-descriptions-item>
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
    const res = await client.get('/admin/cards', { params: listQuery.value })
    list.value = res.data?.items || []
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
    await ElMessageBox.confirm('确认冻结该卡片？', '提示', { type: 'warning' })
    await client.put(`/admin/cards/${row.id}/freeze`)
    ElMessage.success('冻结成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('冻结失败')
  }
}

const handleUnfreeze = async (row) => {
  try {
    await ElMessageBox.confirm('确认解冻该卡片？', '提示', { type: 'warning' })
    await client.put(`/admin/cards/${row.id}/unfreeze`)
    ElMessage.success('解冻成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('解冻失败')
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确认销卡？此操作不可恢复！', '警告', { type: 'error' })
    await client.delete(`/admin/cards/${row.id}`)
    ElMessage.success('销卡成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('销卡失败')
  }
}

onMounted(getList)
</script>
