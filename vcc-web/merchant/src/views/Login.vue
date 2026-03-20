<template>
  <div class="min-h-screen bg-slate-50 flex flex-col justify-center items-center py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full sm:w-[420px] bg-white p-8 rounded-xl shadow-sm border border-slate-200">
      <div class="text-center mb-8">
        <h2 class="text-3xl font-bold text-slate-900 tracking-tight">Kimoox VCC</h2>
        <p class="mt-2 text-sm text-slate-500">Sign in to your account</p>
      </div>
      
      <el-form :model="form" @submit.prevent="handleLogin" :rules="rules" ref="loginForm" label-position="top">
        <el-form-item label="Email address" prop="email" class="mb-4">
          <el-input 
            v-model="form.email" 
            placeholder="you@example.com"
            size="large"
            class="form-input"
          />
        </el-form-item>
        
        <el-form-item label="Password" prop="password" class="mb-6">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="••••••••" 
            show-password
            size="large"
          />
        </el-form-item>

        <div class="flex items-center justify-between mb-6">
          <div class="text-sm">
            <a href="#" class="font-medium text-indigo-600 hover:text-indigo-500">
              Forgot your password?
            </a>
          </div>
        </div>

        <el-button 
          type="primary" 
          native-type="submit" 
          class="w-full h-10 rounded-lg bg-indigo-600 hover:bg-indigo-500 border-none"
          :loading="loading"
        >
          Sign in
        </el-button>
        
        <div class="mt-6 text-center text-sm">
          <span class="text-slate-500">Don't have an account? </span>
          <a href="#" class="font-medium text-indigo-600 hover:text-indigo-500">
            Sign up
          </a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loginForm = ref(null)
const loading = ref(false)

const form = reactive({
  email: '',
  password: ''
})

const rules = {
  email: [
    { required: true, message: 'Please input email address', trigger: 'blur' },
    { type: 'email', message: 'Please input correct email address', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: 'Please input password', trigger: 'blur' },
    { min: 6, max: 20, message: 'Length should be 6 to 20', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginForm.value) return
  await loginForm.value.validate((valid) => {
    if (valid) {
      loading.value = true
      setTimeout(() => {
        loading.value = false
        router.push('/dashboard')
      }, 500)
    }
  })
}
</script>

<style scoped>
:deep(.el-form-item__label) {
  @apply text-sm font-medium text-slate-700 pb-1 mb-0 leading-tight;
}
:deep(.el-input__wrapper) {
  @apply rounded-lg shadow-sm border-slate-300;
}
:deep(.el-input__wrapper.is-focus) {
  @apply ring-2 ring-indigo-500/20 border-indigo-500 shadow-none;
}
</style>
