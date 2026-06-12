/** AI API，封装AI对话和会话管理相关接口调用。 */
import request from '../utils/request'

export function chatWithAi(question, sessionId) {
  return request.post('/ai/chat', { question, sessionId })
}

export function getAiSessions() {
  return request.get('/ai/sessions')
}

export function getAiSessionHistory(sessionId) {
  return request.get(`/ai/sessions/${sessionId}/history`)
}

export function deleteAiSession(sessionId) {
  return request.delete(`/ai/sessions/${sessionId}`)
}

export function chatWithImage(question, images, sessionId) {
  return request.post('/ai/chat/multimodal', { question, images, sessionId })
}

export function getLearningAdvice(type) {
  return request.post('/ai/learning-advice', { type })
}

export function previewModeration(content, scene) {
  return request.post('/ai/moderate/preview', { content, scene })
}
