/** 课程 API，封装课程查询和考试查询相关接口调用。 */
import request from '../utils/request'

export function getCoursePage(params) {
  return request.get('/course/page', { params })
}

export function getCourseDetail(id) {
  return request.get(`/course/${id}`)
}

export function getCourseManagePage(params) {
  return request.get('/course/manage/page', { params })
}

export function createCourse(data) {
  return request.post('/course', data)
}

export function updateCourse(data) {
  return request.put('/course', data)
}

export function deleteCourse(id) {
  return request.delete(`/course/${id}`)
}

export function getExamPage(params) {
  return request.get('/exam/page', { params })
}

export function getExamDetail(id) {
  return request.get(`/exam/${id}`)
}

export function getExamManagePage(params) {
  return request.get('/exam/manage/page', { params })
}

export function createExam(data) {
  return request.post('/exam', data)
}

export function updateExam(data) {
  return request.put('/exam', data)
}

export function deleteExam(id) {
  return request.delete(`/exam/${id}`)
}

// ====== 座位管理 ======
export function getExamSeats(examId) {
  return request.get(`/exam/${examId}/seats`)
}

export function generateExamSeats(examId, data) {
  return request.post(`/exam/${examId}/seats/generate`, data)
}

export function saveExamSeats(examId, seats) {
  return request.put(`/exam/${examId}/seats/save`, seats)
}

export function updateExamSeat(seatId, seatNo) {
  return request.put(`/exam/seats/${seatId}?seatNo=${encodeURIComponent(seatNo)}`)
}

export function getExamSeatsExportUrl(examId) {
  return `/exam/${examId}/seats/export`
}

export function getMyExamSeats() {
  return request.get('/exam/seats/my')
}
