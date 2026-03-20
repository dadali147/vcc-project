<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">总用户数</div>
          <div class="stat-value">{{ stats.totalUsers }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">总卡片数</div>
          <div class="stat-value">{{ stats.totalCards }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">今日交易额 (USD)</div>
          <div class="stat-value">${{ stats.todayVolume }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">待审核 KYC</div>
          <div class="stat-value stat-value--alert">{{ stats.pendingKyc }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="recent-card">
      <div class="recent-card-header">近 7 天数据</div>
      <div v-for="item in recentData" :key="item.date" class="recent-list-item">
        <span class="recent-date">{{ item.date }}</span>
        <span class="recent-meta">新增用户: <strong>{{ item.newUsers }}</strong></span>
        <span class="recent-meta">交易额: <strong>${{ item.volume }}</strong></span>
      </div>
      <div v-if="recentData.length === 0" class="recent-empty">暂无数据</div>
    </div>
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
.dashboard-container {
  padding: 24px;
}

.stat-card {
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  padding: 24px;
  background: #fff;
}

.stat-label {
  color: #6B7280;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #111111;
  line-height: 1;
}

.stat-value--alert {
  color: #F97316;
}

.recent-card {
  margin-top: 20px;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  background: #fff;
  overflow: hidden;
}

.recent-card-header {
  padding: 16px 24px;
  font-size: 15px;
  font-weight: 600;
  color: #111111;
  border-bottom: 1px solid #F3F4F6;
}

.recent-list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 24px;
  border-bottom: 1px solid #F9FAFB;
  font-size: 14px;
  transition: background-color 0.15s;
}

.recent-list-item:last-child {
  border-bottom: none;
}

.recent-list-item:hover {
  background-color: #F9FAFB;
}

.recent-date {
  color: #374151;
  font-weight: 500;
  min-width: 100px;
}

.recent-meta {
  color: #6B7280;
}

.recent-empty {
  padding: 24px;
  text-align: center;
  color: #9CA3AF;
  font-size: 14px;
}
</style>
