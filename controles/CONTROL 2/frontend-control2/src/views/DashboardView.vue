<script setup>
import { ref, onMounted, nextTick } from "vue";
import TaskCard from "../components/TaskCard.vue";
import ReportCard from "../components/ReportCard.vue";
import GeoPointManager from "../components/GeoPointManager.vue";
import { useRouter } from "vue-router";
import api from "../services/api.js";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

// Fix del ícono de marcador de leaflet transparente
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
    iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
    shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
});

const router = useRouter();
const userName = ref("");
const userId = ref(null);

// Estados de Tareas y Filtros
const tasks = ref([]);
const filterStatus = ref("Pendiente");
const searchKeyword = ref("");

// Formulario de Nueva Tarea
const newTask = ref({
    title: "",
    description: "",
    dueDate: "",
    idGeoPoint: null,
});
const geoPoints = ref([]);

// Reportes y Alertas
const alerts = ref([]);
const closestTask = ref(null);
const avgDistance = ref(0);
const concentrationSectors = ref([]);
// Reportes faltantes
const topSector = ref(null);
const topSectorRadius = ref(5);
const userSectorCounts = ref([]);
const showGeoManager = ref(false);

// --- ESTADOS Y LÓGICA PARA EL MODAL DEL MAPA ---
const showMapModal = ref(false);
const selectedMapTask = ref(null);
let viewMapInstance = null;

onMounted(async () => {
    userName.value = localStorage.getItem("userName") || "";
    const storedUserId = localStorage.getItem("userId");
    if (!localStorage.getItem("token")) {
        router.push("/login");
        return;
    }

    if (storedUserId) {
        userId.value = Number(storedUserId);
        loadReports();
    } else {
        try {
            const userRes = await api.get("/users");
            if (userRes.data && Array.isArray(userRes.data)) {
                const currentUser = userRes.data.find(
                    (u) => u.userName === userName.value,
                );
                if (currentUser) {
                    userId.value = currentUser.idUser;
                    localStorage.setItem("userId", currentUser.idUser);
                    loadReports();
                }
            }
        } catch (e) {
            console.error("Error al cargar datos del usuario", e);
        }
    }

    loadTasks();
    loadGeoPoints();
    loadNotifications();
});

const loadTasks = async () => {
    try {
        const res = await api.get("/tasks", {
            params: {
                status: filterStatus.value,
                keyword: searchKeyword.value,
            },
        });
        tasks.value = res.data || [];
    } catch (e) {
        tasks.value = [];
    }
};

const loadGeoPoints = async () => {
    try {
        const res = await api.get("/GeoPoints");
        geoPoints.value = res.data || [];
    } catch (e) {
        console.error(e);
    }
};

const loadNotifications = async () => {
    try {
        const res = await api.get("/notifications");
        alerts.value = res.data || [];
    } catch (e) {
        console.error(e);
    }
};

const loadReports = async () => {
    if (!userId.value) return;
    try {
        const closest = await api.get(
            `/tasks/reports/closest-pending/${userId.value}`,
        );
        closestTask.value = closest.status === 200 ? closest.data : null;

        const distance = await api.get(
            `/tasks/reports/average-distance/${userId.value}`,
        );
        avgDistance.value = distance.data || 0;

        const concentration = await api.get(
            "/tasks/reports/pending-concentration",
        );
        concentrationSectors.value = concentration.data || [];

        loadTopSector();
        loadUserSectorCounts();
    } catch (e) {
        console.error("Error cargando reportes espaciales", e);
    }
};

const loadTopSector = async () => {
    if (!userId.value) return;
    try {
        const res = await api.get(
            `/tasks/reports/most-completed-sector/${userId.value}/${topSectorRadius.value}`,
        );
        topSector.value = res.status === 200 ? res.data : null;
    } catch (e) {
        topSector.value = null;
    }
};

const loadUserSectorCounts = async () => {
    if (!userId.value) return;
    try {
        const res = await api.get(
            `/tasks/reports/user-sector-counts/${userId.value}`,
        );
        userSectorCounts.value = res.data || [];
    } catch (e) {
        userSectorCounts.value = [];
    }
};

