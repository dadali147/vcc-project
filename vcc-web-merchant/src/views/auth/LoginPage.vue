<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <img src="@/assets/logo/kimoox-logo-text.svg" alt="kimoox" class="logo-img" />
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-page::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: drift 20s linear infinite;
}

@keyframes drift {
  from { transform: translate(0, 0); }
  to { transform: translate(50px, 50px); }
}

.login-container {
  width: 100%;
  max-width: 440px;
  padding: 24px;
  position: relative;
  z-index: 1;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 48px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-img {
  height: 48px;
  width: auto;
  object-fit: contain;
  margin-bottom: 24px;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.1));
}

.login-header h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #111111;
  letter-spacing: -0.5px;
}

.subtitle {
  color: #6B7280;
  font-size: 15px;
  margin: 0;
  font-weight: 500;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 28px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.form-group input {
  padding: 12px 16px;
  border: 2px solid #E5E7EB;
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.3s ease;
  background: #ffffff;
  font-weight: 500;
}

.form-group input:focus {
  outline: none;
  border-color: #F97316;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1);
  transform: translateY(-1px);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  margin-top: -4px;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #4B5563;
  font-weight: 500;
  user-select: none;
}

.checkbox input {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #F97316;
}

.forgot-password {
  color: #F97316;
  text-decoration: none;
  transition: all 0.2s;
  font-weight: 600;
}

.forgot-password:hover {
  color: #EA6B0E;
  text-decoration: underline;
}

.login-button {
  padding: 14px 20px;
  background: linear-gradient(135deg, #F97316 0%, #EA6B0E 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
  letter-spacing: 0.3px;
}

.login-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #EA6B0E 0%, #DC5F0A 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(249, 115, 22, 0.4);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 16px;
  font-weight: 500;
}

.login-footer a {
  color: #F97316;
  text-decoration: none;
  font-weight: 700;
  transition: color 0.2s;
}

.login-footer a:hover {
  color: #EA6B0E;
  text-decoration: underline;
}

.error-message {
  padding: 14px 18px;
  background: #FEE2E2;
  color: #991B1B;
  border-radius: 10px;
  font-size: 14px;
  border: 1px solid #FECACA;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(153, 27, 27, 0.1);
}

@media (max-width: 480px) {
  .login-card {
    padding: 32px 24px;
  }

  .login-header h1 {
    font-size: 24px;
  }

  .logo-img {
    height: 40px;
  }
}
</style>
