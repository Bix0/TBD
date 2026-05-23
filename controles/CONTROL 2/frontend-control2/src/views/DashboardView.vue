<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api.js'

const router = useRouter()
const userName = ref('')
const userId = ref(null)

// Estados de Tareas y Filtros
const tasks = ref([])
const filterStatus = ref('Pendiente')
const searchKeyword = ref('')

// Formulario de Nueva Tarea
const newTask = ref({ title: '', description: '', dueDate: '', idGeoPoint: null })
const geoPoints = ref([])

// Reportes y Alertas
const alerts = ref([])
const closestTask = ref(null)
const avgDistance = ref(0)
const concentrationSectors = ref([])

onMounted(async () => {
  userName.value = localStorage.getItem('userName') || ''
  const storedUserId = localStorage.getItem('userId')
  if (!localStorage.getItem('token')) {
    router.push('/login')
    return
  }

  if (storedUserId) {
    userId.value = Number(storedUserId)
    loadReports()
  } else {
    // Decodificar el ID de usuario desde el flujo de autenticación o endpoint
    try {
      const userRes = await api.get('/users')
      if (userRes.data && Array.isArray(userRes.data)) {
        const currentUser = userRes.data.find(u => u.userName === userName.value)
        if (currentUser) {
          userId.value = currentUser.idUser
          localStorage.setItem('userId', currentUser.idUser)
          loadReports()
        }
      }
    } catch (e) {
      console.error("Error al cargar datos del usuario", e)
    }
  }

  loadTasks()
  loadGeoPoints()
  loadNotifications()
})

const loadTasks = async () => {
  try {
    const res = await api.get('/tasks', {
      params: { status: filterStatus.value, keyword: searchKeyword.value }
    })
    tasks.value = res.data || []
  } catch (e) {
    tasks.value = []
  }
}

