import request from '@/utils/request'

/** 获取卡片详情 */
export function getCardDetail(cardId) {
  return request({ url: '/merchant/card/' + cardId, method: 'get' })
}

/** 查询交易列表 */
export function listTransactions(query) {
  return request({ url: '/merchant/transaction/list', method: 'get', params: query })
}
