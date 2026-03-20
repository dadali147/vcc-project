<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1>{{ t('cardholders.title') }}</h1>
        <p>{{ t('cardholders.pageDescription') }}</p>
      </div>
      <div class="header-actions">
        <button class="secondary-button">{{ t('common.export') }}</button>
        <button class="primary-button">{{ t('cardholders.add') }}</button>
      </div>
    </div>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('common.filter') }}</h2>
        <button class="link-button">{{ t('common.reset') }}</button>
      </div>
      <div class="filter-grid">
        <div class="form-group">
          <label>{{ t('common.search') }}</label>
          <input type="text" :placeholder="t('cardholders.searchPlaceholder')" />
        </div>
        <div class="form-group">
          <label>{{ t('cardholders.status') }}</label>
          <select>
            <option>{{ t('common.all') }}</option>
            <option>{{ t('cardholders.statusActive') }}</option>
            <option>{{ t('cardholders.statusPending') }}</option>
            <option>{{ t('cardholders.statusDisabled') }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>{{ t('cardholders.createdAt') }}</label>
          <input type="text" :placeholder="t('common.dateRangePlaceholder')" />
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>{{ t('cardholders.list') }}</h2>
        <span class="meta">2</span>
      </div>
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>{{ t('cardholders.name') }}</th>
              <th>{{ t('cardholders.email') }}</th>
              <th>{{ t('cardholders.phone') }}</th>
              <th>{{ t('cardholders.idNumber') }}</th>
              <th>{{ t('cardholders.status') }}</th>
              <th>{{ t('cardholders.createdAt') }}</th>
              <th>{{ t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in rows" :key="item.id">
              <td>{{ item.name }}</td>
              <td>{{ item.email }}</td>
              <td>{{ item.phone }}</td>
              <td>{{ item.idNumber }}</td>
              <td><span class="status-pill">{{ item.status }}</span></td>
              <td>{{ item.createdAt }}</td>
              <td>
                <div class="action-row">
                  <button class="table-button">{{ t('common.edit') }}</button>
                  <button class="table-button danger">{{ t('common.delete') }}</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="empty-state">
        <strong>{{ t('cardholders.emptyTitle') }}</strong>
        <p>{{ t('cardholders.noCardholders') }}</p>
        <button class="primary-button primary-button--small">{{ t('cardholders.add') }}</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const rows = [
  {
    id: 1,
    name: 'Luna Zhang',
    email: 'luna@example.com',
    phone: '+86 13800000001',
    idNumber: '310***********1024',
    status: t('cardholders.statusActive'),
    createdAt: '2026-03-18 14:20'
  },
  {
    id: 2,
    name: 'Jason Chen',
    email: 'jason@example.com',
    phone: '+86 13800000002',
    idNumber: '440***********2211',
    status: t('cardholders.statusPending'),
    createdAt: '2026-03-19 10:05'
  }
]
</script>

<style scoped>
.page-shell {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.page-header,
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}
.page-header h1,
.panel-header h2 {
  margin: 0;
  color: #111827;
}
.page-header p {
  margin: 8px 0 0;
  color: #6b7280;
}
.panel {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 20px;
}
.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group input, .form-group select {
  height: 40px; border: 1px solid #d1d5db; border-radius: 8px; padding: 0 12px;
}
.table-wrapper { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 14px 12px; border-bottom: 1px solid #f3f4f6; text-align: left; }
th { color: #6b7280; font-weight: 600; }
.status-pill {
  display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 999px;
  background: #eff6ff; color: #1d4ed8; font-size: 12px; font-weight: 600;
}
.empty-state {
  margin-top: 20px; border: 1px dashed #d1d5db; border-radius: 12px; padding: 24px; text-align: center; color: #6b7280;
}
.header-actions, .action-row { display: flex; gap: 12px; }
.primary-button, .secondary-button, .table-button, .link-button {
  border: none; border-radius: 8px; cursor: pointer; font-weight: 600;
}
.primary-button { background: #2563eb; color: white; padding: 0 16px; height: 40px; }
.primary-button--small { height: 36px; }
.secondary-button { background: #eef2ff; color: #4338ca; padding: 0 16px; height: 40px; }
.table-button { background: #f3f4f6; color: #374151; padding: 6px 10px; }
.table-button.danger { color: #b91c1c; }
.link-button { background: transparent; color: #2563eb; }
.meta { background: #f3f4f6; border-radius: 999px; padding: 4px 10px; color: #6b7280; }
@media (max-width: 900px) {
  .filter-grid { grid-template-columns: 1fr; }
  .page-header, .panel-header { flex-direction: column; align-items: flex-start; }
}
</style>
