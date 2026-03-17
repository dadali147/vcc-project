import request from '@/utils/request'

// 查询个人信息
export function getUserProfile() {
  return request({
    url: '/merchant/user/profile',
    method: 'get'
  })
}

// 修改个人信息
export function updateUserProfile(data) {
  return request({
    url: '/merchant/user/profile',
    method: 'put',
    data: data
  })
}

// 修改密码
export function updateUserPwd(oldPassword, newPassword) {
  return request({
    url: '/merchant/user/profile/updatePwd',
    method: 'put',
    data: { oldPassword, newPassword }
  })
}

// 获取KYC认证状态
export function getKycStatus() {
  return request({
    url: '/merchant/user/kyc',
    method: 'get'
  })
}

// 提交KYC认证
export function submitKyc(data) {
  return request({
    url: '/merchant/user/kyc',
    method: 'post',
    data: data
  })
}

// 获取2FA状态
export function get2faStatus() {
  return request({
    url: '/merchant/user/2fa',
    method: 'get'
  })
}

// 绑定2FA
export function bind2fa(data) {
  return request({
    url: '/merchant/user/2fa/bind',
    method: 'post',
    data: data
  })
}

// 获取钱包信息
export function getWallet() {
  return request({
    url: '/merchant/wallet',
    method: 'get'
  })
}

// 查询动账明细
export function listWalletLog(query) {
  return request({
    url: '/merchant/wallet/log',
    method: 'get',
    params: query
  })
}
