<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1>{{ t('kyc.title') }}</h1>
        <p>{{ t('kyc.pageDescription') }}</p>
      </div>
    </div>

    <!-- Status Banner -->
    <div class="status-card" :class="kycStatus.toLowerCase()">
      <div class="status-icon">
        <span v-if="kycStatus === 'PENDING'">⏳</span>
        <span v-else-if="kycStatus === 'APPROVED'">✅</span>
        <span v-else-if="kycStatus === 'REJECTED'">❌</span>
        <span v-else>📋</span>
      </div>
      <div class="status-info">
        <h3>{{ t('kyc.status') }}: {{ kycStatusLabel }}</h3>
        <p v-if="rejectionReason" class="rejection-reason">{{ t('kyc.rejectionReason') }}: {{ rejectionReason }}</p>
      </div>
    </div>

    <!-- KYC Form (only when not approved) -->
    <section v-if="kycStatus !== 'APPROVED'" class="panel">
      <!-- KYC Type Selector -->
      <div class="type-selector">
        <button
          class="type-btn"
          :class="{ active: kycType === 'personal' }"
          @click="kycType = 'personal'"
        >
          👤 {{ t('kyc.personalKyc') }}
        </button>
        <button
          class="type-btn"
          :class="{ active: kycType === 'enterprise' }"
          @click="kycType = 'enterprise'"
        >
          🏢 {{ t('kyc.enterpriseKyc') }}
        </button>
      </div>

      <!-- Personal KYC -->
      <div v-if="kycType === 'personal'" class="upload-section">
        <h3>{{ t('kyc.personalKyc') }}</h3>
        <div class="upload-grid">
          <div class="upload-item">
            <div class="upload-label">{{ t('kyc.idFront') }} <span class="required">*</span></div>
            <div
              class="upload-area"
              :class="{ filled: personalForm.idFront }"
              @click="triggerUpload('idFront')"
            >
              <img v-if="personalForm.idFront" :src="personalForm.idFront" class="preview-image" />
              <div v-else class="upload-placeholder">
                <div class="upload-icon">📸</div>
                <div>{{ t('kyc.uploadHint') }}</div>
              </div>
            </div>
            <input ref="idFrontInput" type="file" accept="image/*" hidden @change="handleFileChange($event, 'personal', 'idFront')" />
          </div>

          <div class="upload-item">
            <div class="upload-label">{{ t('kyc.idBack') }} <span class="required">*</span></div>
            <div
              class="upload-area"
              :class="{ filled: personalForm.idBack }"
              @click="triggerUpload('idBack')"
            >
              <img v-if="personalForm.idBack" :src="personalForm.idBack" class="preview-image" />
              <div v-else class="upload-placeholder">
                <div class="upload-icon">📸</div>
                <div>{{ t('kyc.uploadHint') }}</div>
              </div>
            </div>
            <input ref="idBackInput" type="file" accept="image/*" hidden @change="handleFileChange($event, 'personal', 'idBack')" />
          </div>

          <div class="upload-item">
            <div class="upload-label">{{ t('kyc.faceSelfie') }} <span class="required">*</span></div>
            <div
              class="upload-area"
              :class="{ filled: personalForm.faceSelfie }"
              @click="triggerUpload('faceSelfie')"
            >
              <img v-if="personalForm.faceSelfie" :src="personalForm.faceSelfie" class="preview-image" />
              <div v-else class="upload-placeholder">
                <div class="upload-icon">🤳</div>
                <div>{{ t('kyc.uploadHint') }}</div>
              </div>
            </div>
            <input ref="faceSelfieInput" type="file" accept="image/*" hidden @change="handleFileChange($event, 'personal', 'faceSelfie')" />
          </div>
        </div>
      </div>

      <!-- Enterprise KYC -->
      <div v-else class="upload-section">
        <h3>{{ t('kyc.enterpriseKyc') }}</h3>
        <div class="upload-grid">
          <div class="upload-item">
            <div class="upload-label">{{ t('kyc.businessLicense') }} <span class="required">*</span></div>
            <div
              class="upload-area"
              :class="{ filled: enterpriseForm.businessLicense }"
              @click="triggerUpload('businessLicense')"
            >
              <img v-if="enterpriseForm.businessLicense" :src="enterpriseForm.businessLicense" class="preview-image" />
              <div v-else class="upload-placeholder">
                <div class="upload-icon">📄</div>
                <div>{{ t('kyc.uploadHint') }}</div>
              </div>
            </div>
            <input ref="businessLicenseInput" type="file" accept="image/*,application/pdf" hidden @change="handleFileChange($event, 'enterprise', 'businessLicense')" />
          </div>

          <div class="upload-item">
            <div class="upload-label">{{ t('kyc.legalRepId') }} <span class="required">*</span></div>
            <div
              class="upload-area"
              :class="{ filled: enterpriseForm.legalRepId }"
              @click="triggerUpload('legalRepId')"
            >
              <img v-if="enterpriseForm.legalRepId" :src="enterpriseForm.legalRepId" class="preview-image" />
              <div v-else class="upload-placeholder">
                <div class="upload-icon">🪪</div>
                <div>{{ t('kyc.uploadHint') }}</div>
              </div>
            </div>
            <input ref="legalRepIdInput" type="file" accept="image/*" hidden @change="handleFileChange($event, 'enterprise', 'legalRepId')" />
          </div>

          <div class="upload-item">
            <div class="upload-label">{{ t('kyc.bankStatement') }} <span class="required">*</span></div>
            <div
              class="upload-area"
              :class="{ filled: enterpriseForm.bankStatement }"
              @click="triggerUpload('bankStatement')"
            >
              <img v-if="enterpriseForm.bankStatement" :src="enterpriseForm.bankStatement" class="preview-image" />
              <div v-else class="upload-placeholder">
                <div class="upload-icon">🏦</div>
                <div>{{ t('kyc.uploadHint') }}</div>
              </div>
            </div>
            <input ref="bankStatementInput" type="file" accept="image/*,application/pdf" hidden @change="handleFileChange($event, 'enterprise', 'bankStatement')" />
          </div>
        </div>
      </div>

      <div class="form-actions">
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          @click="handleSubmit"
          :disabled="kycStatus === 'PENDING'"
        >
          {{ t('kyc.submitForReview') }}
        </el-button>
      </div>
    </section>

    <!-- Submitted Documents -->
    <section v-if="submittedDocuments.length" class="panel">
      <div class="panel-header">
        <h2>{{ t('kyc.submittedDocuments') }}</h2>
      </div>
      <div class="documents-list">
        <div v-for="doc in submittedDocuments" :key="doc.id" class="document-item">
          <div class="document-preview">
            <img v-if="doc.url" :src="doc.url" :alt="doc.name" />
            <div v-else class="no-preview">📄</div>
          </div>
          <div class="document-info">
            <p class="document-name">{{ doc.name }}</p>
            <p class="document-date">{{ doc.uploadedAt }}</p>
            <span class="document-status" :class="(doc.status || '').toLowerCase()">{{ doc.status }}</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { kycApi } from '@/api'

