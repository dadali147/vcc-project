import request from '@/utils/request'

// 查询交易记录列表
export function listTransaction(query) {
  return request({
    url: '/merchant/transaction/list',
    method: 'get',
    params: query
  })
}

// 查询交易详情
export function getTransaction(transId) {
  return request({
    url: '/merchant/transaction/' + transId,
    method: 'get'
  })
}

// 导出交易记录
export function exportTransaction(query) {
  return request({
    url: '/merchant/transaction/export',
    method: 'post',
    data: query,
    responseType: 'blob'
  })
}

// 查询首页统计
export function getHomeStats() {
  return request({
    url: '/merchant/home/stats',
    method: 'get'
  })
}

// 查询最近交易
export function getRecentTransactions() {
  return request({
    url: '/merchant/home/recentTransactions',
    method: 'get'
  })
}

// 查询系统公告
export function getNotices() {
  return request({
    url: '/merchant/home/notices',
    method: 'get'
  })
}
