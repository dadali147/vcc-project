import request from '@/utils/request'

// 查询风控规则列表
export function listRiskRule(query) {
  return request({
    url: '/admin/risk/rule/list',
    method: 'get',
    params: query
  })
}

// 查询风控规则详情
export function getRiskRule(id) {
  return request({
    url: '/admin/risk/rule/' + id,
    method: 'get'
  })
}

// 新增风控规则
export function addRiskRule(data) {
  return request({
    url: '/admin/risk/rule',
    method: 'post',
    data: data
  })
}

// 修改风控规则
export function updateRiskRule(data) {
  return request({
    url: '/admin/risk/rule',
    method: 'put',
    data: data
  })
}

// 启用/禁用风控规则
export function toggleRiskRule(id) {
  return request({
    url: '/admin/risk/rule/' + id + '/toggle',
    method: 'put'
  })
}

// 删除风控规则
export function deleteRiskRule(id) {
  return request({
    url: '/admin/risk/rule/' + id,
    method: 'delete'
  })
}

// 查询风控事件列表
export function listRiskEvent(query) {
  return request({
    url: '/admin/business/risk/list',
    method: 'get',
    params: query
  })
}

// 查询风控事件详情
export function getRiskEvent(id) {
  return request({
    url: '/admin/business/risk/' + id,
    method: 'get'
  })
}

// 处理风控事件
export function handleRiskEvent(id, data) {
  return request({
    url: '/admin/business/risk/' + id + '/handle',
    method: 'put',
    data: data
  })
}

// 风控告警列表（Dashboard 用）
export function listRiskAlerts(query) {
  return request({
    url: '/admin/dashboard/risk-alerts',
    method: 'get',
    params: query
  })
}
