<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-semibold text-slate-800">Cardholders Management</h2>
      <el-button type="primary" icon="Plus" @click="dialogVisible = true">Add Cardholder</el-button>
    </div>

    <div class="bg-white shadow-sm rounded-xl border border-slate-200">
      <el-table :data="tableData" style="width: 100%" class="custom-table">
        <el-table-column prop="name" label="NAME" width="180">
          <template #default="scope">
            <div class="flex items-center">
              <div class="h-8 w-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 font-semibold mr-3">
                {{ scope.row.name.charAt(0) }}
              </div>
              <span class="font-medium text-slate-900">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="EMAIL" min-width="200" />
        <el-table-column prop="role" label="ROLE" width="120">
          <template #default="scope">
             <el-tag size="small" type="info">{{ scope.row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="activeCards" label="ACTIVE CARDS" width="120" align="center" />
        <el-table-column prop="monthlyBudget" label="MONTHLY BUDGET" width="150" align="right">
          <template #default="scope">
            <span class="font-medium text-slate-700">${{ scope.row.monthlyBudget }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="STATUS" width="100">
           <template #default="scope">
            <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium" :class="scope.row.status === 'Active' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'">
              {{ scope.row.status }}
            </span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="ACTIONS" width="150">
          <template #default="scope">
            <el-button link type="primary" size="small">Edit</el-button>
            <el-button link type="danger" size="small">Deactivate</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="Add New Cardholder"
      width="500px"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="Full Name" required>
          <el-input v-model="form.name" placeholder="e.g. John Doe" />
        </el-form-item>
        <el-form-item label="Email Address" required>
          <el-input v-model="form.email" placeholder="john.doe@example.com" />
        </el-form-item>
        <div class="flex gap-4">
          <el-form-item label="Monthly Budget Limit" class="flex-1">
             <el-input v-model="form.budget" type="number" placeholder="1000">
               <template #prefix>$</template>
             </el-input>
          </el-form-item>
          <el-form-item label="Currency" class="w-32">
            <el-select v-model="form.currency" placeholder="USD">
              <el-option label="USD" value="USD" />
              <el-option label="EUR" value="EUR" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="dialogVisible = false">Create Cardholder</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const dialogVisible = ref(false)
const form = reactive({
  name: '',
  email: '',
  budget: '',
  currency: 'USD'
})

const tableData = [
  { name: 'Alice Cooper', email: 'alice@company.com', role: 'Employee', activeCards: 2, monthlyBudget: '5,000.00', status: 'Active' },
  { name: 'Bob Builder', email: 'bob@company.com', role: 'Manager', activeCards: 1, monthlyBudget: '10,000.00', status: 'Active' },
  { name: 'Charlie Davis', email: 'charlie@company.com', role: 'Contractor', activeCards: 0, monthlyBudget: '500.00', status: 'Inactive' },
]
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
</style>
