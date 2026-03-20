<template>
  <div class="page">
    <h1>{{ $t('downloads.title') }}</h1>

    <div class="downloads-container">
      <!-- Export Tabs -->
      <el-tabs v-model="activeTab" class="export-tabs">
        <!-- Transaction Export -->
        <el-tab-pane label="交易明细导出" name="transactions">
          <div class="export-section">
            <div class="export-form">
              <div class="form-group">
                <label>{{ $t('downloads.dateRange') }}</label>
                <div class="date-range">
                  <input 
                    v-model="transactionFilters.startDate" 
                    type="date"
                    class="date-input"
                  />
                  <span class="date-separator">-</span>
                  <input 
                    v-model="transactionFilters.endDate" 
                    type="date"
                    class="date-input"
                  />
                </div>
              </div>

              <div class="form-group">
                <label>{{ $t('downloads.exportFormat') }}</label>
                <select v-model="transactionFilters.format" class="select-input">
                  <option value="excel">Excel</option>
                  <option value="csv">CSV</option>
                </select>
              </div>

              <button class="export-button" @click="exportTransactions">
                <span v-if="!exporting">📥 {{ $t('downloads.export') }}</span>
                <span v-else>⏳ {{ $t('downloads.downloading') }}</span>
              </button>
            </div>

            <div class="export-info">
              <p>• 支持导出过去 12 个月的交易数据</p>
              <p>• Excel 格式包含详细交易信息（时间、商户、金额、类型、状态）</p>
              <p>• CSV 格式适合数据分析导入</p>
            </div>
          </div>
        </el-tab-pane>

        <!-- Recharge Export -->
        <el-tab-pane label="充值明细导出" name="recharge">
          <div class="export-section">
            <div class="export-form">
              <div class="form-group">
                <label>{{ $t('downloads.dateRange') }}</label>
                <div class="date-range">
                  <input 
                    v-model="rechargeFilters.startDate" 
                    type="date"
                    class="date-input"
                  />
                  <span class="date-separator">-</span>
                  <input 
                    v-model="rechargeFilters.endDate" 
                    type="date"
                    class="date-input"
                  />
                </div>
              </div>

              <div class="form-group">
                <label>{{ $t('downloads.exportFormat') }}</label>
                <select v-model="rechargeFilters.format" class="select-input">
                  <option value="excel">Excel</option>
                  <option value="csv">CSV</option>
                </select>
              </div>

              <button class="export-button" @click="exportRecharge">
                <span v-if="!exporting">📥 {{ $t('downloads.export') }}</span>
                <span v-else>⏳ {{ $t('downloads.downloading') }}</span>
              </button>
            </div>

            <div class="export-info">
              <p>• 包含所有充值记录（卡号、金额、时间、状态）</p>
              <p>• 支持按日期范围和卡片类型筛选</p>
              <p>• 自动计算充值小计和合计</p>
            </div>
          </div>
        </el-tab-pane>

        <!-- Monthly Statement Export -->
        <el-tab-pane label="月对账表导出" name="statement">
          <div class="export-section">
            <div class="export-form">
              <div class="form-group">
                <label>选择月份</label>
                <input 
                  v-model="statementFilters.month" 
                  type="month"
                  class="date-input"
                />
              </div>

              <div class="form-group">
                <label>{{ $t('downloads.exportFormat') }}</label>
                <select v-model="statementFilters.format" class="select-input">
                  <option value="excel">Excel</option>
                  <option value="pdf">PDF</option>
                </select>
              </div>

              <button class="export-button" @click="exportStatement">
                <span v-if="!exporting">📥 {{ $t('downloads.export') }}</span>
                <span v-else>⏳ {{ $t('downloads.downloading') }}</span>
              </button>
            </div>

            <div class="export-info">
              <p>• 生成完整的月度对账表</p>
              <p>• 包含开卡数、充值金额、交易金额、手续费等</p>
              <p>• PDF 格式可直接用于财务审计</p>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- Download History -->
      <div class="download-history">
        <h3>下载历史</h3>
        <table class="history-table">
          <thead>
            <tr>
              <th>文件名</th>
              <th>文件类型</th>
              <th>下载时间</th>
              <th>文件大小</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in downloadHistory" :key="item.id">
              <td>{{ item.filename }}</td>
              <td><span class="badge">{{ item.type }}</span></td>
              <td>{{ formatDate(item.downloadTime, 'YYYY-MM-DD HH:mm') }}</td>
              <td>{{ formatFileSize(item.fileSize) }}</td>
              <td>
                <a href="#" class="action-link">重新下载</a>
              </td>
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
const exporting = ref(false)

const transactionFilters = ref({
  startDate: '2024-01-01',
  endDate: new Date().toISOString().split('T')[0],
  format: 'excel'
})

const rechargeFilters = ref({
  startDate: '2024-01-01',
  endDate: new Date().toISOString().split('T')[0],
  format: 'excel'
})

const statementFilters = ref({
  month: new Date().toISOString().slice(0, 7),
  format: 'excel'
})

const downloadHistory = ref([
  {
    id: '1',
    filename: '2024年3月交易明细.xlsx',
    type: 'Excel',
    downloadTime: new Date(Date.now() - 3600000),
    fileSize: 512000
  },
  {
    id: '2',
    filename: '2024年2月充值明细.csv',
    type: 'CSV',
    downloadTime: new Date(Date.now() - 86400000),
    fileSize: 256000
  }
])

function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

async function exportTransactions() {
  exporting.value = true
  try {
    // Simulate file download
    await new Promise(resolve => setTimeout(resolve, 1500))
    console.log('Exporting transactions:', transactionFilters.value)
    // In real implementation, call API and trigger download
  } finally {
    exporting.value = false
  }
}

async function exportRecharge() {
  exporting.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    console.log('Exporting recharge:', rechargeFilters.value)
  } finally {
    exporting.value = false
  }
}

async function exportStatement() {
  exporting.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    console.log('Exporting statement:', statementFilters.value)
  } finally {
    exporting.value = false
  }
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

.downloads-container {
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  padding: 24px;
}

:deep(.export-tabs) {
  margin-bottom: 32px;
}

.export-section {
  padding: 20px 0;
}

.export-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 6px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-input,
.select-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.date-input:focus,
.select-input:focus {
  outline: none;
  border-color: #3B82F6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.date-separator {
  color: #6b7280;
  font-weight: 500;
}

.export-button {
  padding: 10px 16px;
  background: #3B82F6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  align-self: flex-end;
}

.export-button:hover {
  background: #2563EB;
  transform: translateY(-1px);
}

.export-info {
  padding: 12px 16px;
  background: #DBEAFE;
  border-radius: 6px;
  border-left: 4px solid #3B82F6;
}

.export-info p {
  margin: 4px 0;
  font-size: 13px;
  color: #1E40AF;
}

.download-history {
  margin-top: 32px;
  padding-top: 32px;
  border-top: 1px solid #e5e7eb;
}

.download-history h3 {
  margin-bottom: 16px;
  color: #374151;
  font-size: 16px;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
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

.badge {
  display: inline-block;
  padding: 4px 8px;
  background: #D1FAE5;
  color: #065F46;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.action-link {
  color: #3B82F6;
  text-decoration: none;
  transition: color 0.2s;
}

.action-link:hover {
  color: #2563EB;
  text-decoration: underline;
}
</style>
