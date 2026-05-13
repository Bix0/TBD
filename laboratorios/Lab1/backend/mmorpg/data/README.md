# 📦 Script de Inicialización de Datos - MMORPG API

Scripts para inicializar datos de prueba en la API REST del backend MMORPG.

---

## 🚀 Uso Rápido

### En Linux/Mac (bash)
```bash
cd backend/mmorpg/data
bash init-data.sh
```

### En Windows (PowerShell)
```powershell
cd backend\mmorpg\data
powershell -ExecutionPolicy Bypass -File init-data.ps1
```

---

## 📋 Datos que se Crean

| Tipo | Cantidad | Ejemplos |
|------|----------|----------|
| Jugadores | 2 | admin, raider1 |
| Clanes | 1 | Dragones Rojos |
| Personajes | 2 | Thorin (Guerrero), Legolas (Ranger) |
| Items | 3 | Espada del Rey, Poción de Vida, Escudo del Guardian |
| Raids | 1 | Raid de Prueba |
| Inscripciones | 1 | Thorin inscrito a la raid |
| Inventario | 1 | Espada del Rey equipada por Thorin |

---

## 📝 Flujo de Inicialización

1. **Crear jugador admin** - Usuario con rol de administrador
2. **Crear jugador raider** - Usuario normal para pruebas
3. **Crear clan** - "Dragones Rojos" con el admin como líder
4. **Crear personaje líder** - Thorin, Guerrero nivel 60
5. **Crear personaje raider** - Legolas, Ranger nivel 58
6. **Crear items** - 3 items con diferentes niveles y DKP
7. **Agregar clases permitidas** - Restringir qué clases pueden usar cada item
8. **Crear raid** - Raid programada con requisitos de item level
9. **Inscribir personaje** - Thorin se inscribe a la raid
10. **Agregar item al inventario** - Thorin equipa la Espada del Rey

---

## ✅ Verificación

Después de ejecutar el script, verifica los datos:

```bash
# Ver jugadores
curl http://localhost:8080/api/jugadores

# Ver personajes
curl http://localhost:8080/api/personajes

# Ver clanes
curl http://localhost:8080/api/clanes

# Ver items
curl http://localhost:8080/api/items

# Ver raids
curl http://localhost:8080/api/raids

# Ver ranking
curl http://localhost:8080/api/ranking
```

---

## 🔧 Requisitos

- Spring Boot corriendo en `http://localhost:8080`
- PostgreSQL corriendo (docker-compose up -d)
- curl instalado (para bash) o PowerShell (para Windows)

---

## 🐛 Solución de Problemas

### Error: "No se pudo conectar al servidor"
```bash
# Asegúrate de que Spring Boot esté corriendo
cd backend/mmorpg
./mvnw spring-boot:run
```

### Error: "404 Not Found"
```bash
# Verifica que los endpoints existan
curl http://localhost:8080/api/jugadores
```

### Error: "400 Bad Request"
```bash
# Verifica el formato JSON en el script
# Los campos deben coincidir exactamente con el modelo
```

---

## 📚 Documentación Adicional

- [API_ENDPOINTS.md](../API_ENDPOINTS.md) - Diccionario completo de endpoints
- [GUIA_PRUEBAS_API.md](../GUIA_PRUEBAS_API.md) - Guía completa de pruebas