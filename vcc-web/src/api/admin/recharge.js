import request from '@/utils/request'

// 查询充值记录列表
export function listRecharge(query) {
  return request({
    url: '/admin/recharge/list',
    method: 'get',
    params: query
  })
}

// 充值审核
export function auditRecharge(data) {
  return request({
    url: '/admin/recharge/audit',
    method: 'put',
    data: data
  })
}

// 手动充值
export function manualRecharge(data) {
  return request({
    url: '/admin/recharge/manual',
    method: 'post',
    data: data
  })
}

// 导出充值记录
export function exportRecharge(query) {
  return request({
    url: '/admin/recharge/export',
    method: 'post',
    data: query,
    responseType: 'blob'
  })
}
