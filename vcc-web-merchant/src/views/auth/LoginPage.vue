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
            <label>{{ $t('auth.username', '用户名') }}</label>
            <input 
              v-model="credentials.username" 
              type="text" 
              :placeholder="$t('auth.username', '请输入用户名')"
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

          <!-- RuoYi captcha -->
          <div class="form-group" v-if="captchaEnabled">
            <label>{{ $t('auth.captcha', '验证码') }}</label>
            <div class="captcha-row">
              <input 
                v-model="credentials.code" 
                type="text" 
                :placeholder="$t('auth.captcha', '请输入验证码')"
                autocomplete="off"
              />
              <img 
                :src="captchaImg" 
                class="captcha-img" 
                @click="refreshCaptcha"
                :title="$t('auth.refreshCaptcha', '点击刷新')"
              />
            </div>
          </div>

          <div class="form-options">
            <label class="checkbox">
              <input type="checkbox" v-model="credentials.rememberMe" />
              {{ $t('auth.rememberMe', '记住我') }}
            </label>
            <router-link to="/forgot-password" class="forgot-password">{{ $t('auth.forgotPassword', '忘记密码？') }}</router-link>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const credentials = ref({
  username: '',
  password: '',
  code: '',
  uuid: '',
  rememberMe: false
})

const loading = ref(false)
const error = ref('')
const captchaEnabled = ref(false)
const captchaImg = ref('')

async function refreshCaptcha() {
  try {
    const res = await authApi.getCaptcha()
    captchaEnabled.value = res.captchaEnabled !== false
    if (captchaEnabled.value) {
      captchaImg.value = 'data:image/gif;base64,' + res.img
      credentials.value.uuid = res.uuid
    }
  } catch (err) {
    console.error('Failed to load captcha:', err)
  }
}

async function handleLogin() {
  error.value = ''

  if (!credentials.value.username || !credentials.value.password) {
    error.value = '请填写所有必填项'
    return
  }

  if (captchaEnabled.value && !credentials.value.code) {
    error.value = '请输入验证码'
    return
  }

  loading.value = true

  try {
    await authStore.login({
      username: credentials.value.username,
      password: credentials.value.password,
      code: credentials.value.code || undefined,
      uuid: credentials.value.uuid || undefined
    })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (err) {
    error.value = err.response?.data?.msg || err.message || '登录失败，请检查用户名和密码。'
    ElMessage.error(error.value)
    // Refresh captcha on error
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #F5F5F7;
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

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
  background: #F97316;
  animation-delay: 0s;
}

.blob-2 {
  bottom: -20%;
  right: -10%;
  width: 600px;
  height: 600px;
  background: #8B5CF6;
  animation-delay: -4s;
}

.blob-3 {
  top: 30%;
  left: 50%;
  width: 400px;
  height: 400px;
  background: #3B82F6;
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

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-row input {
  flex: 1;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 14px;
  font-size: 15px;
  color: #1D1D1F;
  transition: all 0.3s ease;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.02);
}

.captcha-row input:focus {
  outline: none;
  border-color: #F97316;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.15);
}

.captcha-img {
  width: 120px;
  height: 48px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid rgba(0, 0, 0, 0.08);
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