const handleCreateTask = async () => {
    if (!newTask.value.title || !newTask.value.idGeoPoint) return;
    try {
        await api.post(`/tasks/createtask/${userId.value}`, newTask.value);
        newTask.value = {
            title: "",
            description: "",
            dueDate: "",
            idGeoPoint: null,
        };
        loadTasks();
        loadReports();
    } catch (e) {
        alert("Error al crear la tarea");
    }
};

const handleCompleteTask = async (idTask) => {
    try {
        await api.put(`/tasks/completeTask/${idTask}/${userId.value}`);
        loadTasks();
        loadReports();
    } catch (e) {
        alert("Error al completar la tarea");
    }
};

const handleSaveEdit = async (idTask, formData) => {
    try {
        await api.put(`/tasks/modifytask/${idTask}`, formData);
        loadTasks();
        loadReports();
    } catch (e) {
        alert("Error al modificar la tarea");
    }
};

const handleDeleteTask = async (idTask) => {
    try {
        await api.delete(`/tasks/deletetask/${idTask}/${userId.value}`);
        loadTasks();
        loadReports();
    } catch (e) {
        alert("Error al eliminar la tarea");
    }
};

// --- FUNCIONES DEL MODAL DEL MAPA ---
const handleViewMap = async (task) => {
    if (!task.geoPoint || task.geoPoint.latitude == null || task.geoPoint.longitude == null) {
        alert("Esta tarea no tiene coordenadas geográficas registradas válidas.");
        return;
    }

    selectedMapTask.value = task;
    showMapModal.value = true; // Abre el modal

    // nextTick permite que Vue renderice el <div id="task-map-container"> antes de inyectar Leaflet
    await nextTick();

    const latLng = [task.geoPoint.latitude, task.geoPoint.longitude];

    // Inicializamos el mapa en el contenedor del modal
    viewMapInstance = L.map("task-map-container").setView(latLng, 16);
    
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: "&copy; OpenStreetMap contributors",
    }).addTo(viewMapInstance);

    L.marker(latLng).addTo(viewMapInstance);

    // InvalidateSize asegura que los tiles del mapa carguen correctamente al abrirse dentro de un Modal
    setTimeout(() => {
        if(viewMapInstance) viewMapInstance.invalidateSize();
    }, 150);
};

const closeMapModal = () => {
    showMapModal.value = false;
    selectedMapTask.value = null;
    
    // Es vital destruir la instancia al cerrar para evitar el error "Map container is already initialized"
    if (viewMapInstance) {
        viewMapInstance.remove();
        viewMapInstance = null;
    }
};

const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userName");
    localStorage.removeItem("userId");
    router.push("/login");
};
</script>

