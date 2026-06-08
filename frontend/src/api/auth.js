/** ?? API ???????????????? */
import request from '../utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function currentUser() {
  return request.get('/user/current')
}

export function updatePassword(data) {
  return request.put('/user/password', data)
}

export function getUserPage(params) {
  return request.get('/user/page', { params })
}

export function createUser(data) {
  return request.post('/user', data)
}

export function updateUser(data) {
  return request.put('/user', data)
}

export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}
