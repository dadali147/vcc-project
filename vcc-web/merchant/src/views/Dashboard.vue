<template>
  <div class="space-y-6">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div v-for="stat in stats" :key="stat.name" class="bg-white overflow-hidden shadow-sm rounded-xl border border-slate-200">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <el-icon class="h-6 w-6 text-indigo-600" :is="stat.icon"></el-icon>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-slate-500 truncate">{{ stat.name }}</dt>
                <dd>
                  <div class="text-2xl font-bold text-slate-900">{{ stat.value }}</div>
                </dd>
              </dl>
            </div>
          </div>
        </div>
        <div class="bg-slate-50 px-5 py-3">
          <div class="text-sm">
            <a href="#" class="font-medium text-indigo-600 hover:text-indigo-500">View all</a>
          </div>
        </div>
      </div>
    </div>

    <div class="bg-white shadow-sm rounded-xl border border-slate-200 mt-8">
      <div class="px-6 py-5 border-b border-slate-200 flex justify-between items-center">
        <h3 class="text-lg leading-6 font-medium text-slate-900">Recent Transactions</h3>
        <el-button type="primary" plain size="small">Export</el-button>
      </div>
      <div class="p-6">
        <el-skeleton :rows="5" animated />
        <!-- Actual table implementation will replace this skeleton -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Wallet, CreditCard, DocumentCopy } from '@element-plus/icons-vue'

const stats = ref([
  { name: 'Total Available Balance', value: '$84,302.00', icon: Wallet },
  { name: 'Active Cards', value: '142', icon: CreditCard },
  { name: 'Cards Issued This Month', value: '38', icon: DocumentCopy },
])
</script>
