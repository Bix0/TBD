# 📋 Diccionario de Endpoints - API MMORPG

Documentación completa de todos los endpoints disponibles en la API REST.

---

## 🔗 Base URL

```
http://localhost:8080
```

---

## 👤 Jugadores

| Método | Endpoint | Descripción | Body JSON |
|--------|----------|-------------|-----------|
| POST | `/api/jugadores` | Crear un nuevo jugador | `{username, password, rol}` |
| GET | `/api/jugadores` | Listar todos los jugadores | - |
| GET | `/api/jugadores/{id}` | Obtener jugador por ID | - |
| PUT | `/api/jugadores/{id}` | Actualizar jugador | `{username, password, rol}` |
| DELETE | `/api/jugadores/{id}` | Eliminar jugador | - |
| GET | `/api/jugadores/username/{username}` | Buscar por username | - |
| GET | `/api/jugadores/exists/{username}` | Verificar si username existe | - |

### Ejemplos

```bash
# Crear jugador
curl -X POST http://localhost:8080/api/jugadores \
  -H "Content-Type: application/json" \
  -d '{"username": "emilio", "password": "123", "rol": "usuario"}'

# Listar jugadores
curl http://localhost:8080/api/jugadores

# Buscar por username
curl http://localhost:8080/api/jugadores/username/emilio
```

---

## ⚔️ Personajes

| Método | Endpoint | Descripción | Body JSON / Params |
|--------|----------|-------------|-------------------|
| POST | `/api/personajes` | Crear personaje | `{id_jugador, id_clan, nombre, clase, nivel, faccion, item_level, puntos_merito, rol_clan}` |
| GET | `/api/personajes` | Listar todos | - |
| GET | `/api/personajes/{id}` | Obtener por ID | - |
| PUT | `/api/personajes/{id}` | Actualizar personaje | - |
| DELETE | `/api/personajes/{id}` | Eliminar personaje | - |
| GET | `/api/personajes/clan/{clanId}` | Por clan | - |
| GET | `/api/personajes/clase/{clase}` | Por clase | - |
| GET | `/api/personajes/rol/{rolClan}` | Por rol de clan | - |
| GET | `/api/personajes/itemlevel/{itemLevel}` | Por item level mínimo | - |
| PUT | `/api/personajes/{id}/merito` | Actualizar DKP | `cantidad=100` |
| GET | `/api/personajes/jugador/{jugadorId}` | Por jugador | - |

### Ejemplos

```bash
# Crear personaje
curl -X POST http://localhost:8080/api/personajes \
  -H "Content-Type: application/json" \
  -d '{"id_jugador": 1, "id_clan": 1, "nombre": "Thorin", "clase": "Guerrero", "nivel": 60, "faccion": "Alianza", "item_level": 450, "puntos_merito": 2000, "rol_clan": "Raider"}'

# Personajes por clase
curl http://localhost:8080/api/personajes/clase/Guerrero

# Actualizar DKP
curl -X PUT "http://localhost:8080/api/personajes/1/merito?cantidad=100"
```

---

## 🏰 Clanes

| Método | Endpoint | Descripción | Body JSON / Params |
|--------|----------|-------------|-------------------|
| POST | `/api/clanes` | Crear clan | `{nombre, id_lider}` |
| GET | `/api/clanes` | Listar todos | - |
| GET | `/api/clanes/{id}` | Obtener por ID | - |
| PUT | `/api/clanes/{id}` | Actualizar clan | - |
| PUT | `/api/clanes/{id}/lider` | Cambiar líder | `nuevoLider=2` |
| DELETE | `/api/clanes/{id}` | Eliminar clan | - |
| GET | `/api/clanes/nombre/{nombre}` | Por nombre | - |
| GET | `/api/clanes/exists/{nombre}` | Verificar nombre | - |
| GET | `/api/clanes/{id}/lider-id` | Obtener ID líder | - |

### Ejemplos

```bash
# Crear clan
curl -X POST http://localhost:8080/api/clanes \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Dragones Rojos", "id_lider": 1}'

# Cambiar líder (activa trigger de auditoría)
curl -X PUT "http://localhost:8080/api/clanes/1/lider?nuevoLider=2"
```

---

## 🐉 Raids

| Método | Endpoint | Descripción | Body JSON / Params |
|--------|----------|-------------|-------------------|
| POST | `/api/raids` | Crear raid | `{nombre, fecha, estado, item_level_requerido, cupos_tanque, cupos_healer, cupos_dps}` |
| POST | `/api/raids/con-inscripcion-masiva` | Crear con SP | `nombre, fecha, itemLevel, tanques, heals, dps` |
| GET | `/api/raids` | Listar todas | - |
| GET | `/api/raids/{id}` | Obtener por ID | - |
| PUT | `/api/raids/{id}` | Actualizar raid | - |
| PUT | `/api/raids/{id}/estado` | Cambiar estado | `estado=Programada` |
| DELETE | `/api/raids/{id}` | Eliminar raid | - |
| GET | `/api/raids/estado/{estado}` | Por estado | - |
| GET | `/api/raids/programadas` | Raids programadas | - |
| POST | `/api/raids/{id}/inscribir` | Inscribir personaje | `idPersonaje=1` |
| POST | `/api/raids/{id}/desinscribir` | Desinscribir | `idPersonaje=1` |
| GET | `/api/raids/{id}/inscripciones` | Ver inscripciones | - |
| GET | `/api/raids/{id}/inscripciones-conteo` | Conteo por estado | - |

