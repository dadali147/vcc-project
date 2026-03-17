import request from '@/utils/request'

// 查询充值记录列表
export function listRecharge(query) {
  return request({
    url: '/merchant/recharge/list',
    method: 'get',
    params: query
  })
}

// 查询充值详情
export function getRecharge(rechargeId) {
  return request({
    url: '/merchant/recharge/' + rechargeId,
    method: 'get'
  })
}

// 提交充值
export function submitRecharge(data) {
  return request({
    url: '/merchant/recharge/submit',
    method: 'post',
    data: data
  })
}

// 查询充值结果
export function queryRechargeResult(orderNo) {
  return request({
    url: '/merchant/recharge/query/' + orderNo,
    method: 'get'
  })
}

// 查询用户账户余额
export function getUserAccount(currency) {
  return request({
    url: '/merchant/account',
    method: 'get',
    params: { currency }
  })
}