const { t } = useI18n()

const submitting = ref(false)
const kycStatus = ref('NOT_SUBMITTED')
const rejectionReason = ref('')
const submittedDocuments = ref([])
const kycType = ref('personal')

// File input refs
const idFrontInput = ref(null)
const idBackInput = ref(null)
const faceSelfieInput = ref(null)
const businessLicenseInput = ref(null)
const legalRepIdInput = ref(null)
const bankStatementInput = ref(null)

const fileInputMap = {
  idFront: () => idFrontInput.value,
  idBack: () => idBackInput.value,
  faceSelfie: () => faceSelfieInput.value,
  businessLicense: () => businessLicenseInput.value,
  legalRepId: () => legalRepIdInput.value,
  bankStatement: () => bankStatementInput.value,
}

const personalForm = reactive({
  idFront: '',
  idBack: '',
  faceSelfie: ''
})

// Raw file objects for upload
const personalFiles = reactive({
  idFront: null,
  idBack: null,
  faceSelfie: null
})

const enterpriseForm = reactive({
  businessLicense: '',
  legalRepId: '',
  bankStatement: ''
})

const enterpriseFiles = reactive({
  businessLicense: null,
  legalRepId: null,
  bankStatement: null
})

const kycStatusLabel = computed(() => {
  const labels = {
    'NOT_SUBMITTED': t('kyc.statusNotSubmitted'),
    'PENDING': t('kyc.statusPending'),
    'APPROVED': t('kyc.statusApproved'),
    'REJECTED': t('kyc.statusRejected')
  }
  return labels[kycStatus.value] || kycStatus.value
})

const triggerUpload = (field) => {
  const inputFn = fileInputMap[field]
  if (inputFn) {
    inputFn()?.click()
  }
}

