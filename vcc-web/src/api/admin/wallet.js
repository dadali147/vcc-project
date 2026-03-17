import request from '@/utils/request'

// 查询用户钱包列表
export function listWallet(query) {
  return request({
    url: '/admin/wallet/list',
    method: 'get',
    params: query
  })
}

// 余额调整
export function adjustBalance(data) {
  return request({
    url: '/admin/wallet/adjust',
    method: 'post',
    data: data
  })
}

// 查询动账明细
export function listWalletLog(query) {
  return request({
    url: '/admin/wallet/log',
    method: 'get',
    params: query
  })
}
