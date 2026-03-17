import request from '@/utils/request'

// 查询交易记录列表
export function listTransaction(query) {
  return request({
    url: '/admin/transaction/list',
    method: 'get',
    params: query
  })
}

// 查询交易详情
export function getTransaction(transId) {
  return request({
    url: '/admin/transaction/' + transId,
    method: 'get'
  })
}

// 查询交易统计
export function getTransactionStats() {
  return request({
    url: '/admin/transaction/stats',
    method: 'get'
  })
}

// 导出交易记录
export function exportTransaction(query) {
  return request({
    url: '/admin/transaction/export',
    method: 'post',
    data: query,
    responseType: 'blob'
  })
}
