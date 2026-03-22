import request from '@/utils/request'

/** 获取 KYC 信息 */
export function getKycInfo() {
  return request({ url: '/merchant/kyc/info', method: 'get' })
}

/** 提交 KYC 审核 */
export function submitKyc(data) {
  return request({ url: '/merchant/kyc/submit', method: 'post', data })
}
