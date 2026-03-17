import request from '@/utils/request'

// 查询持卡人列表
export function listCardHolder(query) {
  return request({
    url: '/merchant/card-holder/list',
    method: 'get',
    params: query
  })
}

// 查询持卡人详情
export function getCardHolder(holderId) {
  return request({
    url: '/merchant/card-holder/' + holderId,
    method: 'get'
  })
}

// 添加持卡人
export function addCardHolder(data) {
  return request({
    url: '/merchant/card-holder',
    method: 'post',
    data: data
  })
}

// 修改持卡人
export function updateCardHolder(data) {
  return request({
    url: '/merchant/card-holder',
    method: 'put',
    data: data
  })
}

// 删除持卡人
export function delCardHolder(holderId) {
  return request({
    url: '/merchant/card-holder/' + holderId,
    method: 'delete'
  })
}
