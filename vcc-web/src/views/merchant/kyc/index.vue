<template>
  <div class="app-container">
    <!-- KYC 状态概览 -->
    <el-row :gutter="16" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">当前 KYC 状态</div>
            <div class="stat-value">
              <el-tag :type="kycStatusTagType" size="large">{{ kycStatusLabel }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">提交时间</div>
            <div class="stat-value">{{ kycInfo.submitTime || '—' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">审核时间</div>
            <div class="stat-value">{{ kycInfo.reviewTime || '—' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">审核人</div>
            <div class="stat-value">{{ kycInfo.reviewer || '—' }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 拒绝原因 -->
    <el-alert v-if="kycInfo.kycStatus === 'REJECTED' && kycInfo.rejectReason" type="error" :title="'拒绝原因：' + kycInfo.rejectReason" show-icon :closable="false" class="mb8" />

    <!-- KYC 表单 -->
    <el-card class="mb8">
      <template #header>
        <span>企业 KYC 认证</span>
        <el-button v-if="canSubmit" type="primary" style="float: right" @click="handleSubmit">提交审核</el-button>
      </template>

      <el-form :model="kycForm" :rules="kycRules" ref="kycFormRef" label-width="140px" :disabled="!canEdit">
        <el-divider content-position="left">企业基本信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业名称" prop="companyName">
              <el-input v-model="kycForm.companyName" placeholder="请输入注册企业名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="统一社会信用代码" prop="businessLicenseNo">
              <el-input v-model="kycForm.businessLicenseNo" placeholder="请输入统一社会信用代码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业注册国家" prop="country">
              <el-input v-model="kycForm.country" placeholder="如 US / GB / SG" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业注册地址" prop="businessAddress">
              <el-input v-model="kycForm.businessAddress" placeholder="请输入企业注册地址" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">法人代表信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="法人姓名" prop="legalName">
              <el-input v-model="kycForm.legalName" placeholder="请输入法人代表姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="证件类型" prop="legalIdType">
              <el-select v-model="kycForm.legalIdType" placeholder="请选择">
                <el-option label="身份证" value="ID_CARD" />
                <el-option label="护照" value="PASSPORT" />
                <el-option label="驾照" value="DRIVER_LICENSE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="证件号码" prop="legalIdNumber">
              <el-input v-model="kycForm.legalIdNumber" placeholder="请输入证件号码" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">证件影像</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="营业执照" prop="businessLicenseImage">
              <image-upload v-model="kycForm.businessLicenseImage" :limit="1" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="证件正面" prop="idFrontImage">
              <image-upload v-model="kycForm.idFrontImage" :limit="1" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="证件反面" prop="idBackImage">
              <image-upload v-model="kycForm.idBackImage" :limit="1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="手持证件照" prop="selfieImage">
              <image-upload v-model="kycForm.selfieImage" :limit="1" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">补充问卷</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="主要业务类型" prop="businessType">
              <el-select v-model="kycForm.businessType" placeholder="请选择">
                <el-option label="电商" value="ECOMMERCE" />
                <el-option label="金融科技" value="FINTECH" />
                <el-option label="游戏/娱乐" value="GAMING" />
                <el-option label="SaaS/软件" value="SAAS" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计月交易额(USD)" prop="estimatedMonthlyVolume">
              <el-input-number v-model="kycForm.estimatedMonthlyVolume" :min="0" :step="10000" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getKycInfo, submitKyc } from '@/api/merchant/kyc'

const kycInfo = ref({})
const kycForm = ref({
  companyName: '',
  businessLicenseNo: '',
  country: '',
  businessAddress: '',
  legalName: '',
  legalIdType: '',
  legalIdNumber: '',
  businessLicenseImage: '',
  idFrontImage: '',
  idBackImage: '',
  selfieImage: '',
  businessType: '',
  estimatedMonthlyVolume: 0
})

const kycRules = {
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  businessLicenseNo: [{ required: true, message: '请输入统一社会信用代码', trigger: 'blur' }],
  country: [{ required: true, message: '请输入注册国家', trigger: 'blur' }],
  legalName: [{ required: true, message: '请输入法人姓名', trigger: 'blur' }],
  legalIdType: [{ required: true, message: '请选择证件类型', trigger: 'change' }],
  legalIdNumber: [{ required: true, message: '请输入证件号码', trigger: 'blur' }],
  businessLicenseImage: [{ required: true, message: '请上传营业执照', trigger: 'change' }],
  idFrontImage: [{ required: true, message: '请上传证件正面', trigger: 'change' }]
}

const kycFormRef = ref()

// KYC 状态枚举（对齐状态字典 kycStatus）
const kycStatusMap = {
  NOT_SUBMITTED: { label: '未提交', tag: 'info' },
  SUBMITTED: { label: '已提交', tag: 'warning' },
  UNDER_REVIEW: { label: '审核中', tag: 'warning' },
  APPROVED: { label: '已通过', tag: 'success' },
  REJECTED: { label: '已拒绝', tag: 'danger' }
}

const kycStatusLabel = computed(() => kycStatusMap[kycInfo.value.kycStatus]?.label || '未知')
const kycStatusTagType = computed(() => kycStatusMap[kycInfo.value.kycStatus]?.tag || 'info')

const canEdit = computed(() => ['NOT_SUBMITTED', 'REJECTED'].includes(kycInfo.value.kycStatus))
const canSubmit = computed(() => canEdit.value)

/** 获取 KYC 信息 */
function loadKycInfo() {
  getKycInfo().then(res => {
    kycInfo.value = res.data || {}
    if (res.data && res.data.kycForm) {
      kycForm.value = { ...kycForm.value, ...res.data.kycForm }
    }
  })
}

/** 提交 KYC 审核 */
function handleSubmit() {
  kycFormRef.value?.validate(valid => {
    if (!valid) return
    submitKyc(kycForm.value).then(() => {
      ElMessage.success('KYC 申请已提交，请等待审核')
      loadKycInfo()
    })
  })
}

loadKycInfo()
</script>

<style scoped>
.stat-card { text-align: center; padding: 8px 0; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 18px; font-weight: 600; }
</style>
