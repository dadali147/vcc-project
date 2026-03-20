<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <img src="@/assets/logo/kimoox-logo-stacked.svg" alt="kimoox" class="login-logo" />
        <p class="login-subtitle">管理后台</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="admin@example.com" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" @keyup.enter="handleLogin" show-password />
        </el-form-item>
        <el-form-item style="margin-top: 8px">
          <el-button class="login-btn" :loading="loading" @click="handleLogin" size="large" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>
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
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #1e1e2e 0%, #2d1b4e 50%, #1a2744 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  height: 56px;
  width: auto;
  display: block;
  margin: 0 auto 12px;
  object-fit: contain;
}

.login-subtitle {
  margin: 0;
  font-size: 15px;
  color: #6B7280;
  font-weight: 500;
}

.login-btn {
  background-color: #F97316;
  border-color: #F97316;
  color: #fff;
  font-weight: 600;
  border-radius: 8px;
  transition: background-color 0.2s, border-color 0.2s;
}

.login-btn:hover {
  background-color: #ea6c0a;
  border-color: #ea6c0a;
}

.login-btn:active {
  background-color: #c2560a;
  border-color: #c2560a;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #F97316 inset;
}

:deep(.el-form-item__label) {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}
</style>
