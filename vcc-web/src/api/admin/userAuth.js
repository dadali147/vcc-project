import request from '@/utils/request'

// 查询用户验证列表
export function listUserAuth(query) {
  return request({
    url: '/admin/user-auth/list',
    method: 'get',
    params: query
  })
}

// 查询用户2FA详情
export function getUser2faDetail(userId) {
  return request({
    url: '/admin/user-auth/' + userId + '/2fa',
    method: 'get'
  })
}

// 重置用户2FA
export function resetUser2fa(userId) {
  return request({
    url: '/admin/user-auth/' + userId + '/2fa',
    method: 'delete'
  })
}

// 查询用户3DS OTP
export function getUser3dsOtp(userId, reason) {
  return request({
    url: '/admin/user-auth/' + userId + '/3ds-otp',
    method: 'get',
    params: { reason }
  })
}

// 强制解绑2FA
export function forceUnbind2fa(userId) {
  return request({
    url: '/admin/user-auth/' + userId + '/force-unbind',
    method: 'post'
  })
}