const handleFileChange = (event, type, field) => {
  const file = event.target.files?.[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => {
    if (type === 'personal') {
      personalForm[field] = e.target.result
      personalFiles[field] = file
    } else {
      enterpriseForm[field] = e.target.result
      enterpriseFiles[field] = file
    }
  }
  reader.readAsDataURL(file)
  // Reset input so same file can be re-selected
  event.target.value = ''
}

const handleSubmit = async () => {
  if (kycStatus.value === 'PENDING') {
    ElMessage.warning(t('kyc.alreadyPending'))
    return
  }

  // Validate required files
  if (kycType.value === 'personal') {
    if (!personalFiles.idFront || !personalFiles.idBack || !personalFiles.faceSelfie) {
      ElMessage.warning(t('kyc.uploadAllRequired'))
      return
    }
  } else {
    if (!enterpriseFiles.businessLicense || !enterpriseFiles.legalRepId || !enterpriseFiles.bankStatement) {
      ElMessage.warning(t('kyc.uploadAllRequired'))
      return
    }
  }

  submitting.value = true
  try {
    const formData = new FormData()
    formData.append('kycType', kycType.value)

    if (kycType.value === 'personal') {
      formData.append('idFront', personalFiles.idFront)
      formData.append('idBack', personalFiles.idBack)
      formData.append('faceSelfie', personalFiles.faceSelfie)
    } else {
      formData.append('businessLicense', enterpriseFiles.businessLicense)
      formData.append('legalRepId', enterpriseFiles.legalRepId)
      formData.append('bankStatement', enterpriseFiles.bankStatement)
    }

    await kycApi.uploadDocument(formData)
    await kycApi.submit()

    ElMessage.success(t('kyc.submitSuccess'))
    loadKycStatus()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || t('kyc.submitError'))
  } finally {
    submitting.value = false
  }
}

const loadKycStatus = async () => {
  try {
    const res = await kycApi.getStatus()
    kycStatus.value = res.data?.status || res.status || 'NOT_SUBMITTED'
    rejectionReason.value = res.data?.rejectionReason || res.rejectionReason || ''
  } catch (err) {
    console.error('Failed to load KYC status:', err)
  }

  try {
    const docs = await kycApi.getDocuments()
    submittedDocuments.value = docs.data?.items || docs.data || []
  } catch (err) {
    console.error('Failed to load KYC documents:', err)
  }
}

onMounted(() => {
  loadKycStatus()
})
</script>

<style scoped>
.page-shell { display: flex; flex-direction: column; gap: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-header h1 { margin: 0; color: #111827; }
.page-header p { margin: 8px 0 0; color: #6b7280; }
.status-card {
  display: flex; align-items: center; gap: 20px; padding: 20px; border-radius: 12px;
  border-left: 4px solid; background: #f3f4f6; border-left-color: #6b7280;
}
.status-card.pending { background: #fffbeb; border-left-color: #f59e0b; }
.status-card.approved { background: #f0fdf4; border-left-color: #10b981; }
.status-card.rejected { background: #fef2f2; border-left-color: #ef4444; }
.status-icon { font-size: 32px; }
.status-info h3 { margin: 0 0 4px; font-size: 16px; color: #111827; }
.status-info p { margin: 0; font-size: 14px; color: #6b7280; }
.rejection-reason { color: #991b1b; font-weight: 500; }
.panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 16px; padding: 24px; }
.panel-header { margin-bottom: 16px; }
.panel-header h2 { margin: 0; color: #111827; }
.type-selector { display: flex; gap: 12px; margin-bottom: 24px; }
.type-btn {
  flex: 1; padding: 12px 20px; border: 2px solid #e5e7eb; border-radius: 10px;
  background: white; cursor: pointer; font-weight: 600; color: #6b7280;
  transition: all 0.2s; font-size: 14px;
}
.type-btn.active { border-color: #2563eb; color: #2563eb; background: #eff6ff; }
.upload-section h3 { margin: 0 0 16px; color: #374151; }
.upload-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
.upload-item { display: flex; flex-direction: column; gap: 8px; }
.upload-label { font-size: 14px; font-weight: 600; color: #374151; }
.required { color: #ef4444; }
.upload-area {
  height: 180px; border: 2px dashed #d1d5db; border-radius: 10px; display: flex;
  align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s;
  overflow: hidden; background: #fafafa;
}
.upload-area:hover { border-color: #2563eb; background: #f0f7ff; }
.upload-area.filled { border-style: solid; border-color: #10b981; }
.upload-placeholder { text-align: center; color: #9ca3af; font-size: 13px; }
.upload-icon { font-size: 32px; margin-bottom: 8px; }
.preview-image { width: 100%; height: 100%; object-fit: cover; }
.no-preview { font-size: 40px; display: flex; align-items: center; justify-content: center; }
.form-actions { display: flex; justify-content: flex-end; }
.documents-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; }
.document-item { border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden; }
.document-preview { height: 130px; background: #f3f4f6; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.document-preview img { width: 100%; height: 100%; object-fit: cover; }
.document-info { padding: 10px; }
.document-name { margin: 0 0 4px; font-size: 13px; font-weight: 500; color: #111827; }
.document-date { margin: 0 0 6px; font-size: 11px; color: #6b7280; }
.document-status { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 500; }
.document-status.pending { background: #fef3c7; color: #92400e; }
.document-status.approved { background: #d1fae5; color: #065f46; }
.document-status.rejected { background: #fee2e2; color: #991b1b; }
@media (max-width: 768px) {
  .upload-grid { grid-template-columns: 1fr; }
  .type-selector { flex-direction: column; }
}
</style>
