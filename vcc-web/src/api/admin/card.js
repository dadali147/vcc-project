import request from '@/utils/request'

// 查询卡片列表（管理端）
export function listCardAdmin(query) {
  return request({
    url: '/admin/card/list',
    method: 'get',
    params: query
  })
}

// 查询卡片详情（管理端）
export function getCardAdmin(cardId) {
  return request({
    url: '/admin/card/' + cardId,
    method: 'get'
  })
}

// 审核卡片
export function auditCard(cardId, data) {
  return request({
    url: '/admin/card/' + cardId + '/audit',
    method: 'put',
    data: data
  })
}

// 修改卡片状态
export function changeCardStatus(cardId, status) {
  return request({
    url: '/admin/card/' + cardId + '/status',
    method: 'put',
    params: { status }
  })
}

// 查询卡产品统计
export function getCardStats(query) {
  return request({
    url: '/admin/card/stats',
    method: 'get',
    params: query
  })
}

// 导出卡片数据
export function exportCardAdmin(query) {
  return request({
    url: '/admin/card/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
