import request from '@/utils/request'

// 查询卡产品统计概览
export function getCardStats() {
  return request({
    url: '/admin/stats/card',
    method: 'get'
  })
}

// 按卡BIN统计
export function getStatsByBin(query) {
  return request({
    url: '/admin/stats/byBin',
    method: 'get',
    params: query
  })
}

// 按渠道统计
export function getStatsByChannel(query) {
  return request({
    url: '/admin/stats/byChannel',
    method: 'get',
    params: query
  })
}

// 查询开卡趋势
export function getCardTrend(query) {
  return request({
    url: '/admin/stats/trend',
    method: 'get',
    params: query
  })
}
