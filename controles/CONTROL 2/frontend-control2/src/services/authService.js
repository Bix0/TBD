import api from './api'

export const authService = {
  async login(userName, password) {
    const response = await api.post('/auth/login', { userName, password })
    return response.data
  },

  async register(userName, password, locationUser) {
    const response = await api.post('/auth/register', {
      userName,
      password,
      locationUser
    })
    return response.data
  }
}
