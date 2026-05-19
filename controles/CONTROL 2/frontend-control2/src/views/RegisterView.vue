<template>
  <div class="registro">
    <h1>Crear Usuario</h1>
    <MapSelector @ubicacion-seleccionada="guardarUbicacion" />
    
    <button @click="enviarRegistro">Registrarse</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import MapSelector from '../components/MapSelector.vue'

const ubicacionUsuario = ref(null)

const guardarUbicacion = (coords) => {
  ubicacionUsuario.value = coords
  console.log("Coordenadas listas para enviar a Spring Boot:", coords)
}

const enviarRegistro = () => {
  // Aquí usas Axios para mandar el JSON a tu backend
}
</script>