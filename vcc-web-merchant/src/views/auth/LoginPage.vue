<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <img src="@/assets/logo/kimoox-logo-stacked.svg" alt="kimoox" class="logo-img" />
          <h1>{{ $t('common.login') }}</h1>
          <p class="subtitle">{{ $t('auth.portalSubtitle', 'Virtual Card Merchant Portal') }}</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label>{{ $t('auth.email') }}</label>
            <input 
              v-model="credentials.email" 
              type="email" 
              :placeholder="$t('auth.email')"
              required
            />
          </div>

          <div class="form-group">
            <label>{{ $t('auth.password') }}</label>
            <input 
              v-model="credentials.password" 
              type="password" 
              :placeholder="$t('auth.password')"
              required
            />
          </div>

          <div class="form-options">
            <label class="checkbox">
              <input type="checkbox" v-model="credentials.rememberMe" />
              {{ $t('auth.rememberMe') }}
            </label>
            <a href="#" class="forgot-password">{{ $t('auth.forgotPassword') }}</a>
          </div>

          <button type="submit" class="login-button" :disabled="loading">
            {{ loading ? $t('common.loading') : $t('common.login') }}
          </button>
        </form>

        <div class="login-footer">
          <p>{{ $t('auth.noAccount') }} <router-link to="/register">{{ $t('common.register') }}</router-link></p>
        </div>

        <div v-if="error" class="error-message">{{ error }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const credentials = ref({
  email: 'demo@example.com',
  password: 'password123',
  rememberMe: false
})

const loading = ref(false)
const error = ref('')

async function handleLogin() {
  if (!credentials.value.email || !credentials.value.password) {
    error.value = 'Please fill in all fields'
    return
  }

  loading.value = true
  error.value = ''

  try {
    await authStore.login({
      email: credentials.value.email,
      password: credentials.value.password
    })
    ElMessage.success(authStore.$t ? authStore.$t('auth.loginSuccess') : 'Login successful')
    router.push('/dashboard')
  } catch (err) {
    error.value = err.response?.data?.message || 'Login failed. Please try again.'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F9FAFB;
}

.login-container {
  width: 100%;
  max-width: 420px;
  padding: 24px;
}

.login-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.07), 0 2px 4px -1px rgba(0, 0, 0, 0.04);
  border: 1px solid #E5E7EB;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-img {
  height: 64px;
  width: auto;
  object-fit: contain;
  margin-bottom: 16px;
}

.login-header h1 {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #111111;
}

.subtitle {
  color: #6B7280;
  font-size: 14px;
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

.form-group input {
  padding: 10px 12px;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: #ffffff;
}

.form-group input:focus {
  outline: none;
  border-color: #F97316;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #374151;
}

.checkbox input {
  cursor: pointer;
  accent-color: #F97316;
}

.forgot-password {
  color: #F97316;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-password:hover {
  color: #EA6B0E;
}

.login-button {
  padding: 11px 16px;
  background: #F97316;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.2s;
  width: 100%;
}

.login-button:hover:not(:disabled) {
  background: #EA6B0E;
  transform: translateY(-1px);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 16px;
}

.login-footer a {
  color: #F97316;
  text-decoration: none;
}

.login-footer a:hover {
  text-decoration: underline;
}

.error-message {
  padding: 12px 16px;
  background: #FEE2E2;
  color: #991B1B;
  border-radius: 8px;
  font-size: 14px;
  border: 1px solid #FECACA;
}

@media (max-width: 480px) {
  .login-card {
    padding: 24px;
  }

  .login-header h1 {
    font-size: 20px;
  }
}
</style>
