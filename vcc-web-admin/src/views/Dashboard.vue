<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-title">总用户数</div>
          <div class="card-value">{{ stats.totalUsers }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-title">总卡片数</div>
          <div class="card-value">{{ stats.totalCards }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-title">今日交易额(USD)</div>
          <div class="card-value">${{ stats.todayVolume }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-title">待审核KYC数</div>
          <div class="card-value text-danger">{{ stats.pendingKyc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mt-20">
      <template #header>
        <div>近7天数据</div>
      </template>
      <div v-for="item in recentData" :key="item.date" class="recent-list-item">
        <span>{{ item.date }}</span>
        <span>新增用户: {{ item.newUsers }}</span>
        <span>交易额: ${{ item.volume }}</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import client from '@/api/client'

const stats = ref({
  totalUsers: 0,
  totalCards: 0,
  todayVolume: 0,
  pendingKyc: 0
})
const recentData = ref([])

onMounted(async () => {
  try {
    const res = await client.get('/admin/dashboard/stats')
    if (res.data) {
      stats.value = res.data.stats || stats.value
      recentData.value = res.data.recentData || []
    }
  } catch(e) { /* ignore */ }
})
</script>

<style scoped>
.mt-20 { margin-top: 20px; }
.card-title { color: #909399; font-size: 14px; }
.card-value { font-size: 24px; font-weight: bold; margin-top: 10px; }
.text-danger { color: #F56C6C; }
.recent-list-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}
</style>
