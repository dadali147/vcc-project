<template>
  <div class="app-container">
    <!-- 返回按钮 -->
    <el-page-header @back="goBack" style="margin-bottom: 16px">
      <template #content>卡片详情</template>
    </el-page-header>

    <el-row :gutter="16" v-loading="loading">
      <!-- 左侧：基本信息 -->
      <el-col :span="16">
        <el-card class="mb8">
          <template #header>基本信息</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="卡号">{{ detail.cardNoMask || '—' }}</el-descriptions-item>
            <el-descriptions-item label="卡名称">{{ detail.cardName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="卡类型">
              <el-tag :type="detail.cardType === 'BUDGET' ? 'warning' : ''">
                {{ detail.cardType === 'BUDGET' ? '预算卡' : '储值卡' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="cardStatusTagType">{{ cardStatusLabel }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="币种">{{ detail.currency || 'USD' }}</el-descriptions-item>
            <el-descriptions-item label="BIN">{{ detail.cardBin || '—' }}</el-descriptions-item>
            <el-descriptions-item label="持卡人">{{ detail.holderName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="到期日">{{ detail.expireAt || '—' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detail.createTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 余额信息 -->
        <el-card class="mb8">
          <template #header>余额信息</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="可用余额">{{ detail.availableAmount || '0.00' }}</el-descriptions-item>
            <el-descriptions-item label="冻结金额">{{ detail.frozenAmount || '0.00' }}</el-descriptions-item>
            <el-descriptions-item label="总余额">{{ detail.balance || '0.00' }}</el-descriptions-item>
            <el-descriptions-item label="预算额度" v-if="detail.cardType === 'BUDGET'">{{ detail.budgetAmount || '0.00' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 费用信息 -->
        <el-card class="mb8">
          <template #header>费用信息</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="开卡费">{{ detail.issueFee || '—' }}</el-descriptions-item>
            <el-descriptions-item label="月费">{{ detail.monthlyFee || '—' }}</el-descriptions-item>
            <el-descriptions-item label="交易手续费率">{{ detail.txnFeeRate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="下次扣费日">{{ detail.nextFeeDate || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 额度信息（预算卡） -->
        <el-card class="mb8" v-if="detail.cardType === 'BUDGET'">
          <template #header>额度信息</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="单笔限额">{{ detail.singleLimit || '—' }}</el-descriptions-item>
            <el-descriptions-item label="日限额">{{ detail.dailyLimit || '—' }}</el-descriptions-item>
            <el-descriptions-item label="月限额">{{ detail.monthlyLimit || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 右侧：操作 -->
      <el-col :span="8">
        <el-card class="mb8">
          <template #header>操作</template>
          <div class="action-list">
            <el-button type="primary" :disabled="detail.cardStatus !== 'ACTIVE'" @click="handleRecharge">充值</el-button>
            <el-button type="warning" :disabled="detail.cardStatus !== 'ACTIVE'" @click="handleFreeze">
              {{ detail.cardStatus === 'FROZEN' ? '解冻' : '冻结' }}
            </el-button>
            <el-button type="danger" :disabled="detail.cardStatus === 'CLOSED'" @click="handleCancel">销卡</el-button>
            <el-button @click="handleAdjustLimit" v-if="detail.cardType === 'BUDGET'">调整额度</el-button>
          </div>
        </el-card>

        <!-- 卡面展示 -->
        <el-card class="mb8">
          <template #header>卡面信息</template>
          <div class="card-visual">
            <div class="card-no">{{ detail.cardNoMask || '**** **** **** ****' }}</div>
            <div class="card-meta">
              <span>{{ detail.holderName || 'CARDHOLDER' }}</span>
              <span>{{ detail.expireAt || 'MM/YY' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 交易明细 Tab -->
    <el-card class="mt8">
      <template #header>
        <el-tabs v-model="activeTab">
          <el-tab-pane label="交易明细" name="transactions" />
          <el-tab-pane label="操作记录" name="operations" />
        </el-tabs>
      </template>

      <!-- 交易明细 -->
      <div v-show="activeTab === 'transactions'">
        <el-form :inline="true" :model="txnQuery">
          <el-form-item label="交易类型">
            <el-select v-model="txnQuery.category" clearable placeholder="全部" style="width: 140px">
              <el-option label="消费" value="PURCHASE" />
              <el-option label="退款" value="REFUND" />
              <el-option label="手续费" value="FEE" />
              <el-option label="调账" value="ADJUSTMENT" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="loadTransactions">查询</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="transactionList" v-loading="txnLoading">
          <el-table-column label="交易时间" prop="createTime" width="170" />
          <el-table-column label="交易类型" prop="category" width="100" />
          <el-table-column label="金额" prop="amount" width="120" />
          <el-table-column label="币种" prop="currency" width="80" />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="txnStatusTag(scope.row.status)" size="small">{{ txnStatusLabel(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="商户" prop="merchantName" show-overflow-tooltip />
          <el-table-column label="MCC" prop="mcc" width="80" />
          <el-table-column label="国家" prop="country" width="80" />
        </el-table>
      </div>

      <!-- 操作记录 -->
      <div v-show="activeTab === 'operations'">
        <el-table :data="operationList" v-loading="opLoading">
          <el-table-column label="操作时间" prop="createTime" width="170" />
          <el-table-column label="操作类型" prop="operation" width="120" />
          <el-table-column label="操作人" prop="operator" width="120" />
          <el-table-column label="描述" prop="description" show-overflow-tooltip />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCardDetail, listTransactions } from '@/api/merchant/card'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref({})
const activeTab = ref('transactions')
const transactionList = ref([])
const operationList = ref([])
const txnLoading = ref(false)
const opLoading = ref(false)

const txnQuery = ref({ category: '' })

// 卡状态枚举
const cardStatusMap = {
  PENDING_ACTIVATION: { label: '待激活', tag: 'info' },
  ACTIVE: { label: '正常', tag: 'success' },
  FROZEN: { label: '冻结', tag: 'warning' },
  CLOSED: { label: '已注销', tag: 'danger' }
}
const cardStatusLabel = computed(() => cardStatusMap[detail.value.cardStatus]?.label || detail.value.cardStatus)
const cardStatusTagType = computed(() => cardStatusMap[detail.value.cardStatus]?.tag || 'info')

// 交易状态枚举
function txnStatusLabel(s) {
  const m = { AUTHORIZED: '已授权', DECLINED: '已拒绝', SETTLED: '已清算', REVERSED: '已撤销', REFUNDED: '已退款', DISPUTED: '争议中', PROCESSING: '处理中', SUCCESS: '成功', FAILED: '失败' }
  return m[s] || s
}
function txnStatusTag(s) {
  const m = { AUTHORIZED: 'warning', DECLINED: 'danger', SETTLED: 'success', REVERSED: 'info', REFUNDED: 'info', DISPUTED: 'danger', SUCCESS: 'success', FAILED: 'danger' }
  return m[s] || ''
}

function goBack() { router.push('/merchant/card') }

function loadDetail() {
  loading.value = true
  getCardDetail(route.params.id || route.query.cardId).then(res => {
    detail.value = res.data || {}
  }).finally(() => loading.value = false)
}

function loadTransactions() {
  txnLoading.value = true
  listTransactions({ cardId: detail.value.id, ...txnQuery.value }).then(res => {
    transactionList.value = res.rows || []
  }).finally(() => txnLoading.value = false)
}

function handleRecharge() { router.push({ path: '/merchant/recharge', query: { cardId: detail.value.id } }) }
function handleFreeze() { /* TODO */ }
function handleCancel() { /* TODO */ }
function handleAdjustLimit() { /* TODO */ }

onMounted(() => {
  loadDetail()
  loadTransactions()
})
</script>

<style scoped>
.card-visual { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; padding: 24px; border-radius: 8px; }
.card-no { font-size: 20px; letter-spacing: 3px; margin-bottom: 16px; font-family: monospace; }
.card-meta { display: flex; justify-content: space-between; font-size: 13px; opacity: 0.9; }
.action-list { display: flex; flex-direction: column; gap: 10px; }
.action-list .el-button { width: 100%; }
</style>
