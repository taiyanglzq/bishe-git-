/** ???? API ?????????????????? */
import request from '../utils/request'

export function getBookingPage(params) {
  return request.get('/booking/page', { params })
}

export function createBooking(data) {
  return request.post('/booking', data)
}

export function approveBooking(data) {
  return request.put('/booking/approve', data)
}

export function rejectBooking(data) {
  return request.put('/booking/reject', data)
}

export function cancelBooking(id) {
  return request.post(`/booking/cancel/${id}`)
}
