<template>
  <div class="page">
    <div class="page-header">
      <div class="header-content">
        <button class="back-button" @click="$router.back()">← {{ t('common.back') }}</button>
        <h1>{{ t('cards.cardDetails') }}</h1>
        <p class="card-number">{{ cardData.cardNoMask }}</p>
      </div>
      <div class="header-actions" v-if="cardData.cardNoMask">
        <button
          v-if="cardData.status !== 'cancelled'"
          class="action-btn recharge"
          @click="showRechargeDialog = true"
        >
          💳 {{ t('cards.recharge') }}
        </button>
        <button
          v-if="cardData.status === 'active'"
          class="action-btn freeze"
          @click="handleFreeze"
        >
          ❄️ {{ t('cards.freeze') }}
        </button>
        <button
          v-if="cardData.status === 'frozen'"
          class="action-btn freeze"
          @click="handleUnfreeze"
        >
          🔥 {{ t('cards.unfreeze') }}
        </button>
        <button
          v-if="cardData.status !== 'cancelled'"
          class="action-btn cancel"
          @click="handleCancel"
        >
          🗑️ {{ t('cards.cancel') }}
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-state">{{ t('common.loading') }}</div>

    <template v-else-if="cardData.cardNoMask">
      <!-- Card Info Section -->
      <div class="card-info-section">
        <div class="info-grid">
          <div class="info-item">
            <span class="label">{{ t('cards.cardholder') }}</span>
            <span class="value">{{ cardData.cardholder || cardData.cardholderName }}</span>
          </div>
          <div class="info-item">
            <span class="label">{{ t('cards.cardType') }}</span>
            <span class="value badge">{{ getCardTypeLabel(cardData.cardType) }}</span>
          </div>
          <div class="info-item">
            <span class="label">{{ t('cards.status') }}</span>
            <span class="value badge" :class="(cardData.status || '').toLowerCase()">{{ getStatusLabel(cardData.status) }}</span>
          </div>
          <div class="info-item">
            <span class="label">{{ t('cards.balance') }}</span>
            <span class="value highlight">${{ Number(cardData.balance || 0).toFixed(2) }}</span>
          </div>
          <div class="info-item">
            <span class="label">{{ t('cards.dailyLimit') }}</span>
            <span class="value">${{ Number(cardData.dailyLimit || 0).toFixed(2) }}</span>
          </div>
          <div class="info-item">
            <span class="label">{{ t('cards.monthlyLimit') }}</span>
            <span class="value">${{ Number(cardData.monthlyLimit || 0).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- Tabs Section -->
      <div class="tabs-section">
        <div class="tabs-header">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            class="tab-button"
            :class="{ active: activeTab === tab.id }"
            @click="activeTab = tab.id"
          >
            {{ tab.label }}
          </button>
        </div>

        <!-- Transactions Tab -->
        <div v-if="activeTab === 'transactions'" class="tab-content">
          <div v-if="loadingTx" class="loading-state">{{ t('common.loading') }}</div>
          <table v-else-if="cardData.transactions && cardData.transactions.length" class="data-table">
            <thead>
              <tr>
                <th>{{ t('transactions.date') }}</th>
                <th>{{ t('transactions.merchant') }}</th>
                <th>{{ t('transactions.amount') }}</th>
                <th>{{ t('transactions.type') }}</th>
                <th>{{ t('transactions.status') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="tx in cardData.transactions" :key="tx.id">
                <td>{{ tx.createdAt || tx.date }}</td>
                <td>{{ tx.merchant }}</td>
                <td :class="tx.amount >= 0 ? 'amount-pos' : 'amount-neg'">${{ Math.abs(tx.amount || 0).toFixed(2) }}</td>
                <td><span class="badge type">{{ tx.type }}</span></td>
                <td><span class="badge" :class="(tx.status || '').toLowerCase()">{{ tx.status }}</span></td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-state">{{ t('transactions.noTransactions') }}</div>
        </div>

        <!-- Operations Tab -->
        <div v-if="activeTab === 'operations'" class="tab-content">
          <div v-if="loadingOps" class="loading-state">{{ t('common.loading') }}</div>
          <table v-else-if="cardData.operations && cardData.operations.length" class="data-table">
            <thead>
              <tr>
                <th>{{ t('cardOperation.time') }}</th>
                <th>{{ t('cardOperation.type') }}</th>
                <th>{{ t('cardOperation.operator') }}</th>
                <th>{{ t('cardOperation.content') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="op in cardData.operations" :key="op.id">
                <td>{{ op.operationTime || op.createdAt }}</td>
                <td><span class="badge operation">{{ op.operationType || op.type }}</span></td>
                <td>{{ op.operator }}</td>
                <td class="content">{{ op.content }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-state">{{ t('common.noData') }}</div>
        </div>
      </div>
    </template>

    <!-- Recharge Dialog -->
    <el-dialog v-model="showRechargeDialog" :title="t('cards.recharge')" width="400px">
      <el-form :model="rechargeForm" :rules="rechargeRules" ref="rechargeFormRef" label-position="top">
        <el-form-item :label="t('cards.rechargeAmount')" prop="amount">
          <el-input
            v-model.number="rechargeForm.amount"
            type="number"
            :placeholder="t('cardApplication.dailyLimitPlaceholder')"
          >
            <template #prepend>$</template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRechargeDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleRecharge" :loading="recharging">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cardApi, rechargeApi } from '@/api'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const activeTab = ref('transactions')
const loading = ref(false)
const loadingTx = ref(false)
const loadingOps = ref(false)
const showRechargeDialog = ref(false)
const recharging = ref(false)
const rechargeFormRef = ref(null)

const tabs = computed(() => [
  { id: 'transactions', label: t('cards.transactions') },
  { id: 'operations', label: t('cards.operations') }
])

const cardData = ref({
  cardNoMask: '',
  cardholder: '',
  cardholderName: '',
  cardType: '',
  status: '',
  balance: 0,
  dailyLimit: 0,
  monthlyLimit: 0,
  transactions: [],
  operations: []
})

const rechargeForm = ref({ amount: '' })
const rechargeRules = {
  amount: [
    { required: true, message: t('common.tip'), trigger: 'blur' },
    { type: 'number', min: 1, message: t('common.tip'), trigger: 'blur' }
  ]
}

const getCardTypeLabel = (type) => {
  const labels = { code: t('cards.cardTypeCode'), budget: t('cards.cardTypeBudget') }
  return labels[type] || type
}

const getStatusLabel = (status) => {
  const labels = {
    active: t('cards.statusActive'),
    frozen: t('cards.statusFrozen'),
    cancelled: t('cards.statusCancelled')
  }
  return labels[status] || status
}

const handleRecharge = async () => {
  if (!rechargeFormRef.value) return
  const valid = await rechargeFormRef.value.validate().catch(() => false)
  if (!valid) return

  recharging.value = true
  try {
    await rechargeApi.submit({ cardId: route.params.id, amount: rechargeForm.value.amount })
    ElMessage.success(t('cards.rechargeSuccess'))
    showRechargeDialog.value = false
    rechargeForm.value.amount = ''
    loadCardDetail()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || t('cards.rechargeFailed'))
  } finally {
    recharging.value = false
  }
}

const handleFreeze = async () => {
  try {
    await ElMessageBox.confirm(t('cards.confirmFreeze'), t('common.tip'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await cardApi.freeze(route.params.id)
    ElMessage.success(t('cards.freezeSuccess'))
    loadCardDetail()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(err.response?.data?.message || t('common.operationFailed'))
  }
}

const handleUnfreeze = async () => {
  try {
    await ElMessageBox.confirm(t('cards.confirmUnfreeze'), t('common.tip'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await cardApi.unfreeze(route.params.id)
    ElMessage.success(t('cards.unfreezeSuccess'))
    loadCardDetail()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(err.response?.data?.message || t('common.operationFailed'))
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(t('cards.confirmCancel'), t('common.tip'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await cardApi.cancel(route.params.id)
    ElMessage.success(t('cards.cancelSuccess'))
    router.push('/cards')
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(err.response?.data?.message || t('common.operationFailed'))
  }
}

const loadCardDetail = async () => {
  const cardId = route.params.id
  if (!cardId) return

  loading.value = true
  try {
    const res = await cardApi.get(cardId)
    const data = res.data || res
    cardData.value = { ...cardData.value, ...data }
  } catch (err) {
    ElMessage.error(t('common.loadFailed'))
    console.error(err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCardDetail()
})
</script>

<style scoped>
.page { padding: 24px; display: flex; flex-direction: column; gap: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; background: white; padding: 24px; border-radius: 12px; border: 1px solid #e5e7eb; }
.back-button { border: none; background: transparent; color: #6b7280; cursor: pointer; font-size: 14px; margin-bottom: 8px; display: block; padding: 0; }
.header-content h1 { margin: 0 0 4px; color: #111827; }
.card-number { margin: 0; font-size: 18px; font-weight: 600; color: #3B82F6; }
.header-actions { display: flex; gap: 10px; flex-wrap: wrap; }
.action-btn { padding: 10px 16px; border: 1px solid #d1d5db; background: white; border-radius: 8px; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.action-btn.recharge { color: #10B981; border-color: #10B981; }
.action-btn.recharge:hover { background: #F0FDF4; }
.action-btn.freeze { color: #F59E0B; border-color: #F59E0B; }
.action-btn.freeze:hover { background: #FFFBEB; }
.action-btn.cancel { color: #EF4444; border-color: #EF4444; }
.action-btn.cancel:hover { background: #FEF2F2; }
.card-info-section { background: white; padding: 24px; border-radius: 12px; border: 1px solid #e5e7eb; }
.info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 24px; }
.info-item { display: flex; flex-direction: column; }
.info-item .label { font-size: 11px; font-weight: 600; color: #6b7280; text-transform: uppercase; margin-bottom: 6px; }
.info-item .value { font-size: 15px; font-weight: 500; color: #111827; }
.info-item .value.badge { display: inline-block; padding: 4px 10px; border-radius: 6px; background: #D1FAE5; color: #065F46; font-size: 13px; width: fit-content; }
.info-item .value.badge.active { background: #D1FAE5; color: #065F46; }
.info-item .value.badge.frozen { background: #FEF3C7; color: #92400E; }
.info-item .value.badge.cancelled { background: #FEE2E2; color: #991B1B; }
.info-item .value.highlight { color: #3B82F6; font-weight: 700; font-size: 18px; }
.tabs-section { background: white; border-radius: 12px; border: 1px solid #e5e7eb; overflow: hidden; }
.tabs-header { display: flex; border-bottom: 1px solid #e5e7eb; background: #f9fafb; }
.tab-button { flex: 1; padding: 14px; border: none; background: transparent; color: #6b7280; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s; border-bottom: 2px solid transparent; }
.tab-button.active { color: #3B82F6; border-bottom-color: #3B82F6; background: white; }
.tab-content { padding: 20px; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { padding: 10px 12px; text-align: left; font-size: 12px; font-weight: 600; color: #374151; text-transform: uppercase; border-bottom: 1px solid #e5e7eb; background: #f9fafb; }
.data-table td { padding: 12px; border-bottom: 1px solid #f3f4f6; font-size: 14px; color: #111827; }
.data-table tbody tr:hover { background: #f9fafb; }
.badge { display: inline-block; padding: 3px 8px; border-radius: 4px; font-size: 12px; font-weight: 500; }
.badge.type { background: #DBEAFE; color: #1E40AF; }
.badge.success { background: #D1FAE5; color: #065F46; }
.badge.failed { background: #FEE2E2; color: #991B1B; }
.badge.operation { background: #F3E8FF; color: #6B21A8; }
.amount-pos { color: #059669; font-weight: 600; }
.amount-neg { color: #dc2626; font-weight: 600; }
.content { max-width: 280px; word-break: break-word; }
.loading-state, .empty-state { padding: 32px; text-align: center; color: #6b7280; }
@media (max-width: 768px) {
  .page-header { flex-direction: column; gap: 16px; }
  .header-actions { width: 100%; }
  .info-grid { grid-template-columns: 1fr; }
}
</style>
