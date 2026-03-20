<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1>{{ t('transactions.title') }}</h1>
        <p>{{ t('transactions.pageDescription') }}</p>
      </div>
      <button class="primary-button">{{ t('transactions.export') }}</button>
    </div>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('common.filter') }}</h2>
        <button class="link-button">{{ t('common.reset') }}</button>
      </div>
      <div class="filter-grid">
        <div class="form-group">
          <label>{{ t('transactions.card') }}</label>
          <input type="text" :placeholder="t('transactions.cardPlaceholder')" />
        </div>
        <div class="form-group">
          <label>{{ t('transactions.typeFilter') }}</label>
          <select>
            <option>{{ t('common.all') }}</option>
            <option>{{ t('transactions.typeRecharge') }}</option>
            <option>{{ t('transactions.typeConsumption') }}</option>
            <option>{{ t('transactions.typeRefund') }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>{{ t('transactions.status') }}</label>
          <select>
            <option>{{ t('common.all') }}</option>
            <option>{{ t('transactions.statusSuccess') }}</option>
            <option>{{ t('transactions.statusPending') }}</option>
            <option>{{ t('transactions.statusFailed') }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>{{ t('transactions.dateRange') }}</label>
          <input type="text" :placeholder="t('common.dateRangePlaceholder')" />
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('transactions.list') }}</h2>
        <span class="meta">3</span>
      </div>
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>{{ t('transactions.transactionId') }}</th>
              <th>{{ t('transactions.card') }}</th>
              <th>{{ t('transactions.type') }}</th>
              <th>{{ t('transactions.amount') }}</th>
              <th>{{ t('transactions.merchant') }}</th>
              <th>{{ t('transactions.status') }}</th>
              <th>{{ t('transactions.date') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in rows" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.card }}</td>
              <td>{{ item.type }}</td>
              <td>{{ item.amount }}</td>
              <td>{{ item.merchant }}</td>
              <td><span class="status-pill">{{ item.status }}</span></td>
              <td>{{ item.date }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="empty-state">
        <strong>{{ t('transactions.emptyTitle') }}</strong>
        <p>{{ t('transactions.noTransactions') }}</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const rows = [
  {
    id: 'TXN-0019281',
    card: '5583 **** 2091',
    type: t('transactions.typeConsumption'),
    amount: '-$320.00',
    merchant: 'Meta Ads',
    status: t('transactions.statusSuccess'),
    date: '2026-03-20 10:12'
  },
  {
    id: 'TXN-0019274',
    card: '4219 **** 6430',
    type: t('transactions.typeRecharge'),
    amount: '+$1,000.00',
    merchant: 'Wallet Top-up',
    status: t('transactions.statusSuccess'),
    date: '2026-03-20 09:41'
  },
  {
    id: 'TXN-0019208',
    card: '5583 **** 2091',
    type: t('transactions.typeRefund'),
    amount: '+$48.90',
    merchant: 'Canva',
    status: t('transactions.statusPending'),
    date: '2026-03-19 16:30'
  }
]
</script>

<style scoped>
.page-shell { display: flex; flex-direction: column; gap: 20px; }
.page-header, .panel-header { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.page-header h1, .panel-header h2 { margin: 0; color: #111827; }
.page-header p { margin: 8px 0 0; color: #6b7280; }
.panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 16px; padding: 20px; }
.filter-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group input, .form-group select { height: 40px; border: 1px solid #d1d5db; border-radius: 8px; padding: 0 12px; }
.table-wrapper { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 14px 12px; border-bottom: 1px solid #f3f4f6; text-align: left; }
th { color: #6b7280; font-weight: 600; }
.status-pill { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 999px; background: #eff6ff; color: #1d4ed8; font-size: 12px; font-weight: 600; }
.empty-state { margin-top: 20px; border: 1px dashed #d1d5db; border-radius: 12px; padding: 24px; text-align: center; color: #6b7280; }
.primary-button, .link-button { border: none; border-radius: 8px; cursor: pointer; font-weight: 600; }
.primary-button { background: #2563eb; color: white; padding: 0 16px; height: 40px; }
.link-button { background: transparent; color: #2563eb; }
.meta { background: #f3f4f6; border-radius: 999px; padding: 4px 10px; color: #6b7280; }
@media (max-width: 900px) {
  .filter-grid { grid-template-columns: 1fr; }
  .page-header, .panel-header { flex-direction: column; align-items: flex-start; }
}
</style>
