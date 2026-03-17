import request from '@/utils/request'

// 查询用户钱包列表
export function listWalletAdmin(query) {
  return request({
    url: '/admin/wallet/list',
    method: 'get',
    params: query
  })
}

// 查询用户钱包详情
export function getWalletAdmin(userId) {
  return request({
    url: '/admin/wallet/' + userId,
    method: 'get'
  })
}

// 调整用户余额
export function adjustBalance(userId, data) {
  return request({
    url: '/admin/wallet/' + userId + '/adjust',
    method: 'post',
    data: data
  })
}

// 查询用户动账明细
export function listWalletTransactions(userId, query) {
  return request({
    url: '/admin/wallet/' + userId + '/transactions',
    method: 'get',
    params: query
  })
}
