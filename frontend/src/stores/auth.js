import { defineStore } from 'pinia'
import { currentUser, login as loginApi } from '../api/auth'
import { removeToken, setToken } from '../utils/auth'

const USER_KEY = 'campus_assistant_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  }),
  actions: {
    async login(form) {
      const data = await loginApi(form)
      setToken(data.token)
      this.user = data
      localStorage.setItem(USER_KEY, JSON.stringify(data))
      return data
    },
    async fetchCurrentUser() {
      this.user = await currentUser()
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      return this.user
    },
    logout() {
      removeToken()
      localStorage.removeItem(USER_KEY)
      this.user = null
    }
  }
})
