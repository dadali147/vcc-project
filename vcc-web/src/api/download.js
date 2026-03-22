import request from '@/utils/request'

// 查询下载中心列表
export function listDownload(query) {
  return request({
    url: '/merchant/download/list',
    method: 'get',
    params: query
  })
}

// 下载文件
export function downloadFile(fileId) {
  return request({
    url: '/merchant/download/' + fileId,
    method: 'get',
    responseType: 'blob'
  })
}

// 删除下载记录
export function delDownload(fileId) {
  return request({
    url: '/merchant/download/' + fileId,
    method: 'delete'
  })
}

// 重新发起导出任务
export function retryExport(taskId) {
  return request({
    url: '/merchant/download/retry/' + taskId,
    method: 'post'
  })
}
