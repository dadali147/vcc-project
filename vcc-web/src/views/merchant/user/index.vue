<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧：用户基本信息 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
              <el-button type="primary" link icon="Edit" @click="handleEditProfile">修改</el-button>
            </div>
          </template>
          <el-descriptions :column="1">
            <el-descriptions-item label="用户名">{{ userProfile.userName }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ userProfile.nickName }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userProfile.phone }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userProfile.email }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ parseTime(userProfile.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>安全设置</span>
            </div>
          </template>
          <div class="security-item">
            <div class="security-info">
              <div class="security-title">登录密码</div>
              <div class="security-desc">定期修改密码有助于保护账户安全</div>
            </div>
            <el-button type="primary" link @click="handleChangePassword">修改</el-button>
          </div>
          <el-divider />
          <div class="security-item">
            <div class="security-info">
              <div class="security-title">二次验证 (2FA)</div>
              <div class="security-desc">{{ twoFactorEnabled ? '已开启' : '未开启' }}</div>
            </div>
            <el-button type="primary" link @click="handleSetup2FA">{{ twoFactorEnabled ? '管理' : '开启' }}</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：KYC 和 3DS验证码 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>KYC 认证</span>
              <el-tag v-if="kycStatus === 1" type="success">已认证</el-tag>
              <el-tag v-else-if="kycStatus === 0" type="warning">待审核</el-tag>
              <el-tag v-else-if="kycStatus === 2" type="danger">已拒绝</el-tag>
              <el-tag v-else type="info">未认证</el-tag>
            </div>
          </template>
          <div v-if="kycStatus !== 1" class="kyc-notice">
            <el-alert type="warning" :closable="false">
              <template #default>
                <div>完成 KYC 认证后，您可以：</div>
                <ul>
                  <li>申请开卡</li>
                  <li>提高充值额度</li>
                  <li>使用更多功能</li>
                </ul>
              </template>
            </el-alert>
            <el-button type="primary" style="margin-top: 15px" @click="handleKyc">立即认证</el-button>
          </div>
          <div v-else class="kyc-info">
            <el-descriptions :column="2">
              <el-descriptions-item label="认证姓名">{{ kycInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="证件类型">{{ kycInfo.idType === 1 ? '身份证' : '护照' }}</el-descriptions-item>
              <el-descriptions-item label="证件号码">{{ kycInfo.idCardMask }}</el-descriptions-item>
              <el-descriptions-item label="认证时间">{{ parseTime(kycInfo.verifiedAt) }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>3DS 验证码</span>
              <el-button type="primary" link icon="Refresh" @click="refreshOtpList">刷新</el-button>
            </div>
          </template>
          <el-table :data="otpList" v-loading="otpLoading">
            <el-table-column label="卡号" align="center" prop="cardNoMask" width="160" />
            <el-table-column label="验证码" align="center" width="120">
              <template #default="scope">
                <span class="otp-code">{{ scope.row.otpCode }}</span>
              </template>
            </el-table-column>
            <el-table-column label="有效期至" align="center" prop="expireTime" width="180">
              <template #default="scope">
                <span>{{ parseTime(scope.row.expireTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="100">
              <template #default="scope">
                <el-tag v-if="isExpired(scope.row.expireTime)" type="info">已过期</el-tag>
                <el-tag v-else type="success">有效</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="otpList.length === 0" description="暂无验证码" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 修改基本信息对话框 -->
    <el-dialog title="修改基本信息" v-model="profileOpen" width="500px" append-to-body>
      <el-form ref="profileRef" :model="profileForm" :rules="profileRules" label-width="100px">
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="profileForm.nickName" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitProfile">确 定</el-button>
          <el-button @click="profileOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog title="修改密码" v-model="passwordOpen" width="500px" append-to-body>
      <el-form ref="passwordRef" :model="passwordForm" :rules="passwordRules" label-width="120px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitPassword">确 定</el-button>
          <el-button @click="passwordOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="UserCenter">
import { getUserProfile, updateUserProfile, changePassword, getKycStatus, get3dsOtp } from '@/api/user'

const { proxy } = getCurrentInstance()

const userProfile = ref({})
const kycStatus = ref(-1)
const kycInfo = ref({})
const twoFactorEnabled = ref(false)
const otpList = ref([])
const otpLoading = ref(false)

const profileOpen = ref(false)
const passwordOpen = ref(false)

const profileForm = ref({})
const passwordForm = ref({})

const profileRules = {
  nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

/** 获取用户信息 */
function getProfile() {
  getUserProfile().then(response => {
    userProfile.value = response.data
  })
}

/** 获取KYC状态 */
function getKyc() {
  getKycStatus().then(response => {
    kycStatus.value = response.data?.status || -1
    kycInfo.value = response.data || {}
  })
}

/** 获取3DS验证码列表 */
function refreshOtpList() {
  otpLoading.value = true
  // 这里应该查询所有卡片的3DS验证码，简化处理
  otpList.value = []
  otpLoading.value = false
}

/** 判断是否过期 */
function isExpired(expireTime) {
  return new Date(expireTime) < new Date()
}

/** 修改基本信息 */
function handleEditProfile() {
  profileForm.value = { ...userProfile.value }
  profileOpen.value = true
}

/** 提交基本信息 */
function submitProfile() {
  proxy.$refs['profileRef'].validate(valid => {
    if (valid) {
      updateUserProfile(profileForm.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        profileOpen.value = false
        getProfile()
      })
    }
  })
}

/** 修改密码 */
function handleChangePassword() {
  passwordForm.value = {}
  passwordOpen.value = true
}

/** 提交密码修改 */
function submitPassword() {
  proxy.$refs['passwordRef'].validate(valid => {
    if (valid) {
      changePassword(passwordForm.value).then(() => {
        proxy.$modal.msgSuccess('密码修改成功')
        passwordOpen.value = false
      })
    }
  })
}

/** 设置2FA */
function handleSetup2FA() {
  proxy.$modal.msg('2FA配置功能开发中')
}

/** KYC认证 */
function handleKyc() {
  proxy.$modal.msg('KYC认证功能开发中')
}

getProfile()
getKyc()
refreshOtpList()
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}
.security-info {
  flex: 1;
}
.security-title {
  font-weight: bold;
  margin-bottom: 5px;
}
.security-desc {
  font-size: 12px;
  color: #909399;
}
.kyc-notice ul {
  margin: 10px 0;
  padding-left: 20px;
}
.kyc-notice li {
  margin: 5px 0;
}
.otp-code {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
  letter-spacing: 2px;
}
</style>
