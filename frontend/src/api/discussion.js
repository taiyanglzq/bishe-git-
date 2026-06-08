/** ???? API ?????????????????? */
import request from '../utils/request'

export function getDiscussionPage(params) {
  return request.get('/discussion/page', { params })
}

export function createDiscussionPost(data) {
  return request.post('/discussion/post', data)
}

export function createDiscussionComment(data) {
  return request.post('/discussion/comment', data)
}

export function toggleDiscussionLike(postId) {
  return request.post(`/discussion/like/${postId}`)
}

export function toggleDiscussionCommentLike(commentId) {
  return request.post(`/discussion/comment/like/${commentId}`)
}

export function deleteDiscussionPost(id) {
  return request.delete(`/discussion/post/${id}`)
}

export function deleteDiscussionComment(id) {
  return request.delete(`/discussion/comment/${id}`)
}

export function updateDiscussionPin(data) {
  return request.put('/discussion/pin', data)
}

export function updateDiscussionFeature(data) {
  return request.put('/discussion/feature', data)
}

export function banDiscussionUser(data) {
  return request.post('/discussion/ban', data)
}
