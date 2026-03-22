<template>
  <div class="register">
    <el-form ref="registerRef" :model="registerForm" :rules="registerRules" class="register-form">
      <h3 class="title">{{ title }}</h3>
      <el-form-item prop="merchantName">
        <el-input
          v-model="registerForm.merchantName"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="商户名称"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="contactName">
        <el-input
          v-model="registerForm.contactName"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="联系人姓名"
        >
          <template #prefix><svg-icon icon-class="people" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="email">
        <el-input
          v-model="registerForm.email"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="邮箱（登录账号）"
        >
          <template #prefix><svg-icon icon-class="email" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="emailCaptcha">
        <el-input
          size="large"
          v-model="registerForm.emailCaptcha"
          auto-complete="off"
          placeholder="邮箱验证码"
          style="width: 63%"
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <el-button
          size="large"
          :disabled="captchaCooldown > 0"
          @click="sendEmailCaptcha"
          style="width: 34%; margin-left: 3%"
        >
          {{ captchaCooldown > 0 ? captchaCooldown + 's' : '发送验证码' }}
        </el-button>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="密码"
          show-password
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="确认密码"
          show-password
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="country">
        <el-select v-model="registerForm.country" placeholder="国家（选填）" size="large" style="width: 100%">
          <el-option label="中国" value="CN" />
          <el-option label="中国香港" value="HK" />
          <el-option label="中国台湾" value="TW" />
          <el-option label="美国" value="US" />
          <el-option label="英国" value="GB" />
          <el-option label="新加坡" value="SG" />
          <el-option label="日本" value="JP" />
          <el-option label="韩国" value="KR" />
        </el-select>
      </el-form-item>
      <el-form-item prop="inviteCode">
        <el-input
          v-model="registerForm.inviteCode"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="邀请码（选填）"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="agreeProtocol">
        <el-checkbox v-model="registerForm.agreeProtocol">
          我已阅读并同意
          <el-link type="primary" :underline="false">《用户服务协议》</el-link>
          和
          <el-link type="primary" :underline="false">《隐私政策》</el-link>
        </el-checkbox>
      </el-form-item>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="large"
          type="primary"
          style="width:100%;"
          @click.prevent="handleRegister"
        >
          <span v-if="!loading">注 册</span>
          <span v-else>注 册 中...</span>
        </el-button>
        <div style="float: right;">
          <router-link class="link-type" :to="'/login'">使用已有账户登录</router-link>
        </div>
      </el-form-item>
    </el-form>
    <div class="el-register-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from "element-plus"
import { register } from "@/api/login"
import request from "@/utils/request"
import defaultSettings from '@/settings'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const router = useRouter()
const { proxy } = getCurrentInstance()

const registerForm = ref({
  merchantName: "",
  contactName: "",
  email: "",
  emailCaptcha: "",
  password: "",
  confirmPassword: "",
  country: undefined,
  inviteCode: "",
  agreeProtocol: false
})

const equalToPassword = (rule, value, callback) => {
  if (registerForm.value.password !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const agreeProtocolValidator = (rule, value, callback) => {
  if (!value) {
    callback(new Error("请阅读并同意用户协议和隐私政策"))
  } else {
    callback()
  }
}

const registerRules = {
  merchantName: [{ required: true, trigger: "blur", message: "请输入商户名称" }],
  contactName: [{ required: true, trigger: "blur", message: "请输入联系人姓名" }],
  email: [
    { required: true, trigger: "blur", message: "请输入您的邮箱" },
    { type: 'email', message: "请输入正确的邮箱格式", trigger: "blur" }
  ],
  emailCaptcha: [{ required: true, trigger: "change", message: "请输入邮箱验证码" }],
  password: [
    { required: true, trigger: "blur", message: "请输入您的密码" },
    { min: 8, max: 20, message: "密码长度必须介于 8 和 20 之间", trigger: "blur" },
    { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\\ |", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", message: "请再次输入您的密码" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ],
  agreeProtocol: [{ validator: agreeProtocolValidator, trigger: "change" }]
}

const loading = ref(false)
const captchaCooldown = ref(0)
let captchaTimer = null

function sendEmailCaptcha() {
  if (!registerForm.value.email) {
    proxy.$modal.msgError("请先输入邮箱")
    return
  }
  if (captchaCooldown.value > 0) return
  request({
    url: '/captchaEmail',
    headers: { isToken: false },
    method: 'post',
    data: { email: registerForm.value.email, bizType: 'REGISTER' }
  }).then(() => {
    proxy.$modal.msgSuccess("验证码已发送")
    captchaCooldown.value = 60
    captchaTimer = setInterval(() => {
      captchaCooldown.value--
      if (captchaCooldown.value <= 0) {
        clearInterval(captchaTimer)
        captchaTimer = null
      }
    }, 1000)
  }).catch(() => {})
}

function handleRegister() {
  proxy.$refs.registerRef.validate(valid => {
    if (valid) {
      loading.value = true
      register(registerForm.value).then(res => {
        const merchantName = registerForm.value.merchantName
        ElMessageBox.alert(
          "<font color='red'>恭喜你，商户 \"" + merchantName + "\" 注册成功！</font>",
          "系统提示",
          { dangerouslyUseHTMLString: true, type: "success" }
        ).then(() => {
          router.push("/login")
        }).catch(() => {})
      }).catch(() => {
        loading.value = false
      })
    }
  })
}
</script>

<style lang='scss' scoped>
.register {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.register-form {
  border-radius: 6px;
  background: #ffffff;
  width: 440px;
  padding: 25px 25px 5px 25px;
  .el-input {
    height: 40px;
    input {
      height: 40px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 0px;
  }
}
.el-register-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
</style>
