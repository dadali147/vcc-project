<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :sm="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-label">总开卡数</div>
            <div class="stat-value">{{ stats.totalCards || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :sm="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-label">活跃卡</div>
            <div class="stat-value text-success">{{ stats.activeCards || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :sm="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-label">储值卡</div>
            <div class="stat-value text-primary">{{ stats.prepaidCards || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :sm="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-label">预算卡</div>
            <div class="stat-value text-warning">{{ stats.budgetCards || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 按卡BIN统计 -->
      <el-col :lg="12" :xs="24">
        <el-card shadow="hover">
          <template #header>
            <span>按卡BIN统计</span>
          </template>
          <el-table :data="binStats" size="small">
            <el-table-column label="卡BIN" prop="binName" />
            <el-table-column label="品牌" prop="brand" width="100" />
            <el-table-column label="开卡数" prop="cardCount" width="80" align="right" />
            <el-table-column label="活跃数" prop="activeCount" width="80" align="right" />
            <el-table-column label="总余额" prop="totalBalance" width="120" align="right">
              <template #default="scope">
                {{ scope.row.totalBalance }} USD
              </template>
            </el-table-column>
          </el-table>
          <!-- 饼图占位 -->
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 按渠道统计 -->
      <el-col :lg="12" :xs="24">
        <el-card shadow="hover">
          <template #header>
            <span>按渠道统计</span>
          </template>
          <el-table :data="channelStats" size="small">
            <el-table-column label="渠道" prop="channel" />
            <el-table-column label="开卡数" prop="cardCount" width="80" align="right" />
            <el-table-column label="活跃数" prop="activeCount" width="80" align="right" />
            <el-table-column label="交易笔数" prop="transCount" width="100" align="right" />
            <el-table-column label="交易金额" prop="transAmount" width="120" align="right">
              <template #default="scope">
                {{ scope.row.transAmount }} USD
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 开卡趋势折线图 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>开卡趋势</span>
          <el-radio-group v-model="trendRange" size="small" @change="loadTrend">
            <el-radio-button value="7">近7天</el-radio-button>
            <el-radio-button value="30">近30天</el-radio-button>
            <el-radio-button value="90">近90天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="lineChartRef" class="chart-container" style="height: 350px"></div>
    </el-card>
  </div>
</template>

<script setup name="AdminStats">
import { getCardStats, getStatsByBin, getStatsByChannel, getCardTrend } from "@/api/admin/stats"
import * as echarts from 'echarts'

const stats = ref({})
const binStats = ref([])
const channelStats = ref([])
const trendRange = ref('30')

const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let lineChart = null

function loadStats() {
  getCardStats().then(response => {
    stats.value = response.data || {}
  })
}

function loadBinStats() {
  getStatsByBin().then(response => {
    binStats.value = response.data || []
    renderPieChart()
  })
}

function loadChannelStats() {
  getStatsByChannel().then(response => {
    channelStats.value = response.data || []
  })
}

function loadTrend() {
  getCardTrend({ days: trendRange.value }).then(response => {
    renderLineChart(response.data || [])
  })
}

function renderPieChart() {
  if (!pieChartRef.value || binStats.value.length === 0) return
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }
  const data = binStats.value.map(item => ({
    name: item.binName,
    value: item.cardCount
  }))
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: data,
      label: { formatter: '{b}\n{d}%' }
    }]
  })
}

function renderLineChart(trendData) {
  if (!lineChartRef.value) return
  if (!lineChart) {
    lineChart = echarts.init(lineChartRef.value)
  }
  const dates = trendData.map(item => item.date)
  const counts = trendData.map(item => item.count)
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '开卡数' },
    grid: { left: 60, right: 30, top: 40, bottom: 30 },
    series: [{
      type: 'line',
      data: counts,
      smooth: true,
      areaStyle: { opacity: 0.15 },
      itemStyle: { color: '#409eff' }
    }]
  })
}

function handleResize() {
  pieChart?.resize()
  lineChart?.resize()
}

onMounted(() => {
  loadStats()
  loadBinStats()
  loadChannelStats()
  loadTrend()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  margin-bottom: 16px;
}
.stat-item {
  text-align: center;
  padding: 8px 0;
}
.stat-label {
  font-size: 14px;
  color: #909399;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-top: 8px;
}
.text-success { color: #67c23a; }
.text-primary { color: #409eff; }
.text-warning { color: #e6a23c; }
.chart-container {
  height: 280px;
  margin-top: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
