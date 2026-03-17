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
export function getTransaction(transactionId) {
  return request({
    url: '/merchant/transaction/' + transactionId,
    method: 'get'
  })
}

// 查询卡片交易记录
export function listCardTransaction(cardId, query) {
  return request({
    url: '/merchant/transaction/card/' + cardId,
    method: 'get',
    params: query
  })
}
