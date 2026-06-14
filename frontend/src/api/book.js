/** 图书 API，封装图书检索和管理相关接口调用。 */
import request from '../utils/request'

export function getBookPage(params) {
  return request.get('/book/page', { params })
}

export function getBookDetail(id) {
  return request.get(`/book/${id}`)
}

export function getBookManagePage(params) {
  return request.get('/book/manage/page', { params })
}

export function createBook(data) {
  return request.post('/book', data)
}

export function updateBook(data) {
  return request.put('/book', data)
}

export function deleteBook(id) {
  return request.delete(`/book/${id}`)
}

export function borrowBook(data) {
  return request.post('/book/borrow', data)
}

export function returnBook(bookId) {
  return request.post(`/book/return/${bookId}`)
}

export function getMyBorrows() {
  return request.get('/book/my-borrows')
}

export function getBorrowPage(params) {
  return request.get('/book/borrow/page', { params })
}
