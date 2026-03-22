import request from '@/utils/request'

/** 查询子账户列表 */
export function listSubAccounts(query) {
  return request({ url: '/merchant/subAccount/list', method: 'get', params: query })
}

/** 新增子账户 */
export function addSubAccount(data) {
  return request({ url: '/merchant/subAccount', method: 'post', data })
}

/** 修改子账户 */
export function updateSubAccount(data) {
  return request({ url: '/merchant/subAccount', method: 'put', data })
}

/** 删除子账户 */
export function delSubAccount(userId) {
  return request({ url: '/merchant/subAccount/' + userId, method: 'delete' })
}

/** 重置密码 */
export function resetSubAccountPwd(userId, password) {
  return request({ url: '/merchant/subAccount/resetPwd', method: 'put', data: { userId, password } })
}
