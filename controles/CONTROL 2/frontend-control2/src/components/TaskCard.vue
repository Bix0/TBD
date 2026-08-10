<script setup>
import { ref } from "vue";

const props = defineProps({
    task: { type: Object, required: true },
    geoPoints: { type: Array, default: () => [] },
    userId: { type: Number, required: true },
});

const emit = defineEmits([
    "save-edit",
    "delete-task",
    "complete-task",
    "view-map",
]);

const isEditing = ref(false);
const editForm = ref({
    title: "",
    description: "",
    dueDate: "",
    idGeoPoint: null,
});
const today = new Date().toISOString().split("T")[0];

// FUNCIÓN NUEVA: Limpia la fecha que envía el backend (Corta todo lo que está después de la 'T')
const formatForDisplay = (isoString) => {
    if (!isoString) return "Sin fecha";
    return isoString.split("T")[0];
};

	const startEditing = () => {
	    let dueDate = props.task.dueDate ? props.task.dueDate.split("T")[0] : "";
	    editForm.value = {
	        title: props.task.title,
	        description: props.task.description,
	        dueDate: dueDate,
	        idGeoPoint: props.task.geoPoint?.idGeoPoint || null,
	    };
	    isEditing.value = true;
	};

const cancelEditing = () => {
    isEditing.value = false;
};

	const saveEdit = () => {
	    if (!editForm.value.title || !editForm.value.idGeoPoint) return;
	    const payload = { ...editForm.value };
	    emit("save-edit", props.task.idTask, payload);
	    isEditing.value = false;
	};

const confirmDelete = () => {
    if (confirm("¿Estas seguro de eliminar esta tarea?")) {
        emit("delete-task", props.task.idTask);
    }
};

const completeTask = () => {
    emit("complete-task", props.task.idTask);
};

const viewMap = () => {
    emit("view-map", props.task);
};
</script>

<template>
    <li
        class="task-item"
        :class="{ 'completed-style': task.status === 'Completada' }"
    >
        <div v-if="isEditing" class="edit-form-inline">
            <input
                v-model="editForm.title"
                type="text"
                placeholder="Titulo de la tarea"
                required
            />
            <input
                v-model="editForm.description"
                type="text"
                placeholder="Descripcion"
            />

		                <input
		                    v-model="editForm.dueDate"
		                    type="date"
		                    required
		                />

            <select v-model="editForm.idGeoPoint" required>
                <option :value="null" disabled>
                    Selecciona Ubicacion Geografica / Sector
                </option>
                <option
                    v-for="gp in geoPoints"
                    :key="gp.idGeoPoint"
                    :value="gp.idGeoPoint"
                >
                    {{ gp.name }} (Sector: {{ gp.sector }})
                </option>
            </select>
            <div class="edit-actions">
                <button @click="saveEdit" class="save-btn">
                    Guardar Cambios
                </button>
                <button @click="cancelEditing" class="cancel-btn">
                    Cancelar
                </button>
            </div>
        </div>

        <div v-else class="task-view">
            <div class="task-info">
                <div class="title-row">
                    <h3>{{ task.title }}</h3>
                    <span
                        class="status-badge"
                        :class="
                            task.status === 'Completada'
                                ? 'badge-success'
                                : 'badge-warning'
                        "
                    >
                        {{ task.status }}
                    </span>
                </div>
                <p>{{ task.description }}</p>
                <small
                    >Vence el: {{ formatForDisplay(task.dueDate) }} | Sector:
                    {{ task.geoPoint?.sector || "N/A" }}</small
                >
            </div>
            <div class="task-actions">
                <button @click="viewMap" class="map-btn">🗺️ Ver Mapa</button>
                <button @click="startEditing" class="edit-btn">Editar</button>
                <button
                    v-if="task.status === 'Pendiente'"
                    @click="completeTask"
                    class="complete-btn"
                >
                    Completar
                </button>
                <button @click="confirmDelete" class="delete-btn">
                    Eliminar
                </button>
            </div>
        </div>
    </li>
</template>

<style scoped>
.task-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    background: var(--bg);
    border: 1px solid var(--border);
    border-left: 5px solid #ffc107;
    border-radius: 8px;
    transition:
        transform 0.2s,
        box-shadow 0.2s;
}
.task-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.task-item.completed-style {
    border-left: 5px solid #00e676;
    opacity: 0.85;
}

.task-view {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    gap: 16px;
}

.task-info {
    flex: 1;
}

.title-row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 6px;
}
.title-row h3 {
    margin: 0;
}

.status-badge {
    padding: 4px 8px;
    border-radius: 12px;
    font-size: 11px;
    font-weight: bold;
}
.badge-warning {
    background: #ffe082;
    color: #7f6000;
}
.badge-success {
    background: #b9f6ca;
    color: #00796b;
}

.task-info p {
    margin: 0 0 6px;
    color: var(--text);
}
.task-info small {
    color: var(--text);
    opacity: 0.7;
}

.task-actions {
    display: flex;
    gap: 8px;
    align-items: center;
    flex-shrink: 0;
    flex-wrap: wrap;
}

.map-btn,
.edit-btn,
.complete-btn,
.delete-btn {
    padding: 6px 12px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: bold;
    transition: opacity 0.2s;
}
.map-btn {
    background: #2196f3;
    color: white;
}
.edit-btn {
    background: #ffc107;
    color: #121212;
}
.complete-btn {
    background: #00e676;
    color: #121212;
}
.delete-btn {
    background: #ff5252;
    color: white;
}

.map-btn:hover,
.edit-btn:hover,
.complete-btn:hover,
.delete-btn:hover {
    opacity: 0.9;
}

.edit-form-inline {
    display: flex;
    flex-direction: column;
    gap: 10px;
    width: 100%;
}
.edit-form-inline input,
.edit-form-inline select {
    padding: 8px;
    border-radius: 6px;
    border: 1px solid var(--border);
    background: var(--code-bg);
    color: var(--text);
}
.edit-actions {
    display: flex;
    gap: 8px;
    justify-content: flex-end;
}
.save-btn {
    padding: 8px 16px;
    background: var(--accent);
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: bold;
}
.cancel-btn {
    padding: 8px 16px;
    background: transparent;
    border: 1px solid var(--border);
    color: var(--text-h);
    border-radius: 6px;
    cursor: pointer;
}
</style>
