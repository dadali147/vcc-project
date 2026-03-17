import request from '@/utils/request'

// 查询充值列表（管理端）
export function listRechargeAdmin(query) {
  return request({
    url: '/admin/recharge/list',
    method: 'get',
    params: query
  })
}

// 查询充值详情
export function getRechargeAdmin(rechargeId) {
  return request({
    url: '/admin/recharge/' + rechargeId,
    method: 'get'
  })
}

// 审核充值
export function auditRecharge(rechargeId, data) {
  return request({
    url: '/admin/recharge/' + rechargeId + '/audit',
    method: 'put',
    data: data
  })
}

// 手动充值（管理员代充）
export function manualRecharge(data) {
  return request({
    url: '/admin/recharge/manual',
    method: 'post',
    data: data
  })
}
