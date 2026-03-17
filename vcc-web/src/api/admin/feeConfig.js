import request from '@/utils/request'

// 查询用户费率配置列表
export function listUserFeeConfig(query) {
  return request({
    url: '/admin/fee/user/list',
    method: 'get',
    params: query
  })
}

// 修改用户费率配置
export function updateUserFeeConfig(userId, data) {
  return request({
    url: '/admin/fee/user/' + userId,
    method: 'put',
    data: data
  })
}

// 查询卡BIN费率配置列表
export function listCardBinFeeConfig(query) {
  return request({
    url: '/admin/fee/cardbin/list',
    method: 'get',
    params: query
  })
}

// 修改卡BIN费率配置
export function updateCardBinFeeConfig(cardBinId, data) {
  return request({
    url: '/admin/fee/cardbin/' + cardBinId,
    method: 'put',
    data: data
  })
}
