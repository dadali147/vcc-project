import request from '@/utils/request'

// 查询消息通知列表
export function listMessage(query) {
  return request({
    url: '/merchant/message/list',
    method: 'get',
    params: query
  })
}

// 获取消息详情
export function getMessage(messageId) {
  return request({
    url: '/merchant/message/' + messageId,
    method: 'get'
  })
}

// 标记消息已读
export function markMessageRead(messageId) {
  return request({
    url: '/merchant/message/' + messageId + '/read',
    method: 'put'
  })
}

// 标记全部已读
export function markAllMessageRead() {
  return request({
    url: '/merchant/message/readAll',
    method: 'put'
  })
}

// 获取未读消息数
export function getUnreadCount() {
  return request({
    url: '/merchant/message/unreadCount',
    method: 'get'
  })
}
