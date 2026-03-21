<template>
  <div class="page">
    <h1>{{ $t('sharedCardLimits.title') }}</h1>

    <div class="limits-container">
      <!-- Search and Filter -->
      <div class="filter-bar">
        <input 
          v-model="searchText" 
          type="text"
          :placeholder="$t('sharedCardLimits.searchPlaceholder')"
          class="search-input"
        />
        <select v-model="selectedCard" class="filter-select">
          <option value="">{{ $t('sharedCardLimits.allCards') }}</option>
          <option value="card1">****1234</option>
          <option value="card2">****5678</option>
        </select>
      </div>

      <!-- Cards Overview -->
      <div class="cards-overview">
        <h3>{{ $t('sharedCardLimits.history') }}</h3>
        <div class="cards-grid">
          <div v-for="card in cardLimits" :key="card.id" class="limit-card">
            <div class="card-header">
              <span class="card-number">{{ card.cardNumber }}</span>
              <span class="card-holder">{{ card.cardholder }}</span>
            </div>
            <div class="limit-info">
              <div class="info-item">
                <span class="label">{{ $t('sharedCardLimits.limitAmount') }}</span>
                <span class="value">${{ card.limitAmount }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ $t('sharedCardLimits.usedAmount') }}</span>
                <span class="value">${{ card.usedAmount }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ $t('sharedCardLimits.remainingAmount') }}</span>
                <span class="value remaining">${{ card.remainingAmount }}</span>
              </div>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: (card.usedAmount / card.limitAmount * 100) + '%' }"></div>
            </div>
            <p class="progress-label">
              {{ $t('sharedCardLimits.usageRate') }}: {{ Math.round(card.usedAmount / card.limitAmount * 100) }}%
            </p>
            <button class="view-button" @click="viewCardHistory(card.id)">
              {{ $t('sharedCardLimits.viewHistory') }}
            </button>
          </div>
        </div>
      </div>

      <!-- Adjustment History Table -->
      <div class="history-section">
        <h3>{{ $t('sharedCardLimits.adjustTime') }}</h3>
        <table class="history-table">
          <thead>
            <tr>
              <th>{{ $t('sharedCardLimits.adjustTime') }}</th>
              <th>{{ $t('sharedCardLimits.card') }}</th>
              <th>{{ $t('sharedCardLimits.oldLimit') }}</th>
              <th>{{ $t('sharedCardLimits.newLimit') }}</th>
              <th>{{ $t('sharedCardLimits.adjustAmount') }}</th>
              <th>{{ $t('sharedCardLimits.adjustPerson') }}</th>
              <th>{{ $t('sharedCardLimits.remark') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in adjustmentHistory" :key="record.id">
              <td>{{ formatDate(record.adjustTime, 'YYYY-MM-DD HH:mm') }}</td>
              <td><span class="card-badge">{{ record.cardNumber }}</span></td>
              <td>${{ record.oldLimit }}</td>
              <td>${{ record.newLimit }}</td>
              <td :class="{ 'increase': record.change > 0, 'decrease': record.change < 0 }">
                <span v-if="record.change > 0">+${{ record.change }}</span>
                <span v-else>-${{ Math.abs(record.change) }}</span>
              </td>
              <td>{{ record.operator }}</td>
              <td>{{ record.remark }}</td>
            </tr>
          </tbody>
        </table>

        <!-- Pagination -->
        <div class="pagination">
          <span class="total">{{ $t('sharedCardLimits.totalRecords', { total: adjustmentHistory.length }) }}</span>
          <div class="page-buttons">
            <button :disabled="currentPage === 1">{{ $t('sharedCardLimits.prevPage') }}</button>
            <span class="page-info">{{ $t('sharedCardLimits.pageInfo', { current: currentPage, total: totalPages }) }}</span>
            <button :disabled="currentPage === totalPages">{{ $t('sharedCardLimits.nextPage') }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { formatDate } from '@/utils/common'

const searchText = ref('')
const selectedCard = ref('')
const currentPage = ref(1)

const cardLimits = ref([
  {
    id: '1',
    cardNumber: '****1234',
    cardholder: 'John Doe',
    limitAmount: 5000,
    usedAmount: 3250,
    remainingAmount: 1750
  },
  {
    id: '2',
    cardNumber: '****5678',
    cardholder: 'Jane Smith',
    limitAmount: 10000,
    usedAmount: 7500,
    remainingAmount: 2500
  },
  {
    id: '3',
    cardNumber: '****9012',
    cardholder: 'Bob Johnson',
    limitAmount: 8000,
    usedAmount: 2000,
    remainingAmount: 6000
  }
])

const adjustmentHistory = ref([
  {
    id: '1',
    adjustTime: new Date(Date.now() - 86400000),
    cardNumber: '****1234',
    oldLimit: 4000,
    newLimit: 5000,
    change: 1000,
    operator: 'Admin User',
    remark: '月度提额'
  },
  {
    id: '2',
    adjustTime: new Date(Date.now() - 172800000),
    cardNumber: '****5678',
    oldLimit: 12000,
    newLimit: 10000,
    change: -2000,
    operator: 'Risk Team',
    remark: '风控处理'
  },
  {
    id: '3',
    adjustTime: new Date(Date.now() - 259200000),
    cardNumber: '****9012',
    oldLimit: 8000,
    newLimit: 8000,
    change: 0,
    operator: 'Finance',
    remark: '额度确认'
  }
])

const totalPages = computed(() => Math.ceil(adjustmentHistory.value.length / 10))

function viewCardHistory(cardId) {
}
</script>

<style scoped>
.page {
  padding: 24px;
}

.page h1 {
  margin-bottom: 32px;
  color: #111827;
}

.limits-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.search-input,
.filter-select {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.search-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #3B82F6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.cards-overview h3,
.history-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.limit-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.2s;
}

.limit-card:hover {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.card-number {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.card-holder {
  font-size: 12px;
  color: #6b7280;
}

.limit-info {
  display: grid;
  gap: 8px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item .label {
  font-size: 13px;
  color: #6b7280;
}

.info-item .value {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.info-item .value.remaining {
  color: #10B981;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 6px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3B82F6, #2563EB);
  transition: width 0.3s ease;
}

.progress-label {
  font-size: 12px;
  color: #6b7280;
  margin: 0 0 12px 0;
}

.view-button {
  width: 100%;
  padding: 8px 12px;
  background: #f3f4f6;
  color: #3B82F6;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.view-button:hover {
  background: #DBEAFE;
  border-color: #3B82F6;
}

.history-section {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 24px;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.history-table thead {
  background: #f9fafb;
}

.history-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: #374151;
  text-transform: uppercase;
  border-bottom: 1px solid #e5e7eb;
}

.history-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
  color: #111827;
}

.history-table tbody tr:hover {
  background: #f9fafb;
}

.card-badge {
  display: inline-block;
  padding: 4px 8px;
  background: #DBEAFE;
  color: #1E40AF;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}

.increase {
  color: #10B981;
  font-weight: 600;
}

.decrease {
  color: #EF4444;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.total {
  font-size: 14px;
  color: #6b7280;
}

.page-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-buttons button {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-buttons button:hover:not(:disabled) {
  background: #f3f4f6;
  border-color: #3B82F6;
}

.page-buttons button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #6b7280;
  padding: 0 12px;
}
</style>
