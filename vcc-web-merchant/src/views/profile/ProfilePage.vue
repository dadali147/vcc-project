<template>
  <div class="page">
    <h1>{{ $t('common.profile') }}</h1>
    <div class="profile-container">
      <div class="profile-section">
        <h2>{{ $t('profile.personalInfo') }}</h2>
        <div class="info-grid">
          <div class="info-item">
            <label>{{ $t('profile.name') }}</label>
            <p>John Doe</p>
          </div>
          <div class="info-item">
            <label>{{ $t('profile.email') }}</label>
            <p>john@example.com</p>
          </div>
          <div class="info-item">
            <label>{{ $t('profile.phone') }}</label>
            <p>+1 (555) 123-4567</p>
          </div>
          <div class="info-item">
            <label>{{ $t('profile.company') }}</label>
            <p>Acme Corp</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.page {
  padding: 24px;
  background: white;
  border-radius: 8px;
}

.page h1 {
  margin-bottom: 32px;
  color: #111827;
}

.profile-section {
  margin-bottom: 32px;
}

.profile-section h2 {
  font-size: 18px;
  margin-bottom: 16px;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item label {
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.info-item p {
  font-size: 14px;
  color: #111827;
  margin: 0;
}
</style>
