<template>
  <div class="not-found">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1>{{ $t('common.pageNotFound') }}</h1>
      <p>The page you're looking for doesn't exist.</p>
      <router-link to="/dashboard" class="back-button">
        {{ $t('common.dashboard') }}
      </router-link>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.not-found-content {
  text-align: center;
  color: white;
}

.error-code {
  font-size: 96px;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

h1 {
  font-size: 48px;
  margin-bottom: 10px;
}

p {
  font-size: 18px;
  margin-bottom: 30px;
  opacity: 0.9;
}

.back-button {
  display: inline-block;
  padding: 12px 32px;
  background: white;
  color: #667eea;
  border-radius: 6px;
  font-weight: 600;
  transition: all 0.2s;
}

.back-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}
</style>
