/** 认证工具模块，负责处理前端 Token 的存储、清理和过期判断逻辑。 */
const TOKEN_KEY = 'campus_assistant_token'
const USER_KEY = 'campus_assistant_user'
const AUTH_EXPIRED_TIP_KEY = 'campus_assistant_auth_expired_tip_time'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function removeUserCache() {
  localStorage.removeItem(USER_KEY)
}

export function clearAuthStorage() {
  removeToken()
  removeUserCache()
}

export function canShowAuthExpiredTip() {
  const now = Date.now()
  const last = Number(sessionStorage.getItem(AUTH_EXPIRED_TIP_KEY) || 0)
  if (now - last < 1500) {
    return false
  }
  sessionStorage.setItem(AUTH_EXPIRED_TIP_KEY, String(now))
  return true
}

export function parseJwtPayload(token) {
  if (!token) return null
  const parts = token.split('.')
  if (parts.length < 2) return null
  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const normalized = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
    const json = decodeURIComponent(
      atob(normalized)
        .split('')
        .map((char) => `%${char.charCodeAt(0).toString(16).padStart(2, '0')}`)
        .join('')
    )
    return JSON.parse(json)
  } catch (error) {
    return null
  }
}

export function isTokenExpired(token = getToken()) {
  const payload = parseJwtPayload(token)
  if (!payload?.exp) return false
  return payload.exp * 1000 <= Date.now()
}
