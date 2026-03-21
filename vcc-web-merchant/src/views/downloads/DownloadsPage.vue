<template>
  <div class="page">
    <h1>{{ $t('downloads.title') }}</h1>

    <div class="downloads-container">
      <el-tabs v-model="activeTab" class="export-tabs">
        <el-tab-pane label="交易明细导出" name="transactions">
          <div class="export-section">
            <el-form :model="transactionFilters" :rules="transactionRules" ref="transactionFormRef">
              <div class="export-form">
                <el-form-item label="日期范围" prop="dateRange">
                  <div class="date-range">
                    <el-date-picker
                      v-model="transactionFilters.startDate"
                      type="date"
                      placeholder="开始日期"
                      style="width: 100%"
                    />
                    <span class="date-separator">-</span>
                    <el-date-picker
                      v-model="transactionFilters.endDate"
                      type="date"
                      placeholder="结束日期"
                      style="width: 100%"
                    />
                  </div>
                </el-form-item>

                <el-form-item label="导出格式" prop="format">
                  <el-select v-model="transactionFilters.format" style="width: 100%">
                    <el-option label="Excel" value="excel" />
                    <el-option label="CSV" value="csv" />
                  </el-select>
                </el-form-item>

                <el-button type="primary" @click="exportTransactions" :loading="exporting" class="export-button">
                  <span v-if="!exporting">📥 {{ $t('downloads.export') }}</span>
                  <span v-else>⏳ {{ $t('downloads.downloading') }}</span>
                </el-button>
              </div>
            </el-form>

            <div class="export-info">
              <p>• 支持导出过去 12 个月的交易数据</p>
              <p>• Excel 格式包含详细交易信息（时间、商户、金额、类型、状态）</p>
              <p>• CSV 格式适合数据分析导入</p>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="充值明细导出" name="recharge">
          <div class="export-section">
            <el-form :model="rechargeFilters" ref="rechargeFormRef">
              <div class="export-form">
                <el-form-item label="日期范围">
                  <div class="date-range">
                    <el-date-picker
                      v-model="rechargeFilters.startDate"
                      type="date"
                      placeholder="开始日期"
                      style="width: 100%"
                    />
                    <span class="date-separator">-</span>
                    <el-date-picker
                      v-model="rechargeFilters.endDate"
                      type="date"
                      placeholder="结束日期"
                      style="width: 100%"
                    />
                  </div>
                </el-form-item>

                <el-form-item label="导出格式">
                  <el-select v-model="rechargeFilters.format" style="width: 100%">
                    <el-option label="Excel" value="excel" />
                    <el-option label="CSV" value="csv" />
                  </el-select>
                </el-form-item>

                <el-button type="primary" @click="exportRecharge" :loading="exporting" class="export-button">
                  <span v-if="!exporting">📥 {{ $t('downloads.export') }}</span>
                  <span v-else>⏳ {{ $t('downloads.downloading') }}</span>
                </el-button>
              </div>
            </el-form>

            <div class="export-info">
              <p>• 包含所有充值记录（卡号、金额、时间、状态）</p>
              <p>• 支持按日期范围和卡片类型筛选</p>
              <p>• 自动计算充值小计和合计</p>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="月对账表导出" name="statement">
          <div class="export-section">
            <el-form :model="statementFilters" ref="statementFormRef">
              <div class="export-form">
                <el-form-item label="选择月份">
                  <el-date-picker
                    v-model="statementFilters.month"
                    type="month"
                    placeholder="选择月份"
                    style="width: 100%"
                  />
                </el-form-item>

                <el-form-item label="导出格式">
                  <el-select v-model="statementFilters.format" style="width: 100%">
                    <el-option label="Excel" value="excel" />
                    <el-option label="PDF" value="pdf" />
                  </el-select>
                </el-form-item>

                <el-button type="primary" @click="exportStatement" :loading="exporting" class="export-button">
                  <span v-if="!exporting">📥 {{ $t('downloads.export') }}</span>
                  <span v-else>⏳ {{ $t('downloads.downloading') }}</span>
                </el-button>
              </div>
            </el-form>

            <div class="export-info">
              <p>• 生成完整的月度对账表</p>
              <p>• 包含开卡数、充值金额、交易金额、手续费等</p>
              <p>• PDF 格式可直接用于财务审计</p>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <div class="download-history">
        <h3>下载历史</h3>
        <el-table :data="downloadHistory" style="width: 100%">
          <el-table-column prop="fileName" label="文件名" />
          <el-table-column prop="type" label="类型" width="120" />
          <el-table-column prop="createdAt" label="生成时间" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <span class="badge">{{ row.status }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="downloadFile(row)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { downloadApi } from '@/api'

const activeTab = ref('transactions')
const exporting = ref(false)
const transactionFormRef = ref(null)
const rechargeFormRef = ref(null)
const statementFormRef = ref(null)

const transactionFilters = reactive({
  startDate: '',
  endDate: '',
  format: 'excel'
})

const rechargeFilters = reactive({
  startDate: '',
  endDate: '',
  format: 'excel'
})

const statementFilters = reactive({
  month: '',
  format: 'excel'
})

const transactionRules = {
  dateRange: [
    { required: true, message: '请选择日期范围', trigger: 'change' }
  ]
}

const downloadHistory = ref([])

const exportTransactions = async () => {
  if (!transactionFilters.startDate || !transactionFilters.endDate) {
    ElMessage.warning('请选择日期范围')
    return
  }

  exporting.value = true
  try {
    const params = {
      startDate: transactionFilters.startDate,
      endDate: transactionFilters.endDate,
      format: transactionFilters.format
    }
    const blob = await downloadApi.exportTransactions(params)
    downloadBlob(blob, `transactions_${Date.now()}.${transactionFilters.format === 'excel' ? 'xlsx' : 'csv'}`)
    ElMessage.success('导出成功')
    loadDownloadHistory()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

const exportRecharge = async () => {
  if (!rechargeFilters.startDate || !rechargeFilters.endDate) {
    ElMessage.warning('请选择日期范围')
    return
  }

  exporting.value = true
  try {
    const params = {
      startDate: rechargeFilters.startDate,
      endDate: rechargeFilters.endDate,
      format: rechargeFilters.format
    }
    const blob = await downloadApi.exportRecharge(params)
    downloadBlob(blob, `recharge_${Date.now()}.${rechargeFilters.format === 'excel' ? 'xlsx' : 'csv'}`)
    ElMessage.success('导出成功')
    loadDownloadHistory()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

const exportStatement = async () => {
  if (!statementFilters.month) {
    ElMessage.warning('请选择月份')
    return
  }

  exporting.value = true
  try {
    const params = {
      month: statementFilters.month,
      format: statementFilters.format
    }
    const blob = await downloadApi.exportStatement(params)
    downloadBlob(blob, `statement_${Date.now()}.${statementFilters.format}`)
    ElMessage.success('导出成功')
    loadDownloadHistory()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

const downloadBlob = (blob, filename) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  window.URL.revokeObjectURL(url)
}

const downloadFile = (row) => {
  if (row.url) {
    window.open(row.url, '_blank')
  } else {
    ElMessage.warning('文件不存在或已过期')
  }
}

const loadDownloadHistory = async () => {
  try {
    const res = await downloadApi.getHistory({ page: 1, pageSize: 10 })
    downloadHistory.value = res.list || res.data || []
  } catch (err) {
    console.error('Failed to load download history:', err)
  }
}

onMounted(() => {
  loadDownloadHistory()
})
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
  max-width: 1000px;
  background: white;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e5e7eb;
}

.export-tabs {
  margin-bottom: 32px;
}

.export-section {
  padding: 20px 0;
}

.export-form {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 16px;
  align-items: end;
  margin-bottom: 20px;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-separator {
  color: #6b7280;
  font-weight: 500;
}

.export-button {
  height: 40px;
  padding: 0 24px;
}

.export-info {
  background: #EFF6FF;
  border-left: 3px solid #3B82F6;
  padding: 16px;
  border-radius: 6px;
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

.badge {
  display: inline-block;
  padding: 4px 8px;
  background: #D1FAE5;
  color: #065F46;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

@media (max-width: 768px) {
  .export-form {
    grid-template-columns: 1fr;
  }
}
</style>
