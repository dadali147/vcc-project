<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <img src="@/assets/logo/kimoox-logo-stacked.svg" alt="kimoox" class="login-logo" />
        <h1 class="login-title">kimoox Admin</h1>
        <p class="login-subtitle">Virtual Card Control Platform</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="admin@example.com" size="large" />
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="form.password" type="password" placeholder="Enter password" size="large" @keyup.enter="handleLogin" show-password />
        </el-form-item>
        <el-form-item style="margin-top: 24px">
          <el-button class="login-btn" :loading="loading" @click="handleLogin" size="large" style="width: 100%">Sign In</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="login-footer">
      <p>Powered by kimoox Technology</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  email: '',
  password: ''
})

const rules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.login(form.email, form.password)
        ElMessage.success('登录成功')
        router.push('/dashboard')
      } catch (error) {
        // error handled in interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 25%, #334155 50%, #1e293b 75%, #0f172a 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(249, 115, 22, 0.1) 0%, transparent 70%);
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.login-card {
  width: 440px;
  padding: 48px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 24px;
  border: 1px solid rgba(249, 115, 22, 0.2);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.4), 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-logo {
  height: 72px;
  width: auto;
  display: block;
  margin: 0 auto 16px;
  object-fit: contain;
  filter: drop-shadow(0 4px 12px rgba(249, 115, 22, 0.3));
}

.login-title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.5px;
}

.login-subtitle {
  margin: 0;
  font-size: 14px;
  color: #6B7280;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.login-btn {
  background: linear-gradient(135deg, #F97316 0%, #EA580C 100%);
  border: none;
  color: #fff;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
}

.login-btn:hover {
  background: linear-gradient(135deg, #EA580C 0%, #C2410C 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(249, 115, 22, 0.4);
}

.login-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.3);
}

.login-footer {
  margin-top: 24px;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  position: relative;
  z-index: 1;
}

.login-footer p {
  margin: 0;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1), 0 0 0 1px #F97316 inset;
}

:deep(.el-form-item__label) {
  font-size: 14px;
  color: #374151;
  font-weight: 600;
}
</style>
