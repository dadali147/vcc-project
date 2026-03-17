import request from '@/utils/request'

// 查询3DS验证码
export function get3dsOtp(cardId) {
  return request({
    url: '/merchant/3ds/otp/' + cardId,
    method: 'get'
  })
}

// 获取用户KYC状态
export function getKycStatus() {
  return request({
    url: '/merchant/kyc/status',
    method: 'get'
  })
}

// 提交KYC认证
export function submitKyc(data) {
  return request({
    url: '/merchant/kyc/submit',
    method: 'post',
    data: data
  })
}

// 查询用户基本信息
export function getUserProfile() {
  return request({
    url: '/merchant/user/profile',
    method: 'get'
  })
}

// 修改用户基本信息
export function updateUserProfile(data) {
  return request({
    url: '/merchant/user/profile',
    method: 'put',
    data: data
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/merchant/user/password',
    method: 'put',
    data: data
  })
}

// 获取二次验证配置
export function get2faConfig() {
  return request({
    url: '/merchant/user/2fa',
    method: 'get'
  })
}

// 配置二次验证
export function setup2fa(data) {
  return request({
    url: '/merchant/user/2fa',
    method: 'post',
    data: data
  })
}
