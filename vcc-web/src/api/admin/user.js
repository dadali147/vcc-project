import request from '@/utils/request'

// 查询用户列表
export function listUser(query) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params: query
  })
}

// 查询用户详情
export function getUser(userId) {
  return request({
    url: '/admin/user/' + userId,
    method: 'get'
  })
}

// KYC审核
export function auditKyc(data) {
  return request({
    url: '/admin/user/kyc/audit',
    method: 'put',
    data: data
  })
}

// 配置用户费率
export function updateUserRate(data) {
  return request({
    url: '/admin/user/rate',
    method: 'put',
    data: data
  })
}

// 重置2FA
export function reset2fa(userId) {
  return request({
    url: '/admin/user/' + userId + '/reset2fa',
    method: 'put'
  })
}

// 修改用户状态（启用/禁用）
export function changeUserStatus(userId, status) {
  return request({
    url: '/admin/user/changeStatus',
    method: 'put',
    data: { userId, status }
  })
}
