<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">总开卡数</div>
          <div class="stat-value">{{ stats.totalCards }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">活跃卡片</div>
          <div class="stat-value text-success">{{ stats.activeCards }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">储值卡数量</div>
          <div class="stat-value text-primary">{{ stats.prepaidCards }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">预算卡数量</div>
          <div class="stat-value text-warning">{{ stats.budgetCards }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 按卡BIN统计 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>按卡BIN统计</span>
            </div>
          </template>
          <el-table :data="cardBinStats" size="small">
            <el-table-column label="卡BIN" prop="cardBinName" />
            <el-table-column label="卡组织" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.cardBrand === 'VISA'" type="primary">Visa</el-tag>
                <el-tag v-else type="danger">Mastercard</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="开卡数" prop="cardCount" width="100" />
            <el-table-column label="占比" width="120">
              <template #default="scope">
                <el-progress :percentage="scope.row.percentage" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>按渠道统计</span>
            </div>
          </template>
          <el-table :data="channelStats" size="small">
            <el-table-column label="渠道" prop="channelName" />
            <el-table-column label="开卡数" prop="cardCount" width="100" />
            <el-table-column label="交易金额" prop="transactionAmount" width="120" />
            <el-table-column label="占比" width="120">
              <template #default="scope">
                <el-progress :percentage="scope.row.percentage" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 按卡类型统计 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>卡类型分布</span>
            </div>
          </template>
          <div class="pie-chart" ref="typeChart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>卡片状态分布</span>
            </div>
          </template>
          <div class="pie-chart" ref="statusChart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图 -->
    <el-row class="chart-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>开卡趋势（近30天）</span>
            </div>
          </template>
          <div class="line-chart" ref="trendChart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="CardStats">
import { getCardStats } from '@/api/admin/card'
import * as echarts from 'echarts'

const { proxy } = getCurrentInstance()

const stats = reactive({
  totalCards: 0,
  activeCards: 0,
  prepaidCards: 0,
  budgetCards: 0
})

const cardBinStats = ref([])
const channelStats = ref([])

const typeChart = ref(null)
const statusChart = ref(null)
const trendChart = ref(null)

let typeChartInstance = null
let statusChartInstance = null
let trendChartInstance = null

/** 获取统计数据 */
function getStats() {
  getCardStats().then(response => {
    const data = response.data || {}
    stats.totalCards = data.totalCards || 0
    stats.activeCards = data.activeCards || 0
    stats.prepaidCards = data.prepaidCards || 0
    stats.budgetCards = data.budgetCards || 0
    
    cardBinStats.value = data.cardBinStats || []
    channelStats.value = data.channelStats || []
    
    // 初始化图表
    initCharts(data)
  })
}

/** 初始化图表 */
function initCharts(data) {
  // 卡类型饼图
  if (typeChart.value) {
    typeChartInstance = echarts.init(typeChart.value)
    typeChartInstance.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '5%' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
        data: [
          { value: data.prepaidCards || 0, name: '储值卡', itemStyle: { color: '#67C23A' } },
          { value: data.budgetCards || 0, name: '预算卡', itemStyle: { color: '#E6A23C' } }
        ]
      }]
    })
  }
  
  // 卡片状态饼图
  if (statusChart.value) {
    statusChartInstance = echarts.init(statusChart.value)
    statusChartInstance.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '5%' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
        data: [
          { value: data.activeCards || 0, name: '正常', itemStyle: { color: '#67C23A' } },
          { value: data.frozenCards || 0, name: '冻结', itemStyle: { color: '#E6A23C' } },
          { value: data.cancelledCards || 0, name: '已注销', itemStyle: { color: '#909399' } }
        ]
      }]
    })
  }
  
  // 开卡趋势折线图
  if (trendChart.value) {
    trendChartInstance = echarts.init(trendChart.value)
    const trendData = data.trendData || []
    trendChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: trendData.map(d => d.date)
      },
      yAxis: { type: 'value' },
      series: [{
        data: trendData.map(d => d.count),
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        itemStyle: { color: '#409EFF' }
      }]
    })
  }
}

/** 窗口大小改变时重绘图表 */
function handleResize() {
  typeChartInstance?.resize()
  statusChartInstance?.resize()
  trendChartInstance?.resize()
}

onMounted(() => {
  getStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  typeChartInstance?.dispose()
  statusChartInstance?.dispose()
  trendChartInstance?.dispose()
})
</script>

<style scoped>
.stats-row {
  margin-bottom: 20px;
}
.stat-card {
  text-align: center;
}
.stat-title {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #262626;
}
.text-success {
  color: #52c41a;
}
.text-primary {
  color: #409EFF;
}
.text-warning {
  color: #E6A23C;
}

.chart-row {
  margin-bottom: 20px;
}
.card-header {
  font-weight: bold;
}

.pie-chart {
  height: 300px;
}
.line-chart {
  height: 350px;
}
</style>
