import request from '@/utils/request'

// 查询充值记录列表
export function listRecharge(query) {
  return request({
    url: '/merchant/recharge/list',
    method: 'get',
    params: query
  })
}

// 获取USDT充值地址
export function getRechargeAddress() {
  return request({
    url: '/merchant/recharge/address',
    method: 'get'
  })
}
