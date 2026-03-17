import request from '@/utils/request'

// 查询卡片列表（管理端）
export function listCard(query) {
  return request({
    url: '/admin/card/list',
    method: 'get',
    params: query
  })
}

// 查询卡片详情
export function getCard(cardId) {
  return request({
    url: '/admin/card/' + cardId,
    method: 'get'
  })
}

// 冻结卡片
export function freezeCard(cardId) {
  return request({
    url: '/admin/card/' + cardId + '/freeze',
    method: 'put'
  })
}

// 解冻卡片
export function unfreezeCard(cardId) {
  return request({
    url: '/admin/card/' + cardId + '/unfreeze',
    method: 'put'
  })
}

// 注销卡片
export function cancelCard(cardId) {
  return request({
    url: '/admin/card/' + cardId + '/cancel',
    method: 'put'
  })
}

// 导出卡片
export function exportCard(query) {
  return request({
    url: '/admin/card/export',
    method: 'post',
    data: query,
    responseType: 'blob'
  })
}