<template>
    <div class="dashboard">

        <div v-if="showMapModal" class="modal-overlay" @click.self="closeMapModal">
            <div class="modal-content">
                <h3>Ubicación: {{ selectedMapTask?.title }}</h3>
                <p style="margin-bottom: 12px; color: var(--text-h)">
                    📍 Sector: {{ selectedMapTask?.geoPoint?.sector || 'Sin sector' }}
                </p>
                <div id="task-map-container" style="height: 350px; width: 100%; border-radius: 8px; z-index: 1;"></div>
                <button class="primary-btn close-btn" @click="closeMapModal">Cerrar Mapa</button>
            </div>
        </div>

        <header class="dash-header">
            <h1>Bienvenido, {{ userName }}</h1>
            <button class="logout-btn" @click="handleLogout">
                Cerrar Sesión
            </button>
        </header>

        <div v-if="alerts.length > 0" class="alerts-banner">
            <p>
                <strong>Tareas Proximas a vencer (Proximas 24 horas):</strong>
            </p>
            <ul>
                <li v-for="alert in alerts" :key="alert.id">
                    "{{ alert.title }}" - Estado: {{ alert.status }}
                </li>
            </ul>
        </div>

        <main class="dash-grid">
            <section class="tasks-section">
                <div class="card">
                    <h2>Crear Nueva Tarea</h2>
                    <form @submit.prevent="handleCreateTask" class="form-grid">
                        <input
                            v-model="newTask.title"
                            type="text"
                            placeholder="Título de la tarea"
                            required
                        />
                        <input
                            v-model="newTask.description"
                            type="text"
                            placeholder="Descripción"
                        />
                        <input v-model="newTask.dueDate" type="date" required />

                        <select v-model="newTask.idGeoPoint" required>
                            <option :value="null" disabled selected>
                                Selecciona Ubicación Geográfica / Sector
                            </option>
                            <option
                                v-for="gp in geoPoints"
                                :key="gp.idGeoPoint"
                                :value="gp.idGeoPoint"
                            >
                                {{ gp.name }} (Sector: {{ gp.sector }})
                            </option>
                        </select>
                        <button type="submit" class="primary-btn">
                            Añadir Tarea
                        </button>
                    </form>
                </div>

                <div class="section-tabs">
                    <button
                        :class="{ 'tab-active': !showGeoManager }"
                        @click="showGeoManager = false"
                    >
                        Tareas
                    </button>
                    <button
                        :class="{ 'tab-active': showGeoManager }"
                        @click="showGeoManager = true"
                    >
                        Ubicaciones
                    </button>
                </div>

                <div v-if="!showGeoManager" class="card tasks-list-card">
                    <div class="filters-bar">
                        <h2>Listado de Tareas</h2>
                        <div class="filter-controls">
                            <input
                                v-model="searchKeyword"
                                @input="loadTasks"
                                type="text"
                                placeholder="Buscar por palabra clave..."
                            />
                            <select v-model="filterStatus" @change="loadTasks">
                                <option value="Pendiente">Pendientes</option>
                                <option value="Completada">Completadas</option>
                            </select>
                        </div>
                    </div>

                    <div v-if="tasks.length === 0" class="no-content">
                        No se encontraron tareas con los filtros actuales.
                    </div>
                    <ul v-else class="tasks-list">
                        <TaskCard
                            v-for="task in tasks"
                            :key="task.idTask"
                            :task="task"
                            :geo-points="geoPoints"
                            :user-id="userId"
                            @save-edit="handleSaveEdit"
                            @delete-task="handleDeleteTask"
                            @complete-task="handleCompleteTask"
                            @view-map="handleViewMap"
                        />
                    </ul>
                </div>

                <GeoPointManager
                    v-if="showGeoManager"
                    :user-id="userId"
                    @geo-points-changed="loadGeoPoints"
                />
            </section>

            <section class="reports-section">
                <ReportCard
                    title="Tarea mas Cercana Pendiente"
                    :highlight="true"
                >
                    <div v-if="closestTask">
                        <h3>{{ closestTask.title }}</h3>
                        <p>{{ closestTask.description }}</p>
                        <span class="badge">Prioritaria</span>
                    </div>
                    <p v-else class="no-content">
                        No hay tareas pendientes en el radar.
                    </p>
                </ReportCard>

                <ReportCard title="Tus Tareas completadas por Sector">
                    <div v-if="userSectorCounts.length > 0">
                        <ul>
                            <li
                                v-for="stat in userSectorCounts"
                                :key="stat.sector"
                                class="sector-stat"
                            >
                                <strong>{{
                                    stat.sector || "Sin sector"
                                }}</strong
                                >: {{ stat.total_tareas }} tareas
                            </li>
                        </ul>
                    </div>
                    <p v-else class="no-content">
                        Sin datos de tareas por sector.
                    </p>
                </ReportCard>

                <ReportCard title="Sector con mas Completadas">
                    <div v-if="topSector">
                        <div class="metric">
                            <span class="metric-val">{{
                                topSector.sector || "---"
                            }}</span>
                            <p>
                                {{ topSector.total_tareas || 0 }} tareas
                                completadas
                            </p>
                        </div>
                        <div class="radius-control">
                            <label>Radio de busqueda:</label>
                            <select
                                v-model.number="topSectorRadius"
                                @change="loadTopSector"
                            >
                                <option :value="1">1 km</option>
                                <option :value="2">2 km</option>
                                <option :value="5">5 km</option>
                                <option :value="10">10 km</option>
                            </select>
                        </div>
                    </div>
                    <p v-else class="no-content">
                        No hay datos de sector con mas completadas.
                    </p>
                </ReportCard>

                <ReportCard title="Metricas Generales (PostGIS)">
                    <div class="metric">
                        <span class="metric-val"
                            >{{ (avgDistance / 1000).toFixed(2) }} km</span
                        >
                        <p>Distancia promedio a tus tareas completadas</p>
                    </div>
                </ReportCard>

                <ReportCard title="Concentracion de Tareas Pendientes">
                    <p class="subtitle">
                        Sectores espaciales con mayor acumulacion:
                    </p>
                    <ul>
                        <li
                            v-for="sec in concentrationSectors"
                            :key="sec.sector"
                            class="sector-stat"
                        >
                            <strong>{{ sec.sector || "Sin sector" }}</strong
                            >: {{ sec.total_pendientes }} pendientes
                        </li>
                    </ul>
                </ReportCard>
            </section>
        </main>
    </div>
