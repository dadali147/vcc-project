<template>
  <div class="page">
    <div class="page-header">
      <div class="header-content">
        <h1>{{ $t('cards.cardDetails') }}</h1>
        <p class="card-number">{{ cardData.cardNumber }}</p>
      </div>
      <div class="header-actions">
        <button class="action-btn recharge">💳 {{ $t('cards.recharge') }}</button>
        <button class="action-btn freeze">❄️ {{ $t('cards.freeze') }}</button>
        <button class="action-btn cancel">🗑️ {{ $t('cards.cancel') }}</button>
      </div>
    </div>

    <!-- Card Info Section -->
    <div class="card-info-section">
      <div class="info-grid">
        <div class="info-item">
          <span class="label">{{ $t('cards.cardholder') }}</span>
          <span class="value">{{ cardData.cardholder }}</span>
        </div>
        <div class="info-item">
          <span class="label">{{ $t('cards.cardType') }}</span>
          <span class="value badge">{{ cardData.cardType }}</span>
        </div>
        <div class="info-item">
          <span class="label">{{ $t('cards.status') }}</span>
          <span class="value badge" :class="cardData.status.toLowerCase()">{{ cardData.status }}</span>
        </div>
        <div class="info-item">
          <span class="label">{{ $t('cards.balance') }}</span>
          <span class="value highlight">${{ cardData.balance }}</span>
        </div>
        <div class="info-item">
          <span class="label">{{ $t('cards.dailyLimit') }}</span>
          <span class="value">${{ cardData.dailyLimit }}</span>
        </div>
        <div class="info-item">
          <span class="label">{{ $t('cards.monthlyLimit') }}</span>
          <span class="value">${{ cardData.monthlyLimit }}</span>
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
        <h3>{{ $t('cards.transactions') }}</h3>
        <table class="data-table">
          <thead>
            <tr>
              <th>{{ $t('transactions.date') }}</th>
              <th>{{ $t('transactions.merchant') }}</th>
              <th>{{ $t('transactions.amount') }}</th>
              <th>{{ $t('transactions.type') }}</th>
              <th>{{ $t('transactions.status') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tx in cardData.transactions" :key="tx.id">
              <td>{{ formatDate(tx.date, 'YYYY-MM-DD HH:mm') }}</td>
              <td>{{ tx.merchant }}</td>
              <td>${{ tx.amount }}</td>
              <td><span class="badge type">{{ tx.type }}</span></td>
              <td>
                <span class="badge" :class="tx.status.toLowerCase()">{{ tx.status }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Operations Tab -->
      <div v-if="activeTab === 'operations'" class="tab-content">
        <h3>{{ $t('cardOperation.title') }}</h3>
        <table class="data-table">
          <thead>
            <tr>
              <th>{{ $t('cardOperation.time') }}</th>
              <th>{{ $t('cardOperation.type') }}</th>
              <th>{{ $t('cardOperation.operator') }}</th>
              <th>{{ $t('cardOperation.content') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="op in cardData.operations" :key="op.id">
              <td>{{ formatDate(op.operationTime, 'YYYY-MM-DD HH:mm') }}</td>
              <td><span class="badge operation">{{ op.operationType }}</span></td>
              <td>{{ op.operator }}</td>
              <td class="content">{{ op.content }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { formatDate } from '@/utils/common'

const activeTab = ref('transactions')

const tabs = [
  { id: 'transactions', label: '交易明细' },
  { id: 'operations', label: '操作记录' }
]

const cardData = ref({
  cardNumber: '****1234',
  cardholder: 'John Doe',
  cardType: '储值卡',
  status: 'Active',
  balance: 5230.50,
  dailyLimit: 1000,
  monthlyLimit: 10000,
  transactions: [
    {
      id: '1',
      date: new Date(Date.now() - 3600000),
      merchant: 'Amazon',
      amount: 99.99,
      type: '消费',
      status: '成功'
    },
    {
      id: '2',
      date: new Date(Date.now() - 7200000),
      merchant: 'OpenAI',
      amount: 20.00,
      type: '消费',
      status: '成功'
    }
  ],
  operations: [
    {
      id: '1',
      operationTime: new Date(Date.now() - 86400000),
      operationType: '充值',
      operator: 'User Self',
      content: '充值 $1000 到卡内'
    },
    {
      id: '2',
      operationTime: new Date(Date.now() - 172800000),
      operationType: '预算调整',
      operator: 'Admin',
      content: '月限额从 $5000 调整为 $10000'
    },
    {
      id: '3',
      operationTime: new Date(Date.now() - 259200000),
      operationType: '开卡',
      operator: 'System',
      content: '卡片开卡成功，自动激活'
    }
  ]
})
</script>

<style scoped>
.page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  background: white;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.header-content h1 {
  margin: 0 0 8px 0;
  color: #111827;
}

.card-number {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #3B82F6;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 10px 16px;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f3f4f6;
  border-color: #3B82F6;
}

.action-btn.recharge {
  color: #10B981;
  border-color: #10B981;
}

.action-btn.recharge:hover {
  background: #F0FDF4;
}

.action-btn.freeze {
  color: #F59E0B;
  border-color: #F59E0B;
}

.action-btn.freeze:hover {
  background: #FFFBEB;
}

.action-btn.cancel {
  color: #EF4444;
  border-color: #EF4444;
}

.action-btn.cancel:hover {
  background: #FEF2F2;
}

.card-info-section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  margin-bottom: 24px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item .label {
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.info-item .value {
  font-size: 16px;
  font-weight: 500;
  color: #111827;
}

.info-item .value.badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 4px;
  background: #D1FAE5;
  color: #065F46;
  font-size: 14px;
  width: fit-content;
}

.info-item .value.badge.active {
  background: #D1FAE5;
  color: #065F46;
}

.info-item .value.badge.frozen {
  background: #FEF3C7;
  color: #92400E;
}

.info-item .value.highlight {
  color: #3B82F6;
  font-weight: 700;
}

.tabs-section {
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.tabs-header {
  display: flex;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.tab-button {
  flex: 1;
  padding: 16px;
  border: none;
  background: transparent;
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
}

.tab-button.active {
  color: #3B82F6;
  border-bottom-color: #3B82F6;
}

.tab-button:hover {
  background: white;
  color: #374151;
}

.tab-content {
  padding: 24px;
}

.tab-content h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  color: #374151;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead {
  background: #f9fafb;
}

.data-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: #374151;
  text-transform: uppercase;
  border-bottom: 1px solid #e5e7eb;
}

.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
  color: #111827;
}

.data-table tbody tr:hover {
  background: #f9fafb;
}

.badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.badge.type {
  background: #DBEAFE;
  color: #1E40AF;
}

.badge.success {
  background: #D1FAE5;
  color: #065F46;
}

.badge.failed {
  background: #FEE2E2;
  color: #991B1B;
}

.badge.operation {
  background: #F3E8FF;
  color: #6B21A8;
}

.content {
  max-width: 300px;
  word-break: break-word;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
  }

  .action-btn {
    width: 100%;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
