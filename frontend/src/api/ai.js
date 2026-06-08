/** ai API ???????ai??????? */
import request from '../utils/request'

export function chatWithAi(question) {
  return request.post('/ai/chat', { question })
}

export function previewModeration(content, scene) {
  return request.post('/ai/moderate/preview', { content, scene })
}