</template>

<style scoped>
/* ESTILOS DEL MODAL DE MAPA */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.7);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
}
.modal-content {
    background: var(--code-bg);
    padding: 24px;
    border-radius: 12px;
    width: 90%;
    max-width: 600px;
    border: 1px solid var(--border);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}
.modal-content h3 {
    margin: 0 0 4px 0;
    color: var(--accent);
}
.close-btn {
    margin-top: 16px;
    width: 100%;
    background: #ff5252;
}
.close-btn:hover {
    background: #ff1744;
}

/* ESTILOS GENERALES MANTENIDOS */
.dashboard {
    min-height: 100vh;
    background: var(--bg);
    color: var(--text);
}
.dash-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 32px;
    background: var(--code-bg);
    border-bottom: 1px solid var(--border);
}
.logout-btn {
    padding: 8px 16px;
    background: transparent;
    border: 1px solid var(--border);
    color: var(--text-h);
    cursor: pointer;
    border-radius: 6px;
}
.logout-btn:hover {
    background: var(--accent-bg);
}
.alerts-banner {
    background: #ffeec2;
    color: #7c5e00;
    padding: 16px 32px;
    border-bottom: 1px solid #ffe0b2;
}
.alerts-banner ul {
    margin: 4px 0 0;
    padding-left: 20px;
}
.dash-grid {
    display: grid;
    grid-template-columns: 1fr 400px;
    gap: 24px;
    padding: 32px;
    max-width: 1400px;
    margin: 0 auto;
}
.card {
    background: var(--code-bg);
    border: 1px solid var(--border);
    padding: 24px;
    border-radius: 12px;
    margin-bottom: 24px;
    box-shadow: var(--shadow);
}
.form-grid {
    display: flex;
    flex-direction: column;
    gap: 12px;
}
.form-grid input,
.form-grid select {
    padding: 10px;
    border-radius: 6px;
    border: 1px solid var(--border);
    background: var(--bg);
    color: var(--text);
}
.primary-btn {
    padding: 12px;
    background: var(--accent);
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: bold;
}
.filters-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    flex-wrap: wrap;
    gap: 12px;
}
.filter-controls {
    display: flex;
    gap: 12px;
}
.filter-controls input,
.filter-controls select {
    padding: 8px;
    border-radius: 6px;
    border: 1px solid var(--border);
    background: var(--bg);
    color: var(--text);
}
.tasks-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 12px;
}
.badge {
    display: inline-block;
    background: #ff5252;
    color: white;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    margin-top: 8px;
}
.metric {
    text-align: center;
    padding: 16px 0;
}
.metric-val {
    font-size: 36px;
    font-weight: bold;
    color: var(--accent);
}
.sector-stat {
    display: flex;
    justify-content: space-between;
    padding: 6px 0;
    border-bottom: 1px solid var(--border);
}
.no-content {
    color: var(--text);
    font-style: italic;
    text-align: center;
    padding: 12px 0;
}
.radius-control {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid var(--border);
}
.radius-control label {
    font-size: 13px;
    color: var(--text);
}
.radius-control select {
    padding: 4px 8px;
    border-radius: 6px;
    border: 1px solid var(--border);
    background: var(--bg);
    color: var(--text);
    font-size: 13px;
}
.section-tabs {
    display: flex;
    gap: 0;
    margin-bottom: 16px;
    border: 1px solid var(--border);
    border-radius: 8px;
    overflow: hidden;
}
.section-tabs button {
    flex: 1;
    padding: 10px 16px;
    background: var(--code-bg);
    border: none;
    color: var(--text);
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
}
.section-tabs button:not(:last-child) {
    border-right: 1px solid var(--border);
}
.section-tabs button.tab-active {
    background: var(--accent);
    color: white;
}
@media (max-width: 900px) {
    .dash-grid {
        grid-template-columns: 1fr;
    }
}
</style>