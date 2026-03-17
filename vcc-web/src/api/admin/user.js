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

// 审核用户
export function auditUser(userId, data) {
  return request({
    url: '/admin/user/' + userId + '/audit',
    method: 'put',
    data: data
  })
}

// 修改用户状态
export function changeUserStatus(userId, status) {
  return request({
    url: '/admin/user/' + userId + '/status',
    method: 'put',
    params: { status }
  })
}

// 查询用户费率配置
export function getUserFeeConfig(userId) {
  return request({
    url: '/admin/user/' + userId + '/fee-config',
    method: 'get'
  })
}

// 设置用户费率
export function setUserFeeConfig(userId, data) {
  return request({
    url: '/admin/user/' + userId + '/fee-config',
    method: 'put',
    data: data
  })
}

// 查询用户2FA信息
export function getUser2faInfo(userId) {
  return request({
    url: '/admin/user/' + userId + '/2fa',
    method: 'get'
  })
}

// 重置用户2FA
export function resetUser2fa(userId) {
  return request({
    url: '/admin/user/' + userId + '/2fa/reset',
    method: 'post'
  })
}
