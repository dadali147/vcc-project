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

// 开卡
export function openCard(data) {
  return request({
    url: '/merchant/card/open',
    method: 'post',
    data: data
  })
}

// 查询卡三要素
export function getCardKeyInfo(cardId) {
  return request({
    url: '/merchant/card/' + cardId + '/key-info',
    method: 'get'
  })
}

// 冻结卡片
export function freezeCard(cardId) {
  return request({
    url: '/merchant/card/' + cardId + '/freeze',
    method: 'post'
  })
}

// 解冻卡片
export function unfreezeCard(cardId) {
  return request({
    url: '/merchant/card/' + cardId + '/unfreeze',
    method: 'post'
  })
}

// 注销卡片
export function cancelCard(cardId) {
  return request({
    url: '/merchant/card/' + cardId + '/cancel',
    method: 'post'
  })
}

// 查询卡BIN列表
export function listCardBin(query) {
  return request({
    url: '/merchant/card-bin/list',
    method: 'get',
    params: query
  })
}
