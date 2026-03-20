import client from './client'

/**
 * Authentication APIs
 */
export const authApi = {
  // User login
  login: (credentials) => client.post('/auth/login', credentials),
  
  // User registration
  register: (userData) => client.post('/auth/register', userData),
  
  // Logout
  logout: () => client.post('/auth/logout'),
  
  // Get current user info
  getCurrentUser: () => client.get('/auth/profile'),
  
  // Refresh token
  refreshToken: () => client.post('/auth/refresh'),
}

/**
 * Cardholder APIs
 */
export const cardholderApi = {
  // Get cardholder list
  list: (params) => client.get('/cardholders', { params }),
  
  // Get cardholder details
  get: (id) => client.get(`/cardholders/${id}`),
  
  // Create new cardholder
  create: (data) => client.post('/cardholders', data),
  
  // Update cardholder
  update: (id, data) => client.put(`/cardholders/${id}`, data),
  
  // Delete cardholder
  delete: (id) => client.delete(`/cardholders/${id}`),
}

/**
 * Card APIs
 */
export const cardApi = {
  // Get card list
  list: (params) => client.get('/cards', { params }),
  
  // Get card details
  get: (id) => client.get(`/cards/${id}`),
  
  // Get card three factors (require verification)
  getThreeFactors: (id) => client.get(`/cards/${id}/three-factors`),
  
  // Create card application
  apply: (data) => client.post('/card-applications', data),
  
  // Get card applications
  getApplications: (params) => client.get('/card-applications', { params }),
  
  // Get application details
  getApplication: (id) => client.get(`/card-applications/${id}`),
  
  // Recharge card
  recharge: (id, data) => client.post(`/cards/${id}/recharge`, data),
  
  // Freeze card
  freeze: (id) => client.post(`/cards/${id}/freeze`),
  
  // Unfreeze card
  unfreeze: (id) => client.post(`/cards/${id}/unfreeze`),
  
  // Cancel card
  cancel: (id) => client.post(`/cards/${id}/cancel`),
}

/**
 * Transaction APIs
 */
export const transactionApi = {
  // Get transaction list
  list: (params) => client.get('/transactions', { params }),
  
  // Get transaction details
  get: (id) => client.get(`/transactions/${id}`),
  
  // Export transactions
  export: (params) => client.get('/transactions/export', { params, responseType: 'blob' }),
}

/**
 * User Profile APIs
 */
export const profileApi = {
  // Get profile
  get: () => client.get('/profile'),
  
  // Update profile
  update: (data) => client.put('/profile', data),
  
  // Change password
  changePassword: (data) => client.post('/profile/change-password', data),
  
  // Update notification preferences
  updateNotifications: (data) => client.put('/profile/notifications', data),
}

export default {
  authApi,
  cardholderApi,
  cardApi,
  transactionApi,
  profileApi,
}
