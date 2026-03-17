import request from '@/utils/request'

// 查询交易列表（管理端）
export function listTransactionAdmin(query) {
  return request({
    url: '/admin/transaction/list',
    method: 'get',
    params: query
  })
}

// 查询交易详情
export function getTransactionAdmin(transactionId) {
  return request({
    url: '/admin/transaction/' + transactionId,
    method: 'get'
  })
}

// 交易统计
export function getTransactionStats(query) {
  return request({
    url: '/admin/transaction/stats',
    method: 'get',
    params: query
  })
}

// 导出交易数据
export function exportTransaction(query) {
  return request({
    url: '/admin/transaction/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
