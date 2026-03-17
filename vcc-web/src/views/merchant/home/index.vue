<template>
  <div class="app-container">
    <!-- 欢迎语和公告 -->
    <el-row :gutter="20" class="welcome-row">
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <h2>欢迎回来，{{ userName }}！</h2>
            <p>今天是 {{ today }}，祝您使用愉快</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon blue">
            <el-icon><CreditCard /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.cardCount }}</div>
            <div class="stat-label">我的卡片</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon green">
            <el-icon><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">${{ stats.balance }}</div>
            <div class="stat-label">账户余额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon orange">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayTransactions }}</div>
            <div class="stat-label">今日交易</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon purple">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">${{ stats.todayAmount }}</div>
            <div class="stat-label">今日消费</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告和快捷操作 -->
    <el-row :gutter="20" class="content-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span><el-icon><Bell /></el-icon> 系统公告</span>
              <el-button type="primary" link @click="$router.push('/merchant/notice')">查看更多</el-button>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(notice, index) in noticeList"
              :key="index"
              :type="notice.type"
              :timestamp="notice.time"
            >
              <div class="notice-item" @click="viewNotice(notice)">
                <div class="notice-title">{{ notice.title }}</div>
                <div class="notice-summary">{{ notice.summary }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-if="noticeList.length === 0" description="暂无公告" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span><el-icon><Operation /></el-icon> 快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/merchant/card')">
              <div class="action-icon blue">
                <el-icon><Plus /></el-icon>
              </div>
              <div class="action-text">申请开卡</div>
            </div>
            <div class="action-item" @click="$router.push('/merchant/recharge')">
              <div class="action-icon green">
                <el-icon><Money /></el-icon>
              </div>
              <div class="action-text">账户充值</div>
            </div>
            <div class="action-item" @click="$router.push('/merchant/transaction')">
              <div class="action-icon orange">
                <el-icon><List /></el-icon>
              </div>
              <div class="action-text">交易记录</div>
            </div>
            <div class="action-item" @click="$router.push('/merchant/user')">
              <div class="action-icon purple">
                <el-icon><User /></el-icon>
              </div>
              <div class="action-text">KYC认证</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近交易 -->
    <el-row class="recent-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span><el-icon><Timer /></el-icon> 最近交易</span>
              <el-button type="primary" link @click="$router.push('/merchant/transaction')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentTransactions" stripe>
            <el-table-column label="交易时间" prop="createdAt" width="180">
              <template #default="scope">
                {{ parseTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="卡号" prop="cardNoMask" width="160" />
            <el-table-column label="商户" prop="merchantName" />
            <el-table-column label="金额" width="120">
              <template #default="scope">
                <span :class="scope.row.txnType === 'AUTH' ? 'text-danger' : 'text-success'">
                  {{ scope.row.txnType === 'AUTH' ? '-' : '+' }}${{ scope.row.amount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 1" type="success" size="small">成功</el-tag>
                <el-tag v-else type="danger" size="small">失败</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告详情对话框 -->
    <el-dialog :title="currentNotice.title" v-model="noticeOpen" width="600px">
      <div class="notice-content">
        <div class="notice-time">发布时间：{{ currentNotice.time }}</div>
        <div class="notice-body">{{ currentNotice.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="MerchantHome">
import { listCard } from '@/api/card'
import { listTransaction } from '@/api/transaction'
import { getUserAccount } from '@/api/recharge'
import { getUserProfile } from '@/api/user'

const { proxy } = getCurrentInstance()

const userName = ref('')
const today = ref('')
const noticeOpen = ref(false)
const currentNotice = ref({})

const stats = reactive({
  cardCount: 0,
  balance: '0.00',
  todayTransactions: 0,
  todayAmount: '0.00'
})

const noticeList = ref([
  {
    title: '系统升级通知',
    summary: '系统将于本周日凌晨进行升级维护，预计耗时2小时...',
    content: '系统将于本周日（2026-03-22）凌晨 02:00-04:00 进行升级维护，期间部分功能可能无法使用，请提前做好准备。',
    time: '2026-03-17',
    type: 'warning'
  },
  {
    title: '新功能上线：3DS验证码实时推送',
    summary: '现在您可以在用户中心实时查看3DS验证码了...',
    content: '我们已上线3DS验证码实时推送功能，当您的卡片需要进行3D Secure验证时，验证码将实时显示在用户中心页面。',
    time: '2026-03-15',
    type: 'success'
  },
  {
    title: '充值费率调整公告',
    summary: '自2026年4月1日起，USDT充值费率将进行调整...',
    content: '自2026年4月1日起，USDT充值费率将从1%调整至0.8%，感谢您的支持。',
    time: '2026-03-10',
    type: 'info'
  }
])

const recentTransactions = ref([])

/** 获取用户信息 */
function getUserInfo() {
  getUserProfile().then(response => {
    userName.value = response.data?.nickName || response.data?.userName || '用户'
  })
}

/** 获取统计数据 */
function getStats() {
  // 获取卡片数量
  listCard({ pageNum: 1, pageSize: 1 }).then(response => {
    stats.cardCount = response.total || 0
  })
  
  // 获取余额
  getUserAccount('USD').then(response => {
    stats.balance = response.data?.balance || '0.00'
  })
  
  // 获取今日交易统计（简化处理）
  listTransaction({ pageNum: 1, pageSize: 10 }).then(response => {
    const today = new Date().toDateString()
    const todayList = response.rows?.filter(t => new Date(t.createdAt).toDateString() === today) || []
    stats.todayTransactions = todayList.length
    stats.todayAmount = todayList
      .filter(t => t.txnType === 'AUTH' && t.status === 1)
      .reduce((sum, t) => sum + parseFloat(t.amount || 0), 0)
      .toFixed(2)
  })
}

/** 获取最近交易 */
function getRecentTransactions() {
  listTransaction({ pageNum: 1, pageSize: 5 }).then(response => {
    recentTransactions.value = response.rows || []
  })
}

/** 查看公告详情 */
function viewNotice(notice) {
  currentNotice.value = notice
  noticeOpen.value = true
}

/** 设置今日日期 */
function setToday() {
  const date = new Date()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  today.value = `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 ${weekDays[date.getDay()]}`
}

getUserInfo()
getStats()
getRecentTransactions()
setToday()
</script>

<style scoped>
.welcome-row {
  margin-bottom: 20px;
}
.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}
.welcome-content h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}
.welcome-content p {
  margin: 0;
  opacity: 0.9;
}

.stats-row {
  margin-bottom: 20px;
}
.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}
.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-right: 15px;
}
.stat-icon.blue {
  background: #e6f7ff;
  color: #1890ff;
}
.stat-icon.green {
  background: #f6ffed;
  color: #52c41a;
}
.stat-icon.orange {
  background: #fff7e6;
  color: #fa8c16;
}
.stat-icon.purple {
  background: #f9f0ff;
  color: #722ed1;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #262626;
}
.stat-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-top: 5px;
}

.content-row {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.card-header .el-icon {
  margin-right: 5px;
}

.notice-item {
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
  transition: background 0.3s;
}
.notice-item:hover {
  background: #f5f7fa;
}
.notice-title {
  font-weight: bold;
  margin-bottom: 5px;
}
.notice-summary {
  font-size: 12px;
  color: #8c8c8c;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}
.action-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e8e8e8;
}
.action-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}
.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-right: 12px;
}
.action-icon.blue {
  background: #e6f7ff;
  color: #1890ff;
}
.action-icon.green {
  background: #f6ffed;
  color: #52c41a;
}
.action-icon.orange {
  background: #fff7e6;
  color: #fa8c16;
}
.action-icon.purple {
  background: #f9f0ff;
  color: #722ed1;
}
.action-text {
  font-size: 14px;
  font-weight: 500;
}

.recent-row {
  margin-top: 20px;
}

.text-danger {
  color: #ff4d4f;
}
.text-success {
  color: #52c41a;
}

.notice-content {
  padding: 10px;
}
.notice-time {
  color: #8c8c8c;
  font-size: 12px;
  margin-bottom: 15px;
}
.notice-body {
  line-height: 1.8;
  color: #262626;
}
</style>
