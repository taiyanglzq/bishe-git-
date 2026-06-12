/** request 工具模块，负责封装前端统一请求实例、Token 携带和异常处理逻辑。 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { canShowAuthExpiredTip, clearAuthStorage, getToken, isTokenExpired } from './auth'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token && isTokenExpired(token)) {
    if (canShowAuthExpiredTip()) {
      ElMessage.error('登录已过期，请重新登录')
    }
    clearAuthStorage()
    location.href = '/login'
    return Promise.reject(new Error('登录已过期，请重新登录'))
  }
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const result = response.data
    if (Number(result.code) !== 200) {
      ElMessage.error(result.message || '请求失败')
      return Promise.reject(result)
    }
    return result.data
  },
  (error) => {
    if (error.response?.status === 401) {
      if (canShowAuthExpiredTip()) {
        ElMessage.error('登录已过期，请重新登录')
      }
      clearAuthStorage()
      location.href = '/login'
    }
    ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
