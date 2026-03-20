<template>
  <div class="page">
    <h1>{{ $t('kyc.title') }}</h1>
    
    <div class="kyc-container">
      <!-- KYC Status Card -->
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

      <!-- Upload Section -->
      <div v-if="kycStatus !== 'APPROVED'" class="upload-section">
        <h3>{{ $t('kyc.uploadDocuments') }}</h3>
        <div class="upload-grid">
          <div class="upload-item">
            <label>{{ $t('kyc.idFront') }}</label>
            <div class="upload-area">
              <input type="file" accept="image/*" @change="handleFileUpload">
              <span>📸 {{ $t('kyc.dragOrClick') }}</span>
            </div>
          </div>
          <div class="upload-item">
            <label>{{ $t('kyc.idBack') }}</label>
            <div class="upload-area">
              <input type="file" accept="image/*" @change="handleFileUpload">
              <span>📸 {{ $t('kyc.dragOrClick') }}</span>
            </div>
          </div>
          <div class="upload-item">
            <label>{{ $t('kyc.faceSelfie') }}</label>
            <div class="upload-area">
              <input type="file" accept="image/*" @change="handleFileUpload">
              <span>📸 {{ $t('kyc.dragOrClick') }}</span>
            </div>
          </div>
        </div>
        <button class="submit-button">{{ $t('kyc.submitForReview') }}</button>
      </div>

      <!-- Document Review Section -->
      <div v-if="submittedDocuments.length" class="review-section">
        <h3>{{ $t('kyc.submittedDocuments') }}</h3>
        <div class="documents-list">
          <div v-for="doc in submittedDocuments" :key="doc.id" class="document-item">
            <div class="document-preview">
              <img :src="doc.url" :alt="doc.name">
            </div>
            <div class="document-info">
              <p class="document-name">{{ doc.name }}</p>
              <p class="document-date">{{ formatDate(doc.uploadedAt) }}</p>
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
import { ref, computed } from 'vue'
import { formatDate } from '@/utils/common'

const kycStatus = ref('PENDING') // PENDING, APPROVED, REJECTED
const rejectionReason = ref('')

const submittedDocuments = ref([
  {
    id: '1',
    name: 'ID Front',
    url: '/placeholder.png',
    uploadedAt: new Date(),
    status: 'PENDING'
  }
])

const kycStatusLabel = computed(() => {
  const labels = {
    'PENDING': 'Pending Review',
    'APPROVED': 'Approved',
    'REJECTED': 'Rejected'
  }
  return labels[kycStatus.value] || 'Unknown'
})

function handleFileUpload(event) {
  console.log('File uploaded:', event.target.files[0])
  // Handle file upload logic
}
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
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.upload-item label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  border: 2px dashed #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  background: #f9fafb;
}

.upload-area:hover {
  border-color: #3B82F6;
  background: #DBEAFE;
}

.upload-area input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.upload-area span {
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

.submit-button {
  width: 100%;
  padding: 10px 16px;
  background: #3B82F6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-button:hover {
  background: #2563EB;
}

.review-section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.review-section h3 {
  margin: 0 0 20px 0;
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
</style>