const loadGeoPoints = async () => {
  try {
    const res = await api.get('/GeoPoints')
    geoPoints.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const loadNotifications = async () => {
  try {
    const res = await api.get('/notifications')
    alerts.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const loadReports = async () => {
  if (!userId.value) return
  try {
    const closest = await api.get(`/tasks/reports/closest-pending/${userId.value}`)
    closestTask.value = closest.status === 200 ? closest.data : null

    const distance = await api.get(`/tasks/reports/average-distance/${userId.value}`)
    avgDistance.value = distance.data || 0

    const concentration = await api.get('/tasks/reports/pending-concentration')
    concentrationSectors.value = concentration.data || []
  } catch (e) {
    console.error("Error cargando reportes espaciales", e)
  }
}

const handleCreateTask = async () => {
  if (!newTask.value.title || !newTask.value.idGeoPoint) return
  try {
    await api.post(`/tasks/createtask/${userId.value}`, newTask.value)
    newTask.value = { title: '', description: '', dueDate: '', idGeoPoint: null }
    loadTasks()
    loadReports()
  } catch (e) {
    alert("Error al crear la tarea")
  }
}

const handleCompleteTask = async (idTask) => {
  try {
    await api.put(`/tasks/completeTask/${idTask}/${userId.value}`)
    loadTasks()
    loadReports()
  } catch (e) {
    alert("Error al completar la tarea")
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userName')
  localStorage.removeItem('userId')
  router.push('/login')
}
</script>

<template>
  <div class="dashboard">
    <header class="dash-header">
      <h1>Bienvenido, {{ userName }}</h1>
      <button class="logout-btn" @click="handleLogout">Cerrar Sesión</button>
    </header>

    <!-- Barra Alertas Vencimiento Corto -->
    <div v-if="alerts.length > 0" class="alerts-banner">
      <p>⚠️ <strong>Tareas Próximas a vencer (Próximas 24 horas):</strong></p>
      <ul>
        <li v-for="alert in alerts" :key="alert.id">
          "{{ alert.title }}" - Estado: {{ alert.status }}
        </li>
      </ul>
    </div>

    <main class="dash-grid">
      <!-- Sección Izquierda: Gestión y Listado -->
      <section class="tasks-section">
        <div class="card">
          <h2>Crear Nueva Tarea</h2>
          <form @submit.prevent="handleCreateTask" class="form-grid">
            <input v-model="newTask.title" type="text" placeholder="Título de la tarea" required />
            <input v-model="newTask.description" type="text" placeholder="Descripción" />
            <input v-model="newTask.dueDate" type="date" required />
            
            <select v-model="newTask.idGeoPoint" required>
              <option :value="null" disabled selected>Selecciona Ubicación Geográfica / Sector</option>
              <option v-for="gp in geoPoints" :key="gp.idGeoPoint" :value="gp.idGeoPoint">
                {{ gp.name }} (Sector: {{ gp.sector }})
              </option>
            </select>
            <button type="submit" class="primary-btn">Añadir Tarea</button>
          </form>
        </div>

        <div class="card tasks-list-card">
          <div class="filters-bar">
            <h2>Listado de Tareas</h2>
            <div class="filter-controls">
              <input v-model="searchKeyword" @input="loadTasks" type="text" placeholder="Buscar por palabra clave..." />
              <select v-model="filterStatus" @change="loadTasks">
                <option value="Pendiente">Pendientes</option>
                <option value="Completada">Completadas</option>
              </select>
            </div>
          </div>

          <div v-if="tasks.length === 0" class="no-content">No se encontraron tareas con los filtros actuales.</div>
          <ul v-else class="tasks-list">
            <li v-for="task in tasks" :key="task.idTask" class="task-item">
              <div>
                <h3>{{ task.title }}</h3>
                <p>{{ task.description }}</p>
                <small>Vence el: {{ task.dueDate }} | Sector: {{ task.geoPoint?.sector }}</small>
              </div>
              <button v-if="task.status === 'Pendiente'" @click="handleCompleteTask(task.idTask)" class="complete-btn">
                ✓ Completar
              </button>
            </li>
          </ul>
        </div>
      </section>

      <!-- Sección Derecha: Reportes de Consultas PostGIS -->
      <section class="reports-section">
        <div class="card report-card highlight">
          <h2>📍 Tarea más Cercana Pendiente</h2>
          <div v-if="closestTask">
            <h3>{{ closestTask.title }}</h3>
            <p>{{ closestTask.description }}</p>
            <span class="badge">Prioritaria</span>
          </div>
          <p v-else class="no-content">No hay tareas pendientes en el radar.</p>
        </div>

        <div class="card report-card">
          <h2>📊 Métricas Generales (PostGIS)</h2>
          <div class="metric">
            <span class="metric-val">{{ (avgDistance / 1000).toFixed(2) }} km</span>
            <p>Distancia promedio a tus tareas completadas</p>
          </div>
        </div>

        <div class="card report-card">
          <h2>🔥 Concentración de Tareas Pendientes</h2>
          <p class="subtitle">Sectores espaciales con mayor acumulación:</p>
          <ul>
            <li v-for="sec in concentrationSectors" :key="sec.sector" class="sector-stat">
              <strong>{{ sec.sector || 'Sin sector' }}</strong>: {{ sec.total_pendientes }} pendientes
            </li>
          </ul>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.dashboard { min-height: 100vh; background: var(--bg); color: var(--text); }
.dash-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 32px; background: var(--code-bg); border-bottom: 1px solid var(--border); }
.logout-btn { padding: 8px 16px; background: transparent; border: 1px solid var(--border); color: var(--text-h); cursor: pointer; border-radius: 6px; }
.logout-btn:hover { background: var(--accent-bg); }
.alerts-banner { background: #ffeec2; color: #7c5e00; padding: 16px 32px; border-bottom: 1px solid #ffe0b2; }
.alerts-banner ul { margin: 4px 0 0; padding-left: 20px; }
.dash-grid { display: grid; grid-template-columns: 1fr 400px; gap: 24px; padding: 32px; max-width: 1400px; margin: 0 auto; }
.card { background: var(--code-bg); border: 1px solid var(--border); padding: 24px; border-radius: 12px; margin-bottom: 24px; box-shadow: var(--shadow); }
.form-grid { display: flex; flex-direction: column; gap: 12px; }
.form-grid input, .form-grid select { padding: 10px; border-radius: 6px; border: 1px solid var(--border); background: var(--bg); color: var(--text); }
.primary-btn { padding: 12px; background: var(--accent); color: white; border: none; border-radius: 6px; cursor: pointer; font-weight: bold; }
.filters-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px; }
.filter-controls { display: flex; gap: 12px; }
.filter-controls input, .filter-controls select { padding: 8px; border-radius: 6px; border: 1px solid var(--border); background: var(--bg); color: var(--text); }
.tasks-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 12px; }
.task-item { display: flex; justify-content: space-between; align-items: center; padding: 16px; background: var(--bg); border: 1px solid var(--border); border-radius: 8px; }
.complete-btn { padding: 6px 12px; background: #00e676; color: #121212; border: none; border-radius: 6px; cursor: pointer; font-weight: bold; }
.complete-btn:hover { opacity: 0.9; }
.highlight { border-left: 4px solid var(--accent); }
.badge { display: inline-block; background: #ff5252; color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; margin-top: 8px; }
.metric { text-align: center; padding: 16px 0; }
.metric-val { font-size: 36px; font-weight: bold; color: var(--accent); }
.sector-stat { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid var(--border); }
.no-content { color: var(--text); font-style: italic; text-align: center; padding: 12px 0; }
@media (max-width: 900px) { .dash-grid { grid-template-columns: 1fr; } }
</style>
