<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userName = ref('')

onMounted(() => {
  userName.value = localStorage.getItem('userName') || ''
  if (!localStorage.getItem('token')) {
    router.push('/login')
  }
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userName')
  router.push('/login')
}
</script>

<template>
  <div class="dashboard">
    <header class="dash-header">
      <h1>Bienvenido, {{ userName }}</h1>
      <button class="logout-btn" @click="handleLogout">Cerrar Sesion</button>
    </header>

    <main class="dash-main">
      <div class="placeholder-card">
        <h2>Panel de Tareas</h2>
        <p>Proximamente: gestion de tareas, filtros y notificaciones.</p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.dashboard {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.dash-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 32px;
  border-bottom: 1px solid var(--border);
  background: var(--code-bg);
}

.dash-header h1 {
  font-size: 24px;
  margin: 0;
}

.logout-btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text-h);
  font-size: 14px;
  cursor: pointer;
  font-family: var(--sans);
  transition: background 0.2s;
}

.logout-btn:hover {
  background: var(--accent-bg);
}

.dash-main {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32px;
}

.placeholder-card {
  background: var(--code-bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 48px;
  text-align: center;
  max-width: 500px;
  box-shadow: var(--shadow);
}

.placeholder-card h2 {
  margin: 0 0 12px;
}

.placeholder-card p {
  color: var(--text);
  font-size: 15px;
}
</style>
