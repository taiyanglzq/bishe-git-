/** ?? API ???????????????? */
import request from '../utils/request'

export function getNoticePage(params) {
  return request.get('/notice/page', { params })
}

export function getNoticeDetail(id) {
  return request.get(`/notice/${id}`)
}

export function createNoticeComment(data) {
  return request.post('/notice/comment', data)
}

export function getNoticeManagePage(params) {
  return request.get('/notice/manage/page', { params })
}

export function createNotice(data) {
  return request.post('/notice', data)
}

export function updateNotice(data) {
  return request.put('/notice', data)
}

export function deleteNotice(id) {
  return request.delete(`/notice/${id}`)
}

export function approveNotice(id) {
  return request.put(`/notice/approve/${id}`)
}

export function rejectNotice(id) {
  return request.put(`/notice/reject/${id}`)
}
