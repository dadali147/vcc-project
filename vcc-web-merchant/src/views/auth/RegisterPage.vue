<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-card auth-card--wide">
        <div class="auth-header">
          <div class="logo">VCC</div>
          <h1>{{ t('common.register') }}</h1>
          <p class="subtitle">{{ t('auth.registerSubtitle') }}</p>
        </div>

        <form class="register-form" @submit.prevent="handleSubmit">
          <div class="form-grid">
            <div class="form-group">
              <label>{{ t('auth.companyName') }}</label>
              <input v-model="form.companyName" type="text" :placeholder="t('auth.companyNamePlaceholder')" />
            </div>
            <div class="form-group">
              <label>{{ t('auth.contactName') }}</label>
              <input v-model="form.contactName" type="text" :placeholder="t('auth.contactNamePlaceholder')" />
            </div>
            <div class="form-group">
              <label>{{ t('auth.email') }}</label>
              <input v-model="form.email" type="email" :placeholder="$t('auth.email')" />
            </div>
            <div class="form-group">
              <label>{{ t('auth.phone') }}</label>
              <input v-model="form.phone" type="text" :placeholder="t('auth.phonePlaceholder')" />
            </div>
            <div class="form-group">
              <label>{{ t('auth.password') }}</label>
              <input v-model="form.password" type="password" :placeholder="$t('auth.password')" />
            </div>
            <div class="form-group">
              <label>{{ t('auth.confirmPassword') }}</label>
              <input v-model="form.confirmPassword" type="password" :placeholder="$t('auth.password')" />
            </div>
          </div>

          <div class="form-section">
            <div class="form-group">
              <label>{{ t('auth.businessType') }}</label>
              <select v-model="form.businessType">
                <option value="">{{ t('auth.selectBusinessType') }}</option>
                <option value="ecommerce">{{ t('auth.businessTypeEcommerce') }}</option>
                <option value="saas">{{ t('auth.businessTypeSaas') }}</option>
                <option value="agency">{{ t('auth.businessTypeAgency') }}</option>
                <option value="other">{{ t('auth.businessTypeOther') }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>{{ t('auth.expectedMonthlySpend') }}</label>
              <select v-model="form.expectedMonthlySpend">
                <option value="">{{ t('auth.selectExpectedMonthlySpend') }}</option>
                <option value="lt10k">{{ $t('auth.spendLt10k', '< 10,000 USD') }}</option>
                <option value="10k-50k">{{ $t('auth.spend10k50k', '10,000 - 50,000 USD') }}</option>
                <option value="50k-100k">{{ $t('auth.spend50k100k', '50,000 - 100,000 USD') }}</option>
                <option value="gt100k">{{ $t('auth.spendGt100k', '> 100,000 USD') }}</option>
              </select>
            </div>
          </div>

          <label class="checkbox-row">
            <input v-model="form.agreement" type="checkbox" />
            <span>{{ t('auth.agreeTo') }} {{ t('auth.userAgreement') }} / {{ t('auth.privacyPolicy') }}</span>
          </label>

          <div v-if="error" class="error-message">{{ error }}</div>

          <div class="form-actions">
            <button type="button" class="secondary-button" @click="resetForm">{{ t('common.reset') }}</button>
            <button type="submit" class="primary-button" :disabled="loading">
              {{ loading ? t('common.loading') : t('common.register') }}
            </button>
          </div>
        </form>

        <div class="auth-footer">
          {{ t('auth.haveAccount') }}
          <router-link to="/login">{{ t('common.login') }}</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const error = ref('')
const form = reactive(getDefaultForm())

function getDefaultForm() {
  return {
    companyName: '',
    contactName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    businessType: '',
    expectedMonthlySpend: '',
    agreement: false
  }
}

function resetForm() {
  Object.assign(form, getDefaultForm())
  error.value = ''
}

async function handleSubmit() {
  error.value = ''

  if (!form.companyName || !form.contactName || !form.email || !form.password || !form.confirmPassword) {
    error.value = t('auth.fillRequiredFields')
    return
  }

  if (form.password !== form.confirmPassword) {
    error.value = t('auth.passwordMismatch')
    return
  }

  if (!form.agreement) {
    error.value = t('auth.acceptAgreement')
    return
  }

  loading.value = true
  try {
    await authStore.register({
      companyName: form.companyName,
      contactName: form.contactName,
      email: form.email,
      phone: form.phone,
      password: form.password,
      businessType: form.businessType,
      expectedMonthlySpend: form.expectedMonthlySpend
    })
    ElMessage.success(t('auth.registerSuccess'))
    router.push('/dashboard')
  } catch (err) {
    error.value = err.response?.data?.message || t('cardApplication.error')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
}

.auth-container {
  width: 100%;
  max-width: 920px;
}

.auth-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.18);
}

.auth-card--wide {
  max-width: 920px;
}

.auth-header {
  margin-bottom: 24px;
}

.logo {
  font-size: 32px;
  font-weight: 700;
  color: #3b82f6;
  margin-bottom: 12px;
}

.auth-header h1 {
  margin: 0 0 8px;
  font-size: 28px;
  color: #111827;
}

.subtitle {
  margin: 0;
  color: #6b7280;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-grid,
.form-section {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.form-group input,
.form-group select {
  height: 42px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
}

.checkbox-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  color: #4b5563;
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.primary-button,
.secondary-button {
  min-width: 120px;
  height: 42px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 600;
}

.primary-button {
  background: #2563eb;
  color: #fff;
}

.secondary-button {
  background: #eef2ff;
  color: #4338ca;
}

.primary-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.auth-footer {
  margin-top: 20px;
  text-align: center;
  color: #6b7280;
}

.auth-footer a {
  color: #2563eb;
  text-decoration: none;
}

.error-message {
  padding: 12px 14px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #b91c1c;
}

@media (max-width: 768px) {
  .auth-card {
    padding: 24px;
  }

  .form-grid,
  .form-section {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .primary-button,
  .secondary-button {
    width: 100%;
  }
}
</style>
