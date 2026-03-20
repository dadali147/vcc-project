<template>
  <div class="page">
    <h1>{{ $t('dashboard.title') }}</h1>
    <div class="dashboard-grid">
      <div class="stat-card">
        <div class="stat-label">{{ $t('dashboard.totalCards') }}</div>
        <div class="stat-value">24</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">{{ $t('dashboard.totalBalance') }}</div>
        <div class="stat-value">$5,230.50</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">{{ $t('dashboard.monthlyTransactions') }}</div>
        <div class="stat-value">127</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">{{ $t('dashboard.pendingApplications') }}</div>
        <div class="stat-value">3</div>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.page {
  padding: 24px;
}

.page h1 {
  margin-bottom: 32px;
  color: #111827;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #3B82F6;
}
</style>
