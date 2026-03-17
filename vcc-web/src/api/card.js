import request from '@/utils/request'

// 查询卡片列表
export function listCard(query) {
  return request({
    url: '/merchant/card/list',
    method: 'get',
    params: query
  })
}

// 查询卡片详情
export function getCard(cardId) {
  return request({
    url: '/merchant/card/' + cardId,
    method: 'get'
  })
}

// 查询卡片三要素（卡号/CVV/有效期）
export function getCardSecret(cardId) {
  return request({
    url: '/merchant/card/' + cardId + '/secret',
    method: 'get'
  })
}

// 开卡
export function addCard(data) {
  return request({
    url: '/merchant/card',
    method: 'post',
    data: data
  })
}

// 冻结卡片
export function freezeCard(cardId) {
  return request({
    url: '/merchant/card/' + cardId + '/freeze',
    method: 'put'
  })
}

// 解冻卡片
export function unfreezeCard(cardId) {
  return request({
    url: '/merchant/card/' + cardId + '/unfreeze',
    method: 'put'
  })
}

// 注销卡片
export function cancelCard(cardId) {
  return request({
    url: '/merchant/card/' + cardId + '/cancel',
    method: 'put'
  })
}

// 查询可用卡BIN列表
export function listAvailableCardBin() {
  return request({
    url: '/merchant/cardBin/available',
    method: 'get'
  })
}
