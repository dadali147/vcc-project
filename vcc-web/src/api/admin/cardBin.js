import request from '@/utils/request'

// 查询卡BIN列表
export function listCardBin(query) {
  return request({
    url: '/admin/cardBin/list',
    method: 'get',
    params: query
  })
}

// 查询卡BIN详情
export function getCardBin(binId) {
  return request({
    url: '/admin/cardBin/' + binId,
    method: 'get'
  })
}

// 新增卡BIN
export function addCardBin(data) {
  return request({
    url: '/admin/cardBin',
    method: 'post',
    data: data
  })
}

// 修改卡BIN
export function updateCardBin(data) {
  return request({
    url: '/admin/cardBin',
    method: 'put',
    data: data
  })
}

// 删除卡BIN
export function delCardBin(binId) {
  return request({
    url: '/admin/cardBin/' + binId,
    method: 'delete'
  })
}

// 修改卡BIN状态
export function changeCardBinStatus(binId, status) {
  return request({
    url: '/admin/cardBin/changeStatus',
    method: 'put',
    data: { binId, status }
  })
}
