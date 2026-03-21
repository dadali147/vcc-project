<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1>{{ t('cardApplication.title') }}</h1>
        <p>{{ t('cardApplication.pageDescription') }}</p>
      </div>
    </div>

    <!-- Steps Indicator -->
    <section class="panel">
      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step :title="t('cardApplication.step1')" />
        <el-step :title="t('cardApplication.step2')" />
        <el-step :title="t('cardApplication.step3')" />
        <el-step :title="t('cardApplication.step4')" />
      </el-steps>
    </section>

    <!-- Step 1: Select Cardholder -->
    <section v-if="currentStep === 0" class="panel">
      <div class="panel-header">
        <h2>{{ t('cardApplication.step1') }}</h2>
      </div>
      <div v-if="loadingHolders" class="loading-state">{{ t('common.loading') }}</div>
      <div v-else class="holder-grid">
        <div
          v-for="holder in cardholders"
          :key="holder.id"
          class="holder-card"
          :class="{ selected: form.cardholderId === holder.id }"
          @click="form.cardholderId = holder.id; form.cardholderName = holder.name"
        >
          <div class="holder-avatar">{{ holder.name ? holder.name[0].toUpperCase() : '?' }}</div>
          <div class="holder-info">
            <div class="holder-name">{{ holder.name }}</div>
            <div class="holder-email">{{ holder.email }}</div>
            <div class="holder-status" :class="holder.status">{{ holder.status }}</div>
          </div>
          <div v-if="form.cardholderId === holder.id" class="check-icon">✓</div>
        </div>
      </div>
      <div v-if="!loadingHolders && !cardholders.length" class="empty-state">
        <p>{{ t('cardholders.noCardholders') }}</p>
        <button class="primary-button" @click="$router.push('/cardholders')">{{ t('cardholders.add') }}</button>
      </div>
    </section>

    <!-- Step 2: Card Type & BIN -->
    <section v-if="currentStep === 1" class="panel">
      <div class="panel-header">
        <h2>{{ t('cardApplication.step2') }}</h2>
      </div>
      <el-form :model="form" :rules="step2Rules" ref="step2FormRef" label-position="top">
        <div class="form-grid">
          <el-form-item :label="t('cardApplication.cardType')" prop="cardType">
            <div class="type-selector">
              <div
                class="type-card"
                :class="{ selected: form.cardType === 'code' }"
                @click="form.cardType = 'code'"
              >
                <div class="type-icon">💳</div>
                <div class="type-label">{{ t('cards.cardTypeCode') }}</div>
                <div class="type-desc">适用于广告、订阅等消费场景</div>
              </div>
              <div
                class="type-card"
                :class="{ selected: form.cardType === 'budget' }"
                @click="form.cardType = 'budget'"
              >
                <div class="type-icon">📊</div>
                <div class="type-label">{{ t('cards.cardTypeBudget') }}</div>
                <div class="type-desc">适用于团队预算管理</div>
              </div>
            </div>
          </el-form-item>

          <el-form-item :label="t('cardApplication.cardBin')" prop="cardBin">
            <el-select v-model="form.cardBin" :placeholder="t('cardApplication.selectCardBin')" style="width:100%">
              <el-option label="VCC-US-001 (美国)" value="VCC-US-001" />
              <el-option label="VCC-HK-002 (香港)" value="VCC-HK-002" />
              <el-option label="VCC-SG-003 (新加坡)" value="VCC-SG-003" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
    </section>

    <!-- Step 3: Limits & Purpose -->
    <section v-if="currentStep === 2" class="panel">
      <div class="panel-header">
        <h2>{{ t('cardApplication.step3') }}</h2>
      </div>
      <el-form :model="form" :rules="step3Rules" ref="step3FormRef" label-position="top">
        <div class="form-grid">
          <el-form-item :label="t('cardApplication.cardDailyLimit')" prop="dailyLimit">
            <el-input
              v-model.number="form.dailyLimit"
              type="number"
              :placeholder="t('cardApplication.dailyLimitPlaceholder')"
            >
              <template #prepend>$</template>
            </el-input>
          </el-form-item>
          <el-form-item :label="t('cardApplication.cardMonthlyLimit')" prop="monthlyLimit">
            <el-input
              v-model.number="form.monthlyLimit"
              type="number"
              :placeholder="t('cardApplication.monthlyLimitPlaceholder')"
            >
              <template #prepend>$</template>
            </el-input>
          </el-form-item>
          <el-form-item :label="t('cardApplication.purpose')" prop="purpose">
            <el-input
              v-model="form.purpose"
              :placeholder="t('cardApplication.purposePlaceholder')"
            />
          </el-form-item>
          <el-form-item :label="t('transactions.description')" class="full-width">
            <el-input
              v-model="form.remark"
              type="textarea"
              :rows="4"
              :placeholder="t('cardApplication.remarkPlaceholder')"
            />
          </el-form-item>
        </div>
      </el-form>
    </section>

    <!-- Step 4: Review & Submit -->
    <section v-if="currentStep === 3" class="panel">
      <div class="panel-header">
        <h2>{{ t('cardApplication.reviewTitle') }}</h2>
      </div>
      <div class="review-grid">
        <div class="review-item">
          <span class="review-label">{{ t('cardApplication.cardholder') }}</span>
          <span class="review-value">{{ form.cardholderName }}</span>
        </div>
        <div class="review-item">
          <span class="review-label">{{ t('cardApplication.cardType') }}</span>
          <span class="review-value">{{ form.cardType === 'code' ? t('cards.cardTypeCode') : t('cards.cardTypeBudget') }}</span>
        </div>
        <div class="review-item">
          <span class="review-label">{{ t('cardApplication.cardBin') }}</span>
          <span class="review-value">{{ form.cardBin }}</span>
        </div>
        <div class="review-item">
          <span class="review-label">{{ t('cardApplication.cardDailyLimit') }}</span>
          <span class="review-value">${{ form.dailyLimit }}</span>
        </div>
        <div class="review-item">
          <span class="review-label">{{ t('cardApplication.cardMonthlyLimit') }}</span>
          <span class="review-value">${{ form.monthlyLimit }}</span>
        </div>
        <div class="review-item">
          <span class="review-label">{{ t('cardApplication.purpose') }}</span>
          <span class="review-value">{{ form.purpose }}</span>
        </div>
        <div v-if="form.remark" class="review-item full-width">
          <span class="review-label">{{ t('transactions.description') }}</span>
          <span class="review-value">{{ form.remark }}</span>
        </div>
      </div>

      <div class="checklist">
        <h3>{{ t('cardApplication.applicationChecklist') }}</h3>
        <div class="check-item">
          <span class="check-icon success">✓</span>
          {{ t('cardApplication.checkKyc') }}
        </div>
        <div class="check-item">
          <span class="check-icon success">✓</span>
          {{ t('cardApplication.checkBalance') }}
        </div>
        <div class="check-item">
          <span class="check-icon success">✓</span>
          {{ t('cardApplication.checkCardholder') }}
        </div>
      </div>
    </section>

    <!-- Navigation Buttons -->
    <div class="step-actions">
      <button v-if="currentStep > 0" class="secondary-button" @click="prevStep">
        {{ t('common.prevStep') }}
      </button>
      <button
        v-if="currentStep < 3"
        class="primary-button"
        @click="nextStep"
        :disabled="!canProceed"
      >
        {{ t('common.nextStep') }}
      </button>
      <button
        v-if="currentStep === 3"
        class="primary-button"
        @click="handleSubmit"
        :disabled="submitting"
      >
        <span v-if="submitting">{{ t('common.loading') }}</span>
        <span v-else>{{ t('cardApplication.submit') }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cardApi, cardholderApi } from '@/api'

