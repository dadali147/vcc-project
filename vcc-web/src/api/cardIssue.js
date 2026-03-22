import request from '@/utils/request'

// 查询开卡记录列表
export function listCardIssue(query) {
  return request({
    url: '/merchant/cardIssue/list',
    method: 'get',
    params: query
  })
}

// 查询开卡记录详情
export function getCardIssue(taskId) {
  return request({
    url: '/merchant/cardIssue/' + taskId,
    method: 'get'
  })
}
