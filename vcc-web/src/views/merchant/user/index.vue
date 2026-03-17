<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 个人信息 -->
      <el-col :lg="14" :xs="24">
        <el-card shadow="hover">
          <template #header>
            <span>基本资料</span>
          </template>
          <el-form :model="profileForm" :rules="profileRules" ref="profileRef" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.userName" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickName">
              <el-input v-model="profileForm.nickName" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="手机号" prop="phonenumber">
              <el-input v-model="profileForm.phonenumber" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="hover" style="margin-top: 16px">
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdRef" label-width="100px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitPwd">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：KYC + 2FA -->
      <el-col :lg="10" :xs="24">
        <el-card shadow="hover">
          <template #header>
            <span>KYC 认证</span>
          </template>
          <div class="kyc-status">
            <el-result v-if="kycStatus === '1'" icon="success" title="已认证" sub-title="您的身份已通过认证" />
            <el-result v-else-if="kycStatus === '2'" icon="warning" title="审核中" sub-title="您的认证信息正在审核中，请耐心等待" />
            <el-result v-else-if="kycStatus === '3'" icon="error" title="认证失败" :sub-title="'原因: ' + (kycRemark || '未通过审核')">
              <template #extra>
                <el-button type="primary" @click="kycStatus = '0'">重新认证</el-button>
              </template>
            </el-result>
            <div v-else>
              <el-alert title="您尚未完成KYC认证，部分功能可能受限" type="warning" :closable="false" style="margin-bottom: 16px" />
              <el-form :model="kycForm" :rules="kycRules" ref="kycRef" label-width="90px">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="kycForm.realName" placeholder="请输入真实姓名" />
                </el-form-item>
                <el-form-item label="证件类型" prop="idType">
                  <el-select v-model="kycForm.idType" style="width: 100%">
                    <el-option label="身份证" value="1" />
                    <el-option label="护照" value="2" />
                  </el-select>
                </el-form-item>
                <el-form-item label="证件号码" prop="idNumber">
                  <el-input v-model="kycForm.idNumber" placeholder="请输入证件号码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitKycForm">提交认证</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" style="margin-top: 16px">
          <template #header>
            <span>两步验证 (2FA)</span>
          </template>
          <div v-if="twoFaBound" class="tfa-bound">
            <el-result icon="success" title="已绑定" sub-title="您的账户已开启两步验证保护" />
          </div>
          <div v-else class="tfa-unbind">
            <el-alert title="建议开启两步验证以提升账户安全性" type="info" :closable="false" style="margin-bottom: 16px" />
            <el-form :model="tfaForm" ref="tfaRef" label-width="90px">
              <el-form-item label="验证码">
                <el-input v-model="tfaForm.code" placeholder="请输入Google验证码">
                  <template #append>
                    <el-button @click="generateSecret">获取密钥</el-button>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item v-if="tfaSecret">
                <div class="tfa-secret">
                  <div>密钥: <el-tag>{{ tfaSecret }}</el-tag></div>
                  <div style="margin-top: 8px; font-size: 12px; color: #909399">请在 Google Authenticator 中添加此密钥</div>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="bindTfa">绑定</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="MerchantUser">
import { getUserProfile, updateUserProfile, updateUserPwd, getKycStatus, submitKyc, get2faStatus, bind2fa } from "@/api/user"

const { proxy } = getCurrentInstance()

const profileForm = ref({})
const kycStatus = ref('0')
const kycRemark = ref('')
const twoFaBound = ref(false)
const tfaSecret = ref('')

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const kycForm = reactive({
  realName: '',
  idType: '1',
  idNumber: ''
})

const tfaForm = reactive({
  code: ''
})

const profileRules = {
  nickName: [{ required: true, message: "昵称不能为空", trigger: "blur" }],
  email: [{ type: 'email', message: "请输入正确的邮箱", trigger: ["blur", "change"] }]
}

const equalToPassword = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: "旧密码不能为空", trigger: "blur" }],
  newPassword: [{ required: true, message: "新密码不能为空", trigger: "blur" }, { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" }],
  confirmPassword: [{ required: true, message: "确认密码不能为空", trigger: "blur" }, { validator: equalToPassword, trigger: "blur" }]
}

const kycRules = {
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  idType: [{ required: true, message: "请选择证件类型", trigger: "change" }],
  idNumber: [{ required: true, message: "请输入证件号码", trigger: "blur" }]
}

function loadProfile() {
  getUserProfile().then(response => {
    profileForm.value = response.data || {}
  })
}

function loadKycStatus() {
  getKycStatus().then(response => {
    kycStatus.value = response.data?.status || '0'
    kycRemark.value = response.data?.remark || ''
  })
}

function load2faStatus() {
  get2faStatus().then(response => {
    twoFaBound.value = response.data?.bound || false
  })
}

function submitProfile() {
  proxy.$refs["profileRef"].validate(valid => {
    if (valid) {
      updateUserProfile(profileForm.value).then(() => {
        proxy.$modal.msgSuccess("修改成功")
      })
    }
  })
}

function submitPwd() {
  proxy.$refs["pwdRef"].validate(valid => {
    if (valid) {
      updateUserPwd(pwdForm.oldPassword, pwdForm.newPassword).then(() => {
        proxy.$modal.msgSuccess("密码修改成功")
        pwdForm.oldPassword = ''
        pwdForm.newPassword = ''
        pwdForm.confirmPassword = ''
      })
    }
  })
}

function submitKycForm() {
  proxy.$refs["kycRef"].validate(valid => {
    if (valid) {
      submitKyc(kycForm).then(() => {
        proxy.$modal.msgSuccess("提交成功，请等待审核")
        kycStatus.value = '2'
      })
    }
  })
}

function generateSecret() {
  get2faStatus().then(response => {
    tfaSecret.value = response.data?.secret || ''
  })
}

function bindTfa() {
  if (!tfaForm.code) {
    proxy.$modal.msgError("请输入验证码")
    return
  }
  bind2fa({ code: tfaForm.code, secret: tfaSecret.value }).then(() => {
    proxy.$modal.msgSuccess("绑定成功")
    twoFaBound.value = true
  })
}

onMounted(() => {
  loadProfile()
  loadKycStatus()
  load2faStatus()
})
</script>

<style scoped>
.kyc-status {
  min-height: 200px;
}
.tfa-bound, .tfa-unbind {
  min-height: 150px;
}
.tfa-secret {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>