const { t } = useI18n()
const router = useRouter()

const currentStep = ref(0)
const loadingHolders = ref(false)
const submitting = ref(false)
const cardholders = ref([])
const step2FormRef = ref(null)
const step3FormRef = ref(null)

const form = reactive({
  cardholderId: null,
  cardholderName: '',
  cardType: '',
  cardBin: '',
  dailyLimit: '',
  monthlyLimit: '',
  purpose: '',
  remark: ''
})

const step2Rules = {
  cardType: [{ required: true, message: t('cardApplication.selectCardType'), trigger: 'change' }],
  cardBin: [{ required: true, message: t('cardApplication.selectCardBin'), trigger: 'change' }]
}

const step3Rules = {
  dailyLimit: [
    { required: true, message: t('common.tip'), trigger: 'blur' },
    { type: 'number', min: 1, message: t('common.tip'), trigger: 'blur' }
  ],
  monthlyLimit: [
    { required: true, message: t('common.tip'), trigger: 'blur' },
    { type: 'number', min: 1, message: t('common.tip'), trigger: 'blur' }
  ],
  purpose: [{ required: true, message: t('cardApplication.purposePlaceholder'), trigger: 'blur' }]
}

const canProceed = computed(() => {
  if (currentStep.value === 0) return !!form.cardholderId
  if (currentStep.value === 1) return !!(form.cardType && form.cardBin)
  if (currentStep.value === 2) return !!(form.dailyLimit && form.monthlyLimit && form.purpose)
  return true
})

