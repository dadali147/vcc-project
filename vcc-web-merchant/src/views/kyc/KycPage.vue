<template>
  <div class="page">
    <h1>{{ $t('kyc.title') }}</h1>

    <div class="kyc-container">
      <div class="status-card" :class="kycStatus.toLowerCase()">
        <div class="status-icon">
          <span v-if="kycStatus === 'PENDING'">⏳</span>
          <span v-else-if="kycStatus === 'APPROVED'">✅</span>
          <span v-else-if="kycStatus === 'REJECTED'">❌</span>
          <span v-else>📋</span>
        </div>
        <div class="status-info">
          <h3>{{ $t('kyc.status') }}: {{ kycStatusLabel }}</h3>
          <p v-if="rejectionReason" class="rejection-reason">
            {{ $t('kyc.rejectionReason') }}: {{ rejectionReason }}
          </p>
        </div>
      </div>

      <div v-if="kycStatus !== 'APPROVED'" class="upload-section">
        <h3>{{ $t('kyc.uploadDocuments') }}</h3>
        <el-form :model="form" :rules="rules" ref="formRef">
          <div class="upload-grid">
            <el-form-item label="身份证正面" prop="idFront">
              <el-upload
                class="upload-area"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="(file) => handleFileChange(file, 'idFront')"
                accept="image/*"
              >
                <div v-if="!form.idFront" class="upload-placeholder">
                  <span>📸 点击上传</span>
                </div>
                <img v-else :src="form.idFront" class="preview-image" />
              </el-upload>
            </el-form-item>

            <el-form-item label="身份证背面" prop="idBack">
              <el-upload
                class="upload-area"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="(file) => handleFileChange(file, 'idBack')"
                accept="image/*"
              >
                <div v-if="!form.idBack" class="upload-placeholder">
                  <span>📸 点击上传</span>
                </div>
                <img v-else :src="form.idBack" class="preview-image" />
              </el-upload>
            </el-form-item>

            <el-form-item label="手持身份证照片" prop="faceSelfie">
              <el-upload
                class="upload-area"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="(file) => handleFileChange(file, 'faceSelfie')"
                accept="image/*"
              >
                <div v-if="!form.faceSelfie" class="upload-placeholder">
                  <span>📸 点击上传</span>
                </div>
                <img v-else :src="form.faceSelfie" class="preview-image" />
              </el-upload>
            </el-form-item>
          </div>
        </el-form>
        <el-button type="primary" @click="handleSubmit" :loading="submitting" class="submit-button">
          {{ $t('kyc.submitForReview') }}
        </el-button>
      </div>

      <div v-if="submittedDocuments.length" class="review-section">
        <h3>{{ $t('kyc.submittedDocuments') }}</h3>
        <div class="documents-list">
          <div v-for="doc in submittedDocuments" :key="doc.id" class="document-item">
            <div class="document-preview">
              <img :src="doc.url" :alt="doc.name">
            </div>
            <div class="document-info">
              <p class="document-name">{{ doc.name }}</p>
              <p class="document-date">{{ doc.uploadedAt }}</p>
              <span class="document-status" :class="doc.status.toLowerCase()">
                {{ doc.status }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { kycApi } from '@/api'

const submitting = ref(false)
const formRef = ref(null)
const kycStatus = ref('NOT_SUBMITTED')
const rejectionReason = ref('')
const submittedDocuments = ref([])

const form = reactive({
  idFront: '',
  idBack: '',
  faceSelfie: ''
})

const rules = {
  idFront: [{ required: true, message: '请上传身份证正面', trigger: 'change' }],
  idBack: [{ required: true, message: '请上传身份证背面', trigger: 'change' }],
  faceSelfie: [{ required: true, message: '请上传手持身份证照片', trigger: 'change' }]
}

const kycStatusLabel = computed(() => {
  const labels = {
    'NOT_SUBMITTED': '未提交',
    'PENDING': '审核中',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return labels[kycStatus.value] || '未知'
})

const handleFileChange = (file, field) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    form[field] = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const formData = new FormData()

      for (const [key, value] of Object.entries(form)) {
        if (value) {
          const blob = await fetch(value).then(r => r.blob())
          formData.append(key, blob, `${key}.jpg`)
        }
      }

      await kycApi.uploadDocument(formData)
      await kycApi.submit()

      ElMessage.success('提交成功，等待审核')
      loadKycStatus()
    } catch (err) {
      ElMessage.error(err.response?.data?.message || '提交失败')
    } finally {
      submitting.value = false
    }
  })
}

const loadKycStatus = async () => {
  try {
    const res = await kycApi.getStatus()
    kycStatus.value = res.status || 'NOT_SUBMITTED'
    rejectionReason.value = res.rejectionReason || ''

    const docs = await kycApi.getDocuments()
    submittedDocuments.value = docs.data || []
  } catch (err) {
    console.error('Failed to load KYC status:', err)
  }
}

onMounted(() => {
  loadKycStatus()
})
</script>

<style scoped>
.page {
  padding: 24px;
}

.page h1 {
  margin-bottom: 32px;
  color: #111827;
}

.kyc-container {
  max-width: 800px;
}

.status-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 32px;
  border-left: 4px solid;
}

.status-card.not_submitted {
  background: #F3F4F6;
  border-left-color: #6B7280;
}

.status-card.pending {
  background: #FFFBEB;
  border-left-color: #F59E0B;
}

.status-card.approved {
  background: #F0FDF4;
  border-left-color: #10B981;
}

.status-card.rejected {
  background: #FEF2F2;
  border-left-color: #EF4444;
}

.status-icon {
  font-size: 32px;
  min-width: 48px;
  text-align: center;
}

.status-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #111827;
}

.status-info p {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.rejection-reason {
  color: #991B1B;
  font-weight: 500;
}

.upload-section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  margin-bottom: 32px;
}

.upload-section h3 {
  margin: 0 0 20px 0;
  color: #374151;
  font-size: 16px;
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.upload-area {
  width: 100%;
  height: 200px;
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  overflow: hidden;
}

.upload-area:hover {
  border-color: #2563eb;
  background: #f9fafb;
}

.upload-placeholder {
  text-align: center;
  color: #6b7280;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.submit-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.review-section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.review-section h3 {
  margin: 0 0 16px 0;
  color: #374151;
  font-size: 16px;
}

.documents-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.document-item {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
  transition: all 0.2s;
}

.document-item:hover {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.document-preview {
  width: 100%;
  height: 150px;
  overflow: hidden;
  background: #f3f4f6;
}

.document-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.document-info {
  padding: 12px;
}

.document-name {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.document-date {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #6b7280;
}

.document-status {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.document-status.pending {
  background: #FEF3C7;
  color: #92400E;
}

.document-status.approved {
  background: #D1FAE5;
  color: #065F46;
}

.document-status.rejected {
  background: #FEE2E2;
  color: #991B1B;
}

@media (max-width: 768px) {
  .upload-grid {
    grid-template-columns: 1fr;
  }
}
</style>
