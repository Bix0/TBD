<template>
  <div class="map-container">
    <label>Selecciona tu ubicación en el mapa:</label>
    <div id="map" style="height: 400px; width: 100%; border-radius: 8px;"></div>
    
    <div v-if="coordenadas" class="coordenadas-box">
      Ubicación seleccionada: {{ coordenadas.lat.toFixed(4) }}, {{ coordenadas.lng.toFixed(4) }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Definimos los "emits" para enviar la coordenada al componente padre (ej: RegisterView)
const emit = defineEmits(['ubicacion-seleccionada'])
const coordenadas = ref(null)
const map = ref(null)
const marker = ref(null)

onMounted(() => {
  // Inicializamos el mapa centrado en una ubicación predeterminada (ej: Ciudad de México)
  map.value = L.map('map').setView([19.4326, -99.1332], 13)

  // Agregamos la capa de OpenStreetMap
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map.value)

  // Escuchamos el evento de clic en el mapa para obtener las coordenadas
  map.value.on('click', function(e) {
    const { lat, lng } = e.latlng
    coordenadas.value = { lat, lng }

    // Si ya hay un marcador, lo actualizamos; si no, lo creamos
    if (marker.value) {
      marker.value.setLatLng(e.latlng)
    } else {
      marker.value = L.marker(e.latlng).addTo(map.value)
    }

    // Emitimos las coordenadas al componente padre
    emit('ubicacion-seleccionada', coordenadas.value)
  })
})

</script>

<style scoped>
.map-container {
  margin-bottom: 20px;
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