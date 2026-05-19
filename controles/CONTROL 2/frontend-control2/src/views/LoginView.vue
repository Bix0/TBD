<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authService } from '../services/authService.js'

const router = useRouter()

const userName = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const handleLogin = async () => {
  error.value = ''
  if (!userName.value || !password.value) {
    error.value = 'Completa todos los campos'
    return
  }

  loading.value = true
  try {
    const response = await authService.login(userName.value, password.value)
    localStorage.setItem('token', response.token)
    localStorage.setItem('userName', response.userName)
    router.push('/dashboard')
  } catch (e) {
    error.value = 'Usuario o contrasenia incorrectos'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-card">
      <h1>Iniciar Sesion</h1>
      <p class="subtitle">Ingresa tus credenciales para acceder</p>

      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="field">
          <label for="userName">Usuario</label>
          <input
            id="userName"
            v-model="userName"
            type="text"
            placeholder="tu usuario"
            autocomplete="username"
          />
        </div>

        <div class="field">
          <label for="password">Contrasenia</label>
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="tu contrasenia"
            autocomplete="current-password"
          />
        </div>

        <p v-if="error" class="error-msg">{{ error }}</p>

        <button type="submit" class="auth-btn" :disabled="loading">
          {{ loading ? 'Ingresando...' : 'Ingresar' }}
        </button>
      </form>

      <p class="switch-link">
        ¿No tienes cuenta?
        <router-link to="/register">Registrate</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  box-sizing: border-box;
}

.auth-card {
  background: var(--code-bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: var(--shadow);
}

.auth-card h1 {
  font-size: 32px;
  margin: 0 0 4px;
  text-align: center;
}

.subtitle {
  text-align: center;
  margin-bottom: 28px;
  font-size: 15px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  text-align: left;
}

.field label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-h);
}

.field input {
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: var(--bg);
  color: var(--text-h);
  font-size: 16px;
  font-family: var(--sans);
  outline: none;
  transition: border-color 0.2s;
}

.field input:focus {
  border-color: var(--accent);
}

.error-msg {
  color: #ef4444;
  font-size: 14px;
  text-align: center;
}

.auth-btn {
  padding: 12px;
  border-radius: 8px;
  border: none;
  background: var(--accent);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  font-family: var(--sans);
  cursor: pointer;
  transition: opacity 0.2s;
}

.auth-btn:hover {
  opacity: 0.9;
}

.auth-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.switch-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
}

.switch-link a {
  color: var(--accent);
  text-decoration: none;
  font-weight: 600;
}

.switch-link a:hover {
  text-decoration: underline;
}
</style>
