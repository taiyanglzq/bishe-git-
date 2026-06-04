import request from '../utils/request'

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getVenuePage(params) {
  return request.get('/venue/page', { params })
}

export function createVenue(data) {
  return request.post('/venue', data)
}

export function updateVenue(data) {
  return request.put('/venue', data)
}

export function deleteVenue(id) {
  return request.delete(`/venue/${id}`)
}

export function getVenueSlotPage(params) {
  return request.get('/venue-slot/page', { params })
}

export function createVenueSlot(data) {
  return request.post('/venue-slot', data)
}

export function updateVenueSlot(data) {
  return request.put('/venue-slot', data)
}

export function deleteVenueSlot(id) {
  return request.delete(`/venue-slot/${id}`)
}