### Ejemplos

```bash
# Crear raid
curl -X POST http://localhost:8080/api/raids \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Raid Prueba", "fecha": "2026-05-01T20:00:00", "estado": "Programada", "item_level_requerido": 400, "cupos_tanque": 2, "cupos_healer": 2, "cupos_dps": 6}'

# Inscribir personaje (activa trigger de validación)
curl -X POST "http://localhost:8080/api/raids/1/inscribir?idPersonaje=1"
```

---

## 🗡️ Items

| Método | Endpoint | Descripción | Body JSON / Params |
|--------|----------|-------------|-------------------|
| POST | `/api/items` | Crear item | `{nombre, item_lvl, ganancia_dkp}` |
| GET | `/api/items` | Listar todos | - |
| GET | `/api/items/{id}` | Obtener por ID | - |
| PUT | `/api/items/{id}` | Actualizar item | - |
| DELETE | `/api/items/{id}` | Eliminar item | - |
| GET | `/api/items/nombre/{nombre}` | Por nombre | - |
| GET | `/api/items/itemlevel/{itemLevel}` | Por item level min | - |
| POST | `/api/items/{id}/clases-permitidas` | Agregar clase | `clase=Guerrero` |
| DELETE | `/api/items/{id}/clases-permitidas/{clase}` | Eliminar clase | - |
| GET | `/api/items/{id}/clases-permitidas` | Obtener clases | - |
| GET | `/api/items/{id}/clases-permitidas/verificar/{clase}` | Verificar clase | - |
| GET | `/api/items/{id}/clases-permitidas/completo` | Clases completas | - |

### Ejemplos

```bash
# Crear item
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Espada del Rey", "item_lvl": 450, "ganancia_dkp": 100}'

# Agregar clase permitida
curl -X POST "http://localhost:8080/api/items/1/clases-permitidas?clase=Guerrero"
```

---

## 🎒 Inventario

| Método | Endpoint | Descripción | Body JSON / Params |
|--------|----------|-------------|-------------------|
| POST | `/api/personajes/{id}/inventario` | Agregar item | `{id_item, cantidad, equipado}` |
| GET | `/api/personajes/{id}/inventario` | Ver inventario | - |
| GET | `/api/personajes/{id}/inventario/{inventarioId}` | Obtener registro | - |
| PUT | `/api/personajes/{id}/inventario/{inventarioId}` | Actualizar | - |
| DELETE | `/api/personajes/{id}/inventario/{inventarioId}` | Eliminar | - |
| GET | `/api/personajes/{id}/inventario/equipados` | Items equipados | - |
| GET | `/api/personajes/{id}/inventario/item/{itemId}` | Item específico | - |
| PUT | `/api/personajes/{id}/inventario/{inventarioId}/equipar` | Equipar | - |
| PUT | `/api/personajes/{id}/inventario/{inventarioId}/desequipar` | Desequipar | - |
| PUT | `/api/personajes/{id}/inventario/{inventarioId}/cantidad` | Aumentar cantidad | `cantidad=5` |
| GET | `/api/personajes/{id}/inventario/verificar/{itemId}` | Verificar item | - |
| GET | `/api/personajes/{id}/inventario/cantidad` | Contar items | - |

### Ejemplos

```bash
# Agregar item al inventario
curl -X POST http://localhost:8080/api/personajes/1/inventario \
  -H "Content-Type: application/json" \
  -d '{"id_item": 1, "cantidad": 1, "equipado": true}'

# Equipar item
curl -X PUT http://localhost:8080/api/personajes/1/inventario/1/equipar
```

---

## 🏆 Ranking

| Método | Endpoint | Descripción | Params |
|--------|----------|-------------|--------|
| GET | `/api/ranking` | Obtener ranking | - |
| POST | `/api/ranking/refresh` | Actualizar ranking | - |
| GET | `/api/ranking/limite/{limite}` | Con límite | - |
| GET | `/api/ranking/top/{top}` | Top N | - |
| GET | `/api/ranking/clase/{clase}` | Por clase | - |
| GET | `/api/ranking/dkp/{dkpMinimo}` | Por DKP mínimo | - |
| GET | `/api/ranking/estadisticas` | Estadísticas | - |

### Ejemplos

```bash
# Obtener ranking
curl http://localhost:8080/api/ranking

# Actualizar ranking
curl -X POST http://localhost:8080/api/ranking/refresh

# Ranking por clase
curl http://localhost:8080/api/ranking/clase/Guerrero
```

---

## 📊 Resumen por Módulo

| Módulo | Endpoints | Métodos |
|--------|-----------|---------|
| Jugadores | 7 | POST, GET, PUT, DELETE |
| Personajes | 11 | POST, GET, PUT, DELETE |
| Clanes | 9 | POST, GET, PUT, DELETE |
| Raids | 13 | POST, GET, PUT, DELETE |
| Items | 11 | POST, GET, PUT, DELETE |
| Inventario | 11 | POST, GET, PUT, DELETE |
| Ranking | 7 | GET, POST |
| **Total** | **69** | **4** |

---

## 🔐 Headers

Todos los endpoints requieren:

```
Content-Type: application/json
```

---

## 📝 Respuestas

### Éxito (200/201)
```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "estado": "activo"
}
```

### Error (400)
```json
{
  "error": "El username ya existe"
}
```

### No encontrado (404)
```json
{
  "error": "Recurso no encontrado"
}