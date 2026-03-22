import request from '@/utils/request'

// 提交个人 KYC
export function submitPersonalKyc(data) {
  return request({
    url: '/merchant/kyc/personal',
    method: 'post',
    data: data
  })
}

// 提交企业 KYC
export function submitEnterpriseKyc(data) {
  return request({
    url: '/merchant/kyc/enterprise',
    method: 'post',
    data: data
  })
}

// 查询 KYC 状态
export function getKycStatus() {
  return request({
    url: '/merchant/kyc/status',
    method: 'get'
  })
}

// 获取 KYC 审核结果（含拒绝原因）
export function getKycResult() {
  return request({
    url: '/merchant/kyc/result',
    method: 'get'
  })
}
