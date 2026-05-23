<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { authService } from "../services/authService.js";
import MapSelector from "../components/MapSelector.vue";

const router = useRouter();

const userName = ref("");
const password = ref("");
const confirmPassword = ref("");
const locationName = ref("");
const ubicacionUsuario = ref(null);
const error = ref("");
const loading = ref(false);

const guardarUbicacion = (coords) => {
    ubicacionUsuario.value = coords;
};

const handleRegister = async () => {
    error.value = "";

    if (
        !userName.value ||
        !password.value ||
        !confirmPassword.value ||
        !ubicacionUsuario.value
    ) {
        error.value =
            "Completa todos los campos y selecciona una ubicacion en el mapa";
        return;
    }

    if (password.value !== confirmPassword.value) {
        error.value = "Las contrasenias no coinciden";
        return;
    }

    if (password.value.length < 4) {
        error.value = "La contrasenia debe tener al menos 4 caracteres";
        return;
    }

    loading.value = true;
    try {
        const response = await authService.register(
            userName.value,
            password.value,
            {
                latitude: ubicacionUsuario.value.lat,
                longitude: ubicacionUsuario.value.lng,
                name: locationName.value || "Sin nombre",
            },
        );
        localStorage.setItem("token", response.token);
        localStorage.setItem("userName", response.userName);
        localStorage.setItem("userId", response.idUser);
        router.push("/dashboard");
    } catch (e) {
        if (e.response?.data?.message) {
            error.value = e.response.data.message;
        } else if (e.response?.status === 400) {
            error.value = "El nombre de usuario ya existe";
        } else {
            error.value = "Error al registrarse. Intenta de nuevo";
        }
    } finally {
        loading.value = false;
    }
};
</script>

<template>
    <div class="auth-container">
        <div class="auth-card">
            <h1>Crear Cuenta</h1>
            <p class="subtitle">Registrate para gestionar tus tareas</p>

            <form @submit.prevent="handleRegister" class="auth-form">
                <div class="field">
                    <label for="userName">Usuario</label>
                    <input
                        id="userName"
                        v-model="userName"
                        type="text"
                        placeholder="nombre de usuario"
                        autocomplete="username"
                    />
                </div>

                <div class="field">
                    <label for="password">Contrasenia</label>
                    <input
                        id="password"
                        v-model="password"
                        type="password"
                        placeholder="minimo 4 caracteres"
                        autocomplete="new-password"
                    />
                </div>

                <div class="field">
                    <label for="confirmPassword">Confirmar Contrasenia</label>
                    <input
                        id="confirmPassword"
                        v-model="confirmPassword"
                        type="password"
                        placeholder="repite la contrasenia"
                        autocomplete="new-password"
                    />
                </div>

                <div class="field">
                    <label for="locationName">Nombre de ubicacion</label>
                    <input
                        id="locationName"
                        v-model="locationName"
                        type="text"
                        placeholder="ej: Casa, Trabajo, etc."
                    />
                </div>

                <MapSelector @ubicacion-seleccionada="guardarUbicacion" />

                <p v-if="error" class="error-msg">{{ error }}</p>

                <button type="submit" class="auth-btn" :disabled="loading">
                    {{ loading ? "Registrando..." : "Crear Cuenta" }}
                </button>
            </form>

            <p class="switch-link">
                ¿Ya tienes cuenta?
                <router-link to="/login">Inicia sesion</router-link>
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
    max-width: 480px;
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
