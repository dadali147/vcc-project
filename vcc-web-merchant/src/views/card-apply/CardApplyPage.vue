<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1>{{ t('cardApplication.title') }}</h1>
        <p>{{ t('cardApplication.pageDescription') }}</p>
      </div>
    </div>

    <div class="content-grid">
      <section class="panel">
        <div class="panel-header">
          <h2>{{ t('cardApplication.apply') }}</h2>
        </div>

        <form class="form-grid" @submit.prevent>
          <div class="form-group">
            <label>{{ t('cardApplication.cardholder') }}</label>
            <select>
              <option>{{ t('cardApplication.selectCardholder') }}</option>
              <option>Luna Zhang</option>
              <option>Jason Chen</option>
            </select>
          </div>
          <div class="form-group">
            <label>{{ t('cardApplication.cardType') }}</label>
            <select>
              <option>{{ t('cardApplication.selectCardType') }}</option>
              <option>{{ t('cards.cardTypeCode') }}</option>
              <option>{{ t('cards.cardTypeBudget') }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>{{ t('cardApplication.cardBin') }}</label>
            <select>
              <option>VCC-US-001</option>
              <option>VCC-HK-002</option>
            </select>
          </div>
          <div class="form-group">
            <label>{{ t('cardApplication.cardDailyLimit') }}</label>
            <input type="number" placeholder="5000" />
          </div>
          <div class="form-group">
            <label>{{ t('cardApplication.cardMonthlyLimit') }}</label>
            <input type="number" placeholder="50000" />
          </div>
          <div class="form-group">
            <label>{{ t('cardApplication.purpose') }}</label>
            <input type="text" :placeholder="t('cardApplication.purposePlaceholder')" />
          </div>
          <div class="form-group full-width">
            <label>{{ t('transactions.description') }}</label>
            <textarea :placeholder="t('cardApplication.remarkPlaceholder')"></textarea>
          </div>
          <div class="form-actions full-width">
            <button class="secondary-button" type="button">{{ t('common.saveDraft') }}</button>
            <button class="primary-button" type="submit">{{ t('cardApplication.submit') }}</button>
          </div>
        </form>
      </section>

      <section class="panel side-panel">
        <div class="panel-header">
          <h2>{{ t('cardApplication.applicationChecklist') }}</h2>
        </div>
        <ul class="check-list">
          <li>{{ t('cardApplication.checkKyc') }}</li>
          <li>{{ t('cardApplication.checkBalance') }}</li>
          <li>{{ t('cardApplication.checkLimits') }}</li>
          <li>{{ t('cardApplication.checkCardholder') }}</li>
        </ul>
        <div class="empty-state compact">
          <strong>{{ t('cardApplication.emptyTitle') }}</strong>
          <p>{{ t('cardApplication.emptyHint') }}</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
</script>

<style scoped>
.page-shell { display: flex; flex-direction: column; gap: 20px; }
.page-header h1 { margin: 0; color: #111827; }
.page-header p { margin: 8px 0 0; color: #6b7280; }
.content-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }
.panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 16px; padding: 20px; }
.panel-header { margin-bottom: 16px; }
.panel-header h2 { margin: 0; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group input, .form-group select, .form-group textarea {
  border: 1px solid #d1d5db; border-radius: 8px; padding: 10px 12px; font-size: 14px;
}
.form-group input, .form-group select { height: 40px; }
.form-group textarea { min-height: 110px; resize: vertical; }
.full-width { grid-column: 1 / -1; }
.form-actions { display: flex; justify-content: flex-end; gap: 12px; }
.primary-button, .secondary-button {
  height: 40px; border: none; border-radius: 8px; padding: 0 16px; cursor: pointer; font-weight: 600;
}
.primary-button { background: #2563eb; color: #fff; }
.secondary-button { background: #eef2ff; color: #4338ca; }
.check-list { margin: 0; padding-left: 18px; color: #4b5563; display: flex; flex-direction: column; gap: 12px; }
.empty-state { margin-top: 20px; border: 1px dashed #d1d5db; border-radius: 12px; padding: 20px; color: #6b7280; }
.compact { text-align: left; }
@media (max-width: 900px) {
  .content-grid, .form-grid { grid-template-columns: 1fr; }
}
</style>
