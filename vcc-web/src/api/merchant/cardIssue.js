import request from '@/utils/request'

/** 查询开卡记录列表 */
export function listIssueRequests(query) {
  return request({ url: '/merchant/cardIssue/list', method: 'get', params: query })
}

/** 获取开卡记录详情 */
export function getIssueRequestDetail(id) {
  return request({ url: '/merchant/cardIssue/' + id, method: 'get' })
}