const nextStep = async () => {
  if (currentStep.value === 1 && step2FormRef.value) {
    const valid = await step2FormRef.value.validate().catch(() => false)
    if (!valid) return
  }
  if (currentStep.value === 2 && step3FormRef.value) {
    const valid = await step3FormRef.value.validate().catch(() => false)
    if (!valid) return
  }
  currentStep.value++
}

const prevStep = () => {
  currentStep.value--
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    await cardApi.apply({
      cardholderId: form.cardholderId,
      cardType: form.cardType,
      cardBin: form.cardBin,
      dailyLimit: Number(form.dailyLimit),
      monthlyLimit: Number(form.monthlyLimit),
      purpose: form.purpose,
      remark: form.remark
    })
    ElMessage.success(t('cardApplication.success'))
    router.push('/card-applications')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || t('cardApplication.error'))
  } finally {
    submitting.value = false
  }
}

const loadCardholders = async () => {
  loadingHolders.value = true
  try {
    const res = await cardholderApi.list({ page: 1, pageSize: 100 })
    cardholders.value = res.data?.items || res.data || []
  } catch (err) {
    ElMessage.error(t('cardApplication.loadHolderFailed'))
  } finally {
    loadingHolders.value = false
  }
}

onMounted(() => {
  loadCardholders()
})
</script>

<style scoped>
.page-shell { display: flex; flex-direction: column; gap: 20px; }
.page-header h1 { margin: 0; color: #111827; }
.page-header p { margin: 8px 0 0; color: #6b7280; }
.panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 16px; padding: 24px; }
.panel-header { margin-bottom: 20px; }
.panel-header h2 { margin: 0; color: #111827; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.full-width { grid-column: 1 / -1; }
.holder-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 12px; }
.holder-card {
  display: flex; align-items: center; gap: 12px;
  padding: 16px; border: 2px solid #e5e7eb; border-radius: 12px; cursor: pointer;
  transition: all 0.2s; position: relative;
}
.holder-card:hover { border-color: #2563eb; background: #f0f7ff; }
.holder-card.selected { border-color: #2563eb; background: #eff6ff; }
.holder-avatar {
  width: 40px; height: 40px; border-radius: 50%; background: #2563eb; color: white;
  display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 16px;
  flex-shrink: 0;
}
.holder-name { font-weight: 600; color: #111827; }
.holder-email { font-size: 12px; color: #6b7280; margin-top: 2px; }
.holder-status { font-size: 11px; margin-top: 4px; color: #059669; font-weight: 600; }
.check-icon { position: absolute; top: 8px; right: 8px; color: #2563eb; font-weight: 700; }
.type-selector { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.type-card {
  padding: 20px; border: 2px solid #e5e7eb; border-radius: 12px; cursor: pointer;
  text-align: center; transition: all 0.2s;
}
.type-card:hover { border-color: #2563eb; }
.type-card.selected { border-color: #2563eb; background: #eff6ff; }
.type-icon { font-size: 32px; margin-bottom: 8px; }
.type-label { font-weight: 600; color: #111827; margin-bottom: 4px; }
.type-desc { font-size: 12px; color: #6b7280; }
.review-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; margin-bottom: 24px; }
.review-item { display: flex; flex-direction: column; gap: 4px; }
.review-label { font-size: 12px; color: #6b7280; font-weight: 600; text-transform: uppercase; }
.review-value { font-size: 15px; color: #111827; font-weight: 500; }
.checklist { background: #f0fdf4; border-left: 4px solid #059669; border-radius: 8px; padding: 16px; }
.checklist h3 { margin: 0 0 12px; font-size: 14px; color: #065f46; }
.check-item { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #374151; margin-bottom: 8px; }
.check-icon.success { color: #059669; font-weight: 700; }
.step-actions { display: flex; justify-content: flex-end; gap: 12px; }
.primary-button, .secondary-button { height: 44px; border: none; border-radius: 8px; padding: 0 24px; cursor: pointer; font-weight: 600; font-size: 15px; }
.primary-button { background: #2563eb; color: #fff; }
.primary-button:disabled { opacity: 0.5; cursor: not-allowed; }
.secondary-button { background: #eef2ff; color: #4338ca; }
.empty-state { text-align: center; padding: 40px; color: #6b7280; }
.loading-state { padding: 40px; text-align: center; color: #6b7280; }
@media (max-width: 900px) {
  .form-grid, .review-grid, .type-selector { grid-template-columns: 1fr; }
}
</style>
