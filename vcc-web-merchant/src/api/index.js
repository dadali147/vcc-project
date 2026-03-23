import client from './client'

/**
 * Authentication APIs (RuoYi standard)
 * RuoYi response format: AjaxResult { code: 200, msg: "...", token: "...", ... }
 */
export const authApi = {
  // RuoYi login: POST /login with { username, password, code, uuid }
  login: (credentials) => client.post('/login', credentials),

  // RuoYi captcha: GET /captchaImage returns { img: base64, uuid: string, captchaEnabled: bool }
  getCaptcha: () => client.get('/captchaImage'),

  // RuoYi get user info: GET /getInfo returns { user, roles, permissions }
  getInfo: () => client.get('/getInfo'),

  // RuoYi get routers: GET /getRouters
  getRouters: () => client.get('/getRouters'),

  // RuoYi logout
  logout: () => client.post('/logout'),
}

/**
 * Profile APIs (RuoYi SysProfileController)
 */
export const profileApi = {
  get: () => client.get('/system/user/profile'),
  update: (data) => client.put('/system/user/profile', data),
  changePassword: (data) => client.put('/system/user/profile/updatePwd', data),
  uploadAvatar: (file) => {
    const formData = new FormData()
    formData.append('avatarfile', file)
    return client.post('/system/user/profile/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
}

/**
 * Cardholder APIs (v3)
 * Controller: VccCardHolderController @ /merchant/v3/holders
 */
export const cardholderApi = {
  list: (params) => client.get('/merchant/v3/holders/list', { params }),
  get: (holderId) => client.get(`/merchant/v3/holders/${holderId}`),
  detail: (holderId) => client.get(`/merchant/v3/holders/${holderId}/detail`),
  cards: (holderId) => client.get(`/merchant/v3/holders/${holderId}/cards`),
  create: (data) => client.post('/merchant/v3/holders', data),
  update: (holderId, data) => client.put(`/merchant/v3/holders/${holderId}`, data),
  updateStatus: (holderId, data) => client.put(`/merchant/v3/holders/${holderId}/status`, data),
  delete: (holderId) => client.delete(`/merchant/v3/holders/${holderId}`),
}

/**
 * Card APIs (v3)
 * Controller: CardManageController @ /merchant/v3/cards
 */
export const cardApi = {
  list: (params) => client.get('/merchant/v3/cards/list', { params }),
  get: (id) => client.get(`/merchant/v3/cards/${id}`),
  transactions: (id, params) => client.get(`/merchant/v3/cards/${id}/transactions`, { params }),
  feeConfig: (id) => client.get(`/merchant/v3/cards/${id}/fee-config`),
  updateRemark: (id, data) => client.put(`/merchant/v3/cards/${id}/remark`, data),
  freeze: (id) => client.post(`/merchant/v3/cards/${id}/freeze`),
  unfreeze: (id) => client.post(`/merchant/v3/cards/${id}/unfreeze`),
  cancel: (id) => client.post(`/merchant/v3/cards/${id}/cancel`),
}

/**
 * Card Key Info (v1 controller)
 * Controller: CardController @ /merchant/card
 */
export const cardKeyInfoApi = {
  getKeyInfo: (id) => client.get(`/merchant/card/key-info/${id}`),
  activate: (id) => client.post(`/merchant/card/activate/${id}`),
  open: (data) => client.post('/merchant/card/open', data),
}

/**
 * Card Applications / Card Issue (v3)
 * Controller: CardIssueController @ /merchant/v3/card-issue
 */
export const cardIssueApi = {
  list: (params) => client.get('/merchant/v3/card-issue/list', { params }),
  get: (batchNo) => client.get(`/merchant/v3/card-issue/batch/${batchNo}`),
  getItems: (requestId) => client.get(`/merchant/v3/card-issue/${requestId}/items`),
  submit: (data) => client.post('/merchant/v3/card-issue', data),
  delete: (requestId) => client.delete(`/merchant/v3/card-issue/${requestId}`),
}

/**
 * Transaction APIs (v3)
 * Controller: TransactionController @ /merchant/v3/transactions
 */
export const transactionApi = {
  list: (params) => client.get('/merchant/v3/transactions/list', { params }),
  get: (id) => client.get(`/merchant/v3/transactions/${id}`),
  getByCard: (cardId, params) => client.get(`/merchant/v3/transactions/card/${cardId}`, { params }),
  getRelated: (txnId) => client.get(`/merchant/v3/transactions/related/${txnId}`),
  refund: (data) => client.post('/merchant/v3/transactions/refund', data),
  reverse: (data) => client.post('/merchant/v3/transactions/reverse', data),
}

/**
 * Recharge APIs
 * Controller: RechargeController @ /merchant/recharge
 */
export const rechargeApi = {
  list: (params) => client.get('/merchant/recharge/list', { params }),
  get: (id) => client.get(`/merchant/recharge/${id}`),
  submit: (data) => client.post('/merchant/recharge/submit', data),
  query: (orderNo) => client.get(`/merchant/recharge/query/${orderNo}`),
}

/**
 * Account APIs
 * Controller: UserAccountController @ /merchant/account
 */
export const accountApi = {
  list: (params) => client.get('/merchant/account/list', { params }),
  get: (id) => client.get(`/merchant/account/${id}`),
  getBalance: () => client.get('/merchant/account/balance'),
  getInfo: () => client.get('/merchant/account/info'),
  create: (data) => client.post('/merchant/account/create', data),
  transferIn: (data) => client.post('/merchant/account/transfer-in', data),
  transferOut: (data) => client.post('/merchant/account/transfer-out', data),
}

/**
 * Card Operation Logs (v3)
 * Controller: CardOperationLogController @ /merchant/v3/card-operations
 */
export const cardOperationApi = {
  list: (params) => client.get('/merchant/v3/card-operations/list', { params }),
  getByCard: (cardId, params) => client.get(`/merchant/v3/card-operations/card/${cardId}`, { params }),
  getByMerchant: (params) => client.get('/merchant/v3/card-operations/merchant', { params }),
}

/**
 * Export / Download APIs
 * Controller: ExportTaskController @ /merchant/export
 */
export const downloadApi = {
  list: (params) => client.get('/merchant/export/list', { params }),
  get: (id) => client.get(`/merchant/export/${id}`),
  submit: (data) => client.post('/merchant/export/submit', data),
  download: (id) => client.get(`/merchant/export/download/${id}`, { responseType: 'blob' }),
  delete: (ids) => client.delete(`/merchant/export/${ids}`),
}

/**
 * Message APIs
 * Controller: MessageController @ /merchant/message
 */
export const messageApi = {
  list: (params) => client.get('/merchant/message/list', { params }),
  get: (id) => client.get(`/merchant/message/${id}`),
  getUnreadCount: () => client.get('/merchant/message/unread-count'),
  markRead: (id) => client.post(`/merchant/message/read/${id}`),
  markAllRead: () => client.post('/merchant/message/read-all'),
  delete: (ids) => client.delete(`/merchant/message/${ids}`),
}

/**
 * Debt APIs (v3)
 * Controller: DebtController @ /merchant/v3/debt
 */
export const debtApi = {
  list: (params) => client.get('/merchant/v3/debt/list', { params }),
  get: (id) => client.get(`/merchant/v3/debt/${id}`),
  getOutstanding: (merchantId) => client.get(`/merchant/v3/debt/outstanding/${merchantId}`),
  getByCard: (cardId) => client.get(`/merchant/v3/debt/card/${cardId}`),
  settle: (data) => client.post('/merchant/v3/debt/settle', data),
}

/**
 * Limit APIs (v3)
 * Controller: LimitController @ /merchant/v3/limit
 */
export const limitApi = {
  getHistory: (cardId, params) => client.get(`/merchant/v3/limit/card/${cardId}/history`, { params }),
  adjust: (cardId, data) => client.post(`/merchant/v3/limit/card/${cardId}/adjust`, data),
}

/**
 * 3DS OTP APIs
 * Controller: ThreeDsOtpController @ /merchant/3ds
 */
export const threeDsApi = {
  list: (params) => client.get('/merchant/3ds/list', { params }),
  get: (id) => client.get(`/merchant/3ds/${id}`),
}

export default {
  authApi,
  profileApi,
  cardholderApi,
  cardApi,
  cardKeyInfoApi,
  cardIssueApi,
  transactionApi,
  rechargeApi,
  accountApi,
  cardOperationApi,
  downloadApi,
  messageApi,
  debtApi,
  limitApi,
  threeDsApi,
}
