<script setup>
import { ref } from 'vue'

const props = defineProps({
  task: { type: Object, required: true },
  geoPoints: { type: Array, default: () => [] },
  userId: { type: Number, required: true }
})

const emit = defineEmits(['save-edit', 'delete-task', 'complete-task'])

// Estado de edicion
const isEditing = ref(false)
const editForm = ref({ title: '', description: '', dueDate: '', idGeoPoint: null })

const startEditing = () => {
  editForm.value = {
    title: props.task.title,
    description: props.task.description,
    dueDate: props.task.dueDate,
    idGeoPoint: props.task.geoPoint?.idGeoPoint || null
  }
  isEditing.value = true
}

const cancelEditing = () => {
  isEditing.value = false
}

const saveEdit = () => {
  if (!editForm.value.title || !editForm.value.idGeoPoint) return
  emit('save-edit', props.task.idTask, { ...editForm.value })
  isEditing.value = false
}

const confirmDelete = () => {
  if (confirm('¿Estas seguro de eliminar esta tarea?')) {
    emit('delete-task', props.task.idTask)
  }
}

const completeTask = () => {
  emit('complete-task', props.task.idTask)
}
</script>

<template>
  <li class="task-item">
    <!-- MODO EDICION -->
    <div v-if="isEditing" class="edit-form-inline">
      <input v-model="editForm.title" type="text" placeholder="Titulo de la tarea" required />
      <input v-model="editForm.description" type="text" placeholder="Descripcion" />
      <input v-model="editForm.dueDate" type="date" required />
      <select v-model="editForm.idGeoPoint" required>
        <option :value="null" disabled>Selecciona Ubicacion Geografica / Sector</option>
        <option v-for="gp in geoPoints" :key="gp.idGeoPoint" :value="gp.idGeoPoint">
          {{ gp.name }} (Sector: {{ gp.sector }})
        </option>
      </select>
      <div class="edit-actions">
        <button @click="saveEdit" class="save-btn">Guardar Cambios</button>
        <button @click="cancelEditing" class="cancel-btn">Cancelar</button>
      </div>
    </div>

    <!-- MODO VISTA -->
    <div v-else class="task-view">
      <div class="task-info">
        <h3>{{ task.title }}</h3>
        <p>{{ task.description }}</p>
        <small>Vence el: {{ task.dueDate }} | Sector: {{ task.geoPoint?.sector }}</small>
      </div>
      <div class="task-actions">
        <button @click="startEditing" class="edit-btn">Editar</button>
        <button v-if="task.status === 'Pendiente'" @click="completeTask" class="complete-btn">
          Completar
        </button>
        <button @click="confirmDelete" class="delete-btn">Eliminar</button>
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
  border-radius: 8px;
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

.task-info h3 {
  margin: 0 0 4px;
}

.task-info p {
  margin: 0 0 4px;
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
}

.edit-btn {
  padding: 6px 12px;
  background: #ffc107;
  color: #121212;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
}

.edit-btn:hover {
  opacity: 0.9;
}

.complete-btn {
  padding: 6px 12px;
  background: #00e676;
  color: #121212;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
}

.complete-btn:hover {
  opacity: 0.9;
}

.delete-btn {
  padding: 6px 12px;
  background: #ff5252;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
}

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
