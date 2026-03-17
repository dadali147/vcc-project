<template>
  <div class="app-container home-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :sm="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #409eff">
              <el-icon :size="28"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总余额 (USD)</div>
              <div class="stat-value">{{ stats.totalBalance || '0.00' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :sm="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon :size="28"><CreditCard /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">卡片数</div>
              <div class="stat-value">{{ stats.cardCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :sm="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon :size="28"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本月消费</div>
              <div class="stat-value">{{ stats.monthSpend || '0.00' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :sm="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon :size="28"><Upload /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本月充值</div>
              <div class="stat-value">{{ stats.monthRecharge || '0.00' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 最近交易 -->
      <el-col :lg="16" :xs="24">
        <el-card shadow="hover" class="section-card">
          <template #header>
            <div class="card-header">
              <span>最近交易</span>
              <el-button link type="primary" @click="$router.push('/merchant/transaction')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentTransactions" v-loading="loading" size="small">
            <el-table-column label="交易时间" prop="transTime" width="170">
              <template #default="scope">
                <span>{{ parseTime(scope.row.transTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="卡号末四位" prop="cardLast4" width="100" />
            <el-table-column label="商户" prop="merchantName" show-overflow-tooltip />
            <el-table-column label="金额" prop="amount" width="120" align="right">
              <template #default="scope">
                <span :class="scope.row.amount > 0 ? 'text-danger' : 'text-success'">
                  {{ scope.row.amount > 0 ? '-' : '+' }}{{ Math.abs(scope.row.amount).toFixed(2) }} {{ scope.row.currency }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" prop="status" width="80" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === '1' ? 'success' : scope.row.status === '2' ? 'danger' : 'info'" size="small">
                  {{ scope.row.status === '1' ? '成功' : scope.row.status === '2' ? '失败' : '处理中' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：公告 + 快捷操作 -->
      <el-col :lg="8" :xs="24">
        <el-card shadow="hover" class="section-card">
          <template #header>
            <span>系统公告</span>
          </template>
          <div v-if="notices.length === 0" class="empty-text">暂无公告</div>
          <div v-for="item in notices" :key="item.noticeId" class="notice-item">
            <el-tag :type="item.noticeType === '1' ? 'warning' : ''" size="small">
              {{ item.noticeType === '1' ? '通知' : '公告' }}
            </el-tag>
            <span class="notice-title">{{ item.noticeTitle }}</span>
            <span class="notice-time">{{ parseTime(item.createTime, '{m}-{d}') }}</span>
          </div>
        </el-card>

        <el-card shadow="hover" class="section-card" style="margin-top: 16px">
          <template #header>
            <span>快捷操作</span>
          </template>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-button type="primary" plain style="width: 100%; margin-bottom: 12px" @click="$router.push('/merchant/card')">
                <el-icon><CreditCard /></el-icon> 开卡
              </el-button>
            </el-col>
            <el-col :span="12">
              <el-button type="success" plain style="width: 100%; margin-bottom: 12px" @click="$router.push('/merchant/recharge')">
                <el-icon><Upload /></el-icon> 充值
              </el-button>
            </el-col>
            <el-col :span="12">
              <el-button type="warning" plain style="width: 100%" @click="$router.push('/merchant/cardHolder')">
                <el-icon><User /></el-icon> 持卡人
              </el-button>
            </el-col>
            <el-col :span="12">
              <el-button type="info" plain style="width: 100%" @click="$router.push('/merchant/wallet')">
                <el-icon><Wallet /></el-icon> 钱包
              </el-button>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="MerchantHome">
import { getHomeStats, getRecentTransactions, getNotices } from "@/api/transaction"

const { proxy } = getCurrentInstance()

const stats = ref({})
const recentTransactions = ref([])
const notices = ref([])
const loading = ref(true)

function loadData() {
  loading.value = true
  getHomeStats().then(response => {
    stats.value = response.data || {}
  })
  getRecentTransactions().then(response => {
    recentTransactions.value = response.data || []
    loading.value = false
  })
  getNotices().then(response => {
    notices.value = response.data || []
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.home-container {
  padding: 16px;
}
.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  margin-bottom: 16px;
}
.stat-item {
  display: flex;
  align-items: center;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.stat-info {
  margin-left: 16px;
}
.stat-label {
  font-size: 14px;
  color: #909399;
}
.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-top: 4px;
}
.section-card {
  margin-bottom: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.notice-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.notice-item:last-child {
  border-bottom: none;
}
.notice-title {
  flex: 1;
  margin-left: 8px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notice-time {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}
.empty-text {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}
.text-danger {
  color: #f56c6c;
}
.text-success {
  color: #67c23a;
}
</style>
