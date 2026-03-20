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
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  min-height: 100%;
}

.stat-card {
  border: 1px solid #E5E7EB;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  padding: 28px;
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #F97316 0%, #EA580C 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-label {
  color: #6B7280;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: 800;
  color: #111827;
  line-height: 1;
  background: linear-gradient(135deg, #111827 0%, #374151 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-value--alert {
  background: linear-gradient(135deg, #F97316 0%, #EA580C 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.recent-card {
  margin-top: 24px;
  border: 1px solid #E5E7EB;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  background: #fff;
  overflow: hidden;
}

.recent-card-header {
  padding: 20px 24px;
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  border-bottom: 2px solid #F3F4F6;
  background: linear-gradient(135deg, #fafafa 0%, #ffffff 100%);
}

.recent-list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #F9FAFB;
  font-size: 14px;
  transition: all 0.2s ease;
}

.recent-list-item:last-child {
  border-bottom: none;
}

.recent-list-item:hover {
  background: linear-gradient(90deg, #FFF7ED 0%, #ffffff 100%);
  transform: translateX(4px);
}

.recent-date {
  color: #374151;
  font-weight: 600;
  min-width: 100px;
}

.recent-meta {
  color: #6B7280;
  font-weight: 500;
}

.recent-meta strong {
  color: #F97316;
  font-weight: 700;
}

.recent-empty {
  padding: 32px;
  text-align: center;
  color: #9CA3AF;
  font-size: 14px;
}
</style>
