import request from '../utils/request'

export function getDashboardSummary() {
  return request.get('/dashboard/summary')
}

export function getDashboardStats() {
  return request.get('/dashboard/stats')
}

export function getDashboardWorkbench() {
  return request.get('/dashboard/workbench')
}
