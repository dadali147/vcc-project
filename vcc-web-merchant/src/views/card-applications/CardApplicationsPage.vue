<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1>{{ t('cardApplications.title') }}</h1>
        <p>{{ t('cardApplications.pageDescription') }}</p>
      </div>
      <button class="primary-button">{{ t('cardApplications.reapply') }}</button>
    </div>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('common.filter') }}</h2>
        <button class="link-button">{{ t('common.reset') }}</button>
      </div>
      <div class="filter-grid">
        <div class="form-group">
          <label>{{ t('common.search') }}</label>
          <input type="text" :placeholder="t('cardApplications.searchPlaceholder')" />
        </div>
        <div class="form-group">
          <label>{{ t('cardApplications.status') }}</label>
          <select>
            <option>{{ t('common.all') }}</option>
            <option>{{ t('cardApplications.statusPending') }}</option>
            <option>{{ t('cardApplications.statusApproved') }}</option>
            <option>{{ t('cardApplications.statusRejected') }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>{{ t('cardApplications.appliedAt') }}</label>
          <input type="text" :placeholder="t('common.dateRangePlaceholder')" />
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('cardApplications.list') }}</h2>
        <span class="meta">3</span>
      </div>
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>{{ t('cardApplications.applicationId') }}</th>
              <th>{{ t('cardApplications.cardholder') }}</th>
              <th>{{ t('cardApplications.cardType') }}</th>
              <th>{{ t('cardApplications.status') }}</th>
              <th>{{ t('cardApplications.reason') }}</th>
              <th>{{ t('cardApplications.appliedAt') }}</th>
              <th>{{ t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in rows" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.cardholder }}</td>
              <td>{{ item.cardType }}</td>
              <td><span class="status-pill">{{ item.status }}</span></td>
              <td>{{ item.reason }}</td>
              <td>{{ item.appliedAt }}</td>
              <td>
                <button class="table-button">{{ t('cards.cardDetails') }}</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="empty-state">
        <strong>{{ t('cardApplications.emptyTitle') }}</strong>
        <p>{{ t('cardApplications.noApplications') }}</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const rows = [
  {
    id: 'APP-20260320-001',
    cardholder: 'Luna Zhang',
    cardType: t('cards.cardTypeCode'),
    status: t('cardApplications.statusPending'),
    reason: 'Meta Ads campaign',
    appliedAt: '2026-03-20 09:30'
  },
  {
    id: 'APP-20260319-004',
    cardholder: 'Jason Chen',
    cardType: t('cards.cardTypeBudget'),
    status: t('cardApplications.statusApproved'),
    reason: 'Subscription payment',
    appliedAt: '2026-03-19 15:12'
  },
  {
    id: 'APP-20260318-008',
    cardholder: 'Luna Zhang',
    cardType: t('cards.cardTypeCode'),
    status: t('cardApplications.statusRejected'),
    reason: 'Missing KYC document',
    appliedAt: '2026-03-18 11:40'
  }
]
</script>

<style scoped>
.page-shell { display: flex; flex-direction: column; gap: 20px; }
.page-header, .panel-header { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.page-header h1, .panel-header h2 { margin: 0; color: #111827; }
.page-header p { margin: 8px 0 0; color: #6b7280; }
.panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 16px; padding: 20px; }
.filter-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group input, .form-group select { height: 40px; border: 1px solid #d1d5db; border-radius: 8px; padding: 0 12px; }
.table-wrapper { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 14px 12px; border-bottom: 1px solid #f3f4f6; text-align: left; }
th { color: #6b7280; font-weight: 600; }
.status-pill { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 999px; background: #eff6ff; color: #1d4ed8; font-size: 12px; font-weight: 600; }
.empty-state { margin-top: 20px; border: 1px dashed #d1d5db; border-radius: 12px; padding: 24px; text-align: center; color: #6b7280; }
.primary-button, .table-button, .link-button { border: none; border-radius: 8px; cursor: pointer; font-weight: 600; }
.primary-button { background: #2563eb; color: white; padding: 0 16px; height: 40px; }
.table-button { background: #f3f4f6; color: #374151; padding: 6px 10px; }
.link-button { background: transparent; color: #2563eb; }
.meta { background: #f3f4f6; border-radius: 999px; padding: 4px 10px; color: #6b7280; }
@media (max-width: 900px) {
  .filter-grid { grid-template-columns: 1fr; }
  .page-header, .panel-header { flex-direction: column; align-items: flex-start; }
}
</style>
