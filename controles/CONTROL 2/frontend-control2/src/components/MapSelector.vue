<template>
    <div class="map-container">
        <label>Selecciona tu ubicación en el mapa:</label>

        <div class="search-container">
            <input
                v-model="searchQuery"
                type="text"
                placeholder="Busca una calle, ciudad o lugar..."
                @input="onSearchInput"
                @keyup.enter="onSearchEnter"
                @blur="closeSuggestions"
                autocomplete="off"
            />
            <ul v-if="suggestions.length > 0" class="suggestions-list">
                <li
                    v-for="item in suggestions"
                    :key="item.place_id"
                    @mousedown="selectSuggestion(item)"
                >
                    <span class="suggestion-icon"></span>
                    <span class="suggestion-text">{{ item.display_name }}</span>
                </li>
            </ul>
        </div>

        <div
            id="map"
            style="height: 400px; width: 100%; border-radius: 8px"
        ></div>

        <div v-if="coordenadas" class="coordenadas-box">
            Ubicación seleccionada: {{ coordenadas.lat.toFixed(4) }},
            {{ coordenadas.lng.toFixed(4) }}
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

//fix del incono de marcador de leaflet transparente
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl:
        "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
    iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
    shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
});

// Definimos los "emits" para enviar la coordenada al componente padre (ej: RegisterView)
const emit = defineEmits(["ubicacion-seleccionada"]);
const coordenadas = ref(null);
const map = ref(null);
const marker = ref(null);

const searchQuery = ref("");
const suggestions = ref([]);
let debounceTimeout = null;

const onSearchInput = () => {
    clearTimeout(debounceTimeout);
    if (!searchQuery.value.trim() || searchQuery.value.length < 3) {
        suggestions.value = [];
        return;
    }

    debounceTimeout = setTimeout(async () => {
        try {
            const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(searchQuery.value)}&limit=5`;
            const response = await fetch(url, {
                headers: {
                    "Accept-Language": "es",
                },
            });
            const data = await response.json();
            suggestions.value = data || [];
        } catch (e) {
            console.error("Error al buscar sugerencias:", e);
        }
    }, 400);
};

const selectSuggestion = (item) => {
    const lat = parseFloat(item.lat);
    const lng = parseFloat(item.lon);
    const latLng = [lat, lng];

    // Centrar el mapa en la sugerencia elegida con zoom cercano
    map.value.setView(latLng, 16);

    // Actualizar/Crear marcador
    if (marker.value) {
        marker.value.setLatLng(latLng);
    } else {
        marker.value = L.marker(latLng).addTo(map.value);
    }

    // Guardar y emitir coordenadas
    coordenadas.value = { lat, lng };
    emit("ubicacion-seleccionada", coordenadas.value);

    // Establecer el valor seleccionado en el buscador y limpiar sugerencias
    searchQuery.value = item.display_name;
    suggestions.value = [];
};

const closeSuggestions = () => {
    // Retrasar el cierre para permitir hacer clic en una sugerencia
    setTimeout(() => {
        suggestions.value = [];
    }, 200);
};

const onSearchEnter = async () => {
    clearTimeout(debounceTimeout);
    suggestions.value = []; // Limpiamos sugerencias al presionar Enter

    if (!searchQuery.value.trim()) return;

    try {
        const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(searchQuery.value)}&limit=1`;
        const response = await fetch(url, {
            headers: {
                "Accept-Language": "es",
            },
        });
        const data = await response.json();

        if (data && data.length > 0) {
            const result = data[0];
            const lat = parseFloat(result.lat);
            const lng = parseFloat(result.lon);
            const latLng = [lat, lng];

            // Centrar el mapa en la ubicación encontrada con zoom cercano (16)
            map.value.setView(latLng, 16);

            // Actualizar/Crear marcador
            if (marker.value) {
                marker.value.setLatLng(latLng);
            } else {
                marker.value = L.marker(latLng).addTo(map.value);
            }

            // Guardar y emitir coordenadas
            coordenadas.value = { lat, lng };
            emit("ubicacion-seleccionada", coordenadas.value);

            // Rellenar buscador con el nombre formal devuelto
            searchQuery.value = result.display_name;
        }
    } catch (e) {
        console.error("Error al buscar ubicación con Enter:", e);
    }
};

onMounted(() => {
    // Inicializamos el mapa centrado en Santiago de Chile por defecto
    const defaultCoords = [-33.4489, -70.6693];
    map.value = L.map("map").setView(defaultCoords, 13);

    // Agregamos la capa de OpenStreetMap
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: "&copy; OpenStreetMap contributors",
    }).addTo(map.value);

    // Intentamos obtener la ubicación real del usuario mediante geolocalización
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords;
                const userLatLng = [latitude, longitude];

                // Centrar el mapa en la ubicación del usuario con un zoom más cercano (15)
                map.value.setView(userLatLng, 15);

                // Si ya hay un marcador, lo actualizamos; si no, lo creamos
                if (marker.value) {
                    marker.value.setLatLng(userLatLng);
                } else {
                    marker.value = L.marker(userLatLng).addTo(map.value);
                }

                // Guardar y emitir las coordenadas al componente padre automáticamente
                coordenadas.value = { lat: latitude, lng: longitude };
                emit("ubicacion-seleccionada", coordenadas.value);
            },
            (err) => {
                console.warn(
                    "Geolocalización denegada o no disponible:",
                    err.message,
                );
            },
        );
    }

    // Escuchamos el evento de clic en el mapa para obtener las coordenadas
    map.value.on("click", function (e) {
        const { lat, lng } = e.latlng;
        coordenadas.value = { lat, lng };

        // Si ya hay un marcador, lo actualizamos; si no, lo creamos
        if (marker.value) {
            marker.value.setLatLng(e.latlng);
        } else {
            marker.value = L.marker(e.latlng).addTo(map.value);
        }

        // Emitimos las coordenadas al componente padre
        emit("ubicacion-seleccionada", coordenadas.value);
    });
});
</script>

<style scoped>
.map-container {
    margin-bottom: 20px;
}
.search-container {
    position: relative;
    margin-bottom: 12px;
    margin-top: 5px;
    width: 100%;
}
.search-container input {
    width: 100%;
    padding: 12px 14px;
    border-radius: 6px;
    border: 1px solid #333;
    background-color: #1a1a1a;
    color: #fff;
    font-size: 0.95rem;
    box-sizing: border-box;
    transition:
        border-color 0.2s,
        box-shadow 0.2s;
}
.search-container input:focus {
    border-color: #00e676;
    box-shadow: 0 0 0 2px rgba(0, 230, 118, 0.2);
    outline: none;
}
.suggestions-list {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background-color: #1f1f1f;
    border: 1px solid #333;
    border-radius: 6px;
    margin: 4px 0 0 0;
    padding: 0;
    list-style: none;
    max-height: 250px;
    overflow-y: auto;
    z-index: 1000;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
}
.suggestions-list li {
    display: flex;
    align-items: center;
    padding: 10px 14px;
    cursor: pointer;
    border-bottom: 1px solid #2a2a2a;
    transition: background-color 0.2s;
    text-align: left;
}
.suggestions-list li:last-child {
    border-bottom: none;
}
.suggestions-list li:hover {
    background-color: #2c2c2c;
}
.suggestion-icon {
    margin-right: 10px;
    font-size: 1.1rem;
}
.suggestion-text {
    color: #e0e0e0;
    font-size: 0.88rem;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.coordenadas-box {
    margin-top: 10px;
    padding: 10px;
    background-color: #242424;
    color: #00e676;
    border-radius: 4px;
    font-family: monospace;
}
</style>
