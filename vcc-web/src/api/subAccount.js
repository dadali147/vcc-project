import request from '@/utils/request'

// 查询子账户列表
export function listSubAccount(query) {
  return request({
    url: '/merchant/subAccount/list',
    method: 'get',
    params: query
  })
}

// 获取子账户详情
export function getSubAccount(subAccountId) {
  return request({
    url: '/merchant/subAccount/' + subAccountId,
    method: 'get'
  })
}

// 新增子账户
export function addSubAccount(data) {
  return request({
    url: '/merchant/subAccount',
    method: 'post',
    data: data
  })
}

// 编辑子账户
export function updateSubAccount(data) {
  return request({
    url: '/merchant/subAccount',
    method: 'put',
    data: data
  })
}

// 禁用子账户
export function disableSubAccount(subAccountId) {
  return request({
    url: '/merchant/subAccount/' + subAccountId + '/disable',
    method: 'put'
  })
}

// 启用子账户
export function enableSubAccount(subAccountId) {
  return request({
    url: '/merchant/subAccount/' + subAccountId + '/enable',
    method: 'put'
  })
}

// 重置子账户密码
export function resetSubAccountPwd(subAccountId) {
  return request({
    url: '/merchant/subAccount/' + subAccountId + '/resetPwd',
    method: 'put'
  })
}
