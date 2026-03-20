<template>
  <div class="h-full flex flex-col space-y-4">
    <div class="flex items-center justify-between mb-2">
      <div class="flex items-center space-x-4">
        <el-input 
          v-model="search" 
          placeholder="Search by cardholder..." 
          prefix-icon="Search"
          class="w-64"
        />
        <el-select v-model="statusFilter" placeholder="Status" class="w-32">
          <el-option label="All" value="all" />
          <el-option label="Active" value="active" />
          <el-option label="Frozen" value="frozen" />
        </el-select>
        <el-select v-model="currencyFilter" placeholder="Currency" class="w-32">
          <el-option label="All" value="all" />
          <el-option label="USD" value="usd" />
          <el-option label="EUR" value="eur" />
        </el-select>
      </div>
      <div>
        <el-button type="primary" icon="Plus">Issue New Card</el-button>
      </div>
    </div>

    <div class="bg-white shadow-sm rounded-xl border border-slate-200 overflow-hidden flex-1">
      <el-table :data="tableData" style="width: 100%" class="custom-table">
        <el-table-column prop="maskedPan" label="CARD NUMBER" min-width="180">
          <template #default="scope">
            <span class="font-mono text-slate-800">{{ scope.row.maskedPan }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cardholder" label="CARDHOLDER" min-width="150" />
        <el-table-column prop="status" label="STATUS" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'Active' ? 'success' : 'warning'" size="small">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currency" label="CUR" width="80" align="center" />
        <el-table-column prop="balance" label="BALANCE" width="120" align="right">
          <template #default="scope">
            <span class="font-medium text-slate-900">${{ scope.row.balance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="ISSUED AT" width="120" />
        <el-table-column fixed="right" label="ACTIONS" width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleDetails(scope.row)">Details</el-button>
            <el-button link :type="scope.row.status === 'Active' ? 'warning' : 'success'" size="small">
              {{ scope.row.status === 'Active' ? 'Freeze' : 'Unfreeze' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleCancel(scope.row)">Cancel</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="p-4 border-t border-slate-200 flex justify-end">
        <el-pagination background layout="prev, pager, next" :total="100" />
      </div>
    </div>

    <el-drawer
      v-model="drawerVisible"
      title="Card Details"
      direction="rtl"
      size="400px"
    >
      <div v-if="selectedCard" class="space-y-6">
        <div class="bg-indigo-600 rounded-xl p-6 text-white shadow-lg">
          <div class="flex justify-between items-start mb-8">
            <span class="text-indigo-200 text-sm font-medium">Virtual Card</span>
            <el-icon class="text-2xl"><Platform /></el-icon>
          </div>
          <div class="font-mono text-xl tracking-widest mb-4">{{ selectedCard.maskedPan }}</div>
          <div class="flex justify-between items-end">
            <div>
              <div class="text-indigo-300 text-xs mb-1">Cardholder</div>
              <div class="font-semibold">{{ selectedCard.cardholder }}</div>
            </div>
            <div class="text-right">
              <div class="text-indigo-300 text-xs mb-1">Expires</div>
              <div class="font-medium">12/28</div>
            </div>
          </div>
        </div>

        <div class="space-y-4">
          <h4 class="font-semibold text-slate-800">Recent Transactions</h4>
          <el-skeleton :rows="3" />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'

const search = ref('')
const statusFilter = ref('')
const currencyFilter = ref('')
const drawerVisible = ref(false)
const selectedCard = ref(null)

const tableData = [
  { maskedPan: '4288 **** 1234', cardholder: 'John Doe', status: 'Active', currency: 'USD', balance: '1,250.00', createdAt: '2023-10-01' },
  { maskedPan: '5399 **** 5678', cardholder: 'Jane Smith', status: 'Frozen', currency: 'USD', balance: '300.50', createdAt: '2023-10-05' },
  { maskedPan: '4288 **** 9012', cardholder: 'Marketing Dept', status: 'Active', currency: 'EUR', balance: '5,000.00', createdAt: '2023-10-12' },
]

const handleDetails = (row) => {
  selectedCard.value = row
  drawerVisible.value = true
}

const handleCancel = (row) => {
  ElMessageBox.confirm(
    'This action will permanently cancel the card and cannot be undone. Remaining balance will be returned to the main account. Continue?',
    'Cancel Card',
    {
      confirmButtonText: 'Yes, Cancel',
      cancelButtonText: 'No',
      type: 'error',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(() => {
    ElMessage({ type: 'success', message: 'Card canceled successfully' })
  }).catch(() => {})
}
</script>

<style scoped>
:deep(.custom-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #64748b;
  --el-table-border-color: #e2e8f0;
}
:deep(.el-table th.el-table__cell) {
  @apply text-xs font-semibold uppercase tracking-wider bg-slate-50 border-b border-slate-200;
}
:deep(.el-table__row) {
  @apply transition-colors;
}
:deep(.el-table__row:hover > td.el-table__cell) {
  @apply bg-slate-50;
}
</style>
