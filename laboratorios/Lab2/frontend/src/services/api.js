import axios from 'axios';

// 1. Creamos la instancia con la URL base de tu backend (Spring Boot)
const api = axios.create({
    baseURL: 'http://localhost:8080', 
});

// 2. Interceptor de peticiones (Requests)
// Esto se ejecuta AUTOMÁTICAMENTE antes de que cualquier petición salga al servidor
api.interceptors.request.use(
    (config) => {
        // Buscamos el token en el LocalStorage
        const token = localStorage.getItem('token');
        
        // Si hay un token guardado, se lo inyectamos a los headers de la petición
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default api;