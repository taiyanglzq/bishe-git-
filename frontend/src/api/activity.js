import request from '../utils/request'

export function getActivityPage(params) {
  return request.get('/activity/page', { params })
}

export function getActivityManagePage(params) {
  return request.get('/activity/manage/page', { params })
}

export function enrollActivity(data) {
  return request.post('/activity/enroll', data)
}

export function checkinActivity(data) {
  return request.post('/checkin', data)
}

export function createActivity(data) {
  return request.post('/activity', data)
}

export function updateActivity(data) {
  return request.put('/activity', data)
}

export function deleteActivity(id) {
  return request.delete(`/activity/${id}`)
}

export function cancelEnroll(activityId) {
  return request.post(`/activity/cancel/${activityId}`)
}

export function getMyEnrollments(params) {
  return request.get('/activity/my-enrollments', { params })
}

export function getMyCheckins(params) {
  return request.get('/activity/my-checkins', { params })
}
