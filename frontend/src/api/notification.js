/** ?? API ???????????????? */
import request from '../utils/request'

export function getNotificationPage(params) {
  return request.get('/notification/page', { params })
}

export function getUnreadCount() {
  return request.get('/notification/unread-count')
}

export function markNotificationRead(id) {
  return request.put(`/notification/read/${id}`)
}

export function markAllNotificationRead() {
  return request.put('/notification/read-all')
}
