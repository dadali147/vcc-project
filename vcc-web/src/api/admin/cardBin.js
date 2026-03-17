import request from '@/utils/request'

// 查询卡BIN列表
export function listCardBin(query) {
  return request({
    url: '/admin/card-bin/list',
    method: 'get',
    params: query
  })
}

// 查询卡BIN详情
export function getCardBin(cardBinId) {
  return request({
    url: '/admin/card-bin/' + cardBinId,
    method: 'get'
  })
}

// 添加卡BIN
export function addCardBin(data) {
  return request({
    url: '/admin/card-bin',
    method: 'post',
    data: data
  })
}

// 修改卡BIN
export function updateCardBin(data) {
  return request({
    url: '/admin/card-bin',
    method: 'put',
    data: data
  })
}

// 删除卡BIN
export function delCardBin(cardBinId) {
  return request({
    url: '/admin/card-bin/' + cardBinId,
    method: 'delete'
  })
}

// 修改卡BIN状态
export function changeCardBinStatus(cardBinId, status) {
  return request({
    url: '/admin/card-bin/' + cardBinId + '/status',
    method: 'put',
    params: { status }
  })
}
