<script setup>
import { ref, onMounted } from "vue";
import api from "../services/api.js";
import MapSelector from "./MapSelector.vue";

const props = defineProps({
    userId: { type: Number, required: true },
});

const emit = defineEmits(["geo-points-changed"]);

const geoPoints = ref([]);
const showForm = ref(false);
const editingId = ref(null);
const form = ref({ name: "", sector: "", coordinates: null });

const loadGeoPoints = async () => {
    try {
        const res = await api.get("/GeoPoints");
        geoPoints.value = res.data || [];
    } catch (e) {
        geoPoints.value = [];
    }
};

const startCreate = () => {
    editingId.value = null;
    form.value = { name: "", sector: "", coordinates: null };
    showForm.value = true;
};

const startEdit = (gp) => {
    editingId.value = gp.idGeoPoint;
    form.value = {
        name: gp.name,
        sector: gp.sector,
        coordinates:
            gp.latitude && gp.longitude
                ? { lat: gp.latitude, lng: gp.longitude }
                : null,
    };
    showForm.value = true;
};

const cancelForm = () => {
    showForm.value = false;
    editingId.value = null;
    form.value = { name: "", sector: "", coordinates: null };
};

const saveGeoPoint = async () => {
    if (!form.value.name || !form.value.coordinates) return;

    const data = {
        name: form.value.name,
        sector: form.value.sector,
        latitude: form.value.coordinates.lat,
        longitude: form.value.coordinates.lng,
    };

    try {
        if (editingId.value) {
            await api.put(
                `/GeoPoints/modifygeopoint/${editingId.value}/${props.userId}`,
                data,
            );
        } else {
            await api.post(`/GeoPoints/creategeopoint/${props.userId}`, data);
        }
        cancelForm();
        loadGeoPoints();
        emit("geo-points-changed");
    } catch (e) {
        alert("Error al guardar la ubicacion");
    }
};

const deleteGeoPoint = async (id) => {
    if (!confirm("¿Eliminar esta ubicacion?")) return;
    try {
        await api.delete(`/GeoPoints/deletegeopoint/${id}/${props.userId}`);
        loadGeoPoints();
        emit("geo-points-changed");
    } catch (e) {
        alert("Error al eliminar la ubicacion");
    }
};

const guardarCoordenadas = (coords) => {
    form.value.coordinates = coords;
};

onMounted(() => {
    loadGeoPoints();
});
</script>

<template>
    <div class="geo-manager">
        <div class="geo-header">
            <h2>Mis Ubicaciones</h2>
            <button v-if="!showForm" @click="startCreate" class="primary-btn">
                + Nueva Ubicacion
            </button>
        </div>

        <!-- Formulario crear/editar -->
        <div v-if="showForm" class="geo-form">
            <h3>{{ editingId ? "Editar Ubicacion" : "Nueva Ubicacion" }}</h3>
            <form @submit.prevent="saveGeoPoint">
                <div class="field">
                    <label>Nombre</label>
                    <input
                        v-model="form.name"
                        type="text"
                        placeholder="ej: Casa, Trabajo, Obra Norte"
                        required
                    />
                </div>
                <div class="field">
                    <label>Sector</label>
                    <input
                        v-model="form.sector"
                        type="text"
                        placeholder="ej: Construccion, Calles, Semáforos"
                    />
                </div>

                <MapSelector @ubicacion-seleccionada="guardarCoordenadas" />

                <p v-if="!form.coordinates && showForm" class="error-msg">
                    Selecciona una ubicacion en el mapa
                </p>

                <div class="form-actions">
                    <button type="submit" class="save-btn">Guardar</button>
                    <button
                        type="button"
                        @click="cancelForm"
                        class="cancel-btn"
                    >
                        Cancelar
                    </button>
                </div>
            </form>
        </div>

        <!-- Lista de GeoPoints -->
        <div v-if="!showForm" class="geo-list">
            <div v-if="geoPoints.length === 0" class="no-content">
                No tienes ubicaciones guardadas.
            </div>
            <ul v-else class="geo-items">
                <li
                    v-for="gp in geoPoints"
                    :key="gp.idGeoPoint"
                    class="geo-item"
                >
                    <div class="geo-info">
                        <strong>{{ gp.name }}</strong>
                        <span class="geo-sector">{{
                            gp.sector || "Sin sector"
                        }}</span>
                        <small
                            >{{ gp.latitude?.toFixed(4) }},
                            {{ gp.longitude?.toFixed(4) }}</small
                        >
                    </div>
                    <div class="geo-actions">
                        <button @click="startEdit(gp)" class="edit-btn">
                            Editar
                        </button>
                        <button
                            @click="deleteGeoPoint(gp.idGeoPoint)"
                            class="delete-btn"
                        >
                            Eliminar
                        </button>
                    </div>
                </li>
            </ul>
        </div>
    </div>
</template>

<style scoped>
.geo-manager {
    margin-bottom: 24px;
}

.geo-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.geo-header h2 {
    margin: 0;
}

.geo-form {
    background: var(--code-bg);
    border: 1px solid var(--border);
    padding: 24px;
    border-radius: 12px;
    margin-bottom: 24px;
}

.geo-form h3 {
    margin: 0 0 16px;
}

.field {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-bottom: 12px;
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
    outline: none;
}

.field input:focus {
    border-color: var(--accent);
}

.form-actions {
    display: flex;
    gap: 8px;
    justify-content: flex-end;
    margin-top: 16px;
}

.primary-btn {
    padding: 8px 16px;
    background: var(--accent);
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: bold;
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

.error-msg {
    color: #ef4444;
    font-size: 14px;
    text-align: center;
    margin-top: 8px;
}

.no-content {
    color: var(--text);
    font-style: italic;
    text-align: center;
    padding: 20px;
}

.geo-items {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.geo-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: var(--bg);
    border: 1px solid var(--border);
    border-radius: 8px;
}

.geo-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.geo-sector {
    font-size: 13px;
    color: var(--text);
    opacity: 0.8;
}

.geo-info small {
    font-size: 12px;
    color: var(--text);
    opacity: 0.6;
}

.geo-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
}
</style>
