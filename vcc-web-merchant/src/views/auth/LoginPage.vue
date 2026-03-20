<template>
  <div class="login-page">
    <!-- Web3 风格流体渐变背景 -->
    <div class="blob blob-1"></div>
    <div class="blob blob-2"></div>
    <div class="blob blob-3"></div>

    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <img src="@/assets/logo/kimoox-logo-stacked.svg" alt="kimoox" class="logo-img" />
          <h1>{{ $t('common.login', '登录') }}</h1>
          <p class="subtitle">{{ $t('auth.portalSubtitle', '虚拟信用卡商户管理系统') }}</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label>{{ $t('auth.email', '邮箱地址') }}</label>
            <input 
              v-model="credentials.email" 
              type="email" 
              :placeholder="$t('auth.email', '请输入邮箱地址')"
              required
            />
          </div>

          <div class="form-group">
            <label>{{ $t('auth.password', '密码') }}</label>
            <input 
              v-model="credentials.password" 
              type="password" 
              :placeholder="$t('auth.password', '请输入密码')"
              required
            />
          </div>

          <div class="form-options">
            <label class="checkbox">
              <input type="checkbox" v-model="credentials.rememberMe" />
              {{ $t('auth.rememberMe', '记住我') }}
            </label>
            <a href="#" class="forgot-password">{{ $t('auth.forgotPassword', '忘记密码？') }}</a>
          </div>

          <button type="submit" class="login-button" :disabled="loading">
            <span v-if="loading" class="loader"></span>
            <span v-else>{{ $t('common.login', '登录') }}</span>
          </button>
        </form>

        <div class="login-footer">
          <p>{{ $t('auth.noAccount', '还没有账号？') }} <router-link to="/register">{{ $t('common.register', '立即注册') }}</router-link></p>
        </div>

        <div v-if="error" class="error-message">
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
          {{ error }}
        </div>
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
  error.value = ''

  if (!credentials.value.email || !credentials.value.password) {
    error.value = '请填写所有必填项'
    return
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(credentials.value.email)) {
    error.value = '请输入有效的邮箱地址'
    return
  }

  if (credentials.value.password.length < 6) {
    error.value = '密码长度至少为 6 位'
    return
  }

  loading.value = true

  try {
    await authStore.login({
      email: credentials.value.email,
      password: credentials.value.password
    })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (err) {
    error.value = err.response?.data?.message || '登录失败，请检查您的邮箱和密码。'
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
  background-color: #F5F5F7; /* Apple Light Gray */
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

/* Web3 风格流体背景光晕 */
.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.5;
  z-index: 0;
  animation: float 12s infinite ease-in-out alternate;
}

.blob-1 {
  top: -10%;
  left: -10%;
  width: 500px;
  height: 500px;
  background: #F97316; /* Kimoox Orange */
  animation-delay: 0s;
}

.blob-2 {
  bottom: -20%;
  right: -10%;
  width: 600px;
  height: 600px;
  background: #8B5CF6; /* Web3 Purple */
  animation-delay: -4s;
}

.blob-3 {
  top: 30%;
  left: 50%;
  width: 400px;
  height: 400px;
  background: #3B82F6; /* Tech Blue */
  animation-delay: -8s;
}

@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(40px, 60px) scale(1.1); }
}

.login-container {
  width: 100%;
  max-width: 420px;
  padding: 24px;
  position: relative;
  z-index: 1;
}

/* Apple 风格极致毛玻璃卡片 */
.login-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(40px);
  -webkit-backdrop-filter: blur(40px);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 
    0 24px 48px rgba(0, 0, 0, 0.06), 
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-img {
  height: 64px;
  width: auto;
  object-fit: contain;
  margin-bottom: 24px;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.05));
}

.login-header h1 {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #1D1D1F;
  letter-spacing: -0.5px;
}

.subtitle {
  color: #86868B;
  font-size: 15px;
  margin: 0;
  font-weight: 500;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 32px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-size: 13px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 8px;
  margin-left: 4px;
}

.form-group input {
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 14px;
  font-size: 15px;
  color: #1D1D1F;
  transition: all 0.3s ease;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.02);
}

.form-group input::placeholder {
  color: #86868B;
}

.form-group input:focus {
  outline: none;
  border-color: #F97316;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.15), inset 0 2px 4px rgba(0, 0, 0, 0.02);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  margin-top: -8px;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #515154;
  font-weight: 500;
  user-select: none;
}

.checkbox input {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: #F97316;
  border-radius: 4px;
}

.forgot-password {
  color: #F97316;
  text-decoration: none;
  transition: all 0.2s;
  font-weight: 600;
}

.forgot-password:hover {
  color: #EA6B0E;
}

/* Web3 风格渐变按钮 */
.login-button {
  padding: 16px 20px;
  background: linear-gradient(135deg, #F97316 0%, #FF9D42 100%);
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
  box-shadow: 0 8px 20px rgba(249, 115, 22, 0.25);
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 54px;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(249, 115, 22, 0.35);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loader {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #86868B;
  font-weight: 500;
}

.login-footer a {
  color: #F97316;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: color 0.2s;
}

.login-footer a:hover {
  color: #EA6B0E;
}

.error-message {
  margin-top: 24px;
  padding: 12px 16px;
  background: rgba(255, 59, 48, 0.1);
  color: #FF3B30;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  animation: shake 0.4s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

@media (max-width: 480px) {
  .login-card {
    padding: 40px 24px;
    border-radius: 20px;
  }

  .login-header h1 {
    font-size: 24px;
  }

  .logo-img {
    height: 56px;
  }
}
</style>
