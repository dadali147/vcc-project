import request from '@/utils/request'

// 查询系统配置列表
export function listSysConfig(query) {
  return request({
    url: '/admin/config/list',
    method: 'get',
    params: query
  })
}

// 查询系统配置详情
export function getSysConfig(configKey) {
  return request({
    url: '/admin/config/' + configKey,
    method: 'get'
  })
}

// 修改系统配置
export function updateSysConfig(configKey, configValue) {
  return request({
    url: '/admin/config/' + configKey,
    method: 'put',
    data: { configValue }
  })
}
