# 📚 Guía de Pruebas para la API MMORPG

Esta guía te ayudará a probar la API REST del backend MMORPG utilizando `curl` y herramientas como Postman.

---

## 🚀 Configuración Inicial

### 1. Asegurar que los servicios estén corriendo

```bash
# En una terminal, iniciar la base de datos PostgreSQL
cd backend/mmorpg
docker-compose up -d

# En otra terminal, iniciar el Spring Boot
cd backend/mmorpg
./mvnw spring-boot:run
```

### 2. Variables de entorno para los comandos

```bash
BASE_URL="http://localhost:8080/api"
```

---

## 📝 Pruebas de Jugadores

### Crear un jugador
```bash
curl -X POST http://localhost:8080/api/jugadores \
  -H "Content-Type: application/json" \
  -d '{
    "username": "emilio_gamer",
    "password": "password123",
    "rol": "usuario"
  }'
```

### Listar todos los jugadores
```bash
curl http://localhost:8080/api/jugadores
```

### Buscar jugador por ID
```bash
curl http://localhost:8080/api/jugadores/1
```

### Buscar jugador por username
```bash
curl http://localhost:8080/api/jugadores/username/emilio_gamer
```

### Verificar si un username existe
```bash
curl http://localhost:8080/api/jugadores/exists/emilio_gamer
```

### Actualizar jugador
```bash
curl -X PUT http://localhost:8080/api/jugadores/1 \
  -H "Content-Type: application/json" \
  -d '{
    "id_jugador": 1,
    "username": "emilio_gamer",
    "password": "newpassword",
    "rol": "admin"
  }'
```

### Eliminar jugador
```bash
curl -X DELETE http://localhost:8080/api/jugadores/1
```

---

## 📝 Pruebas de Personajes

### Crear un personaje
```bash
curl -X POST http://localhost:8080/api/personajes \
  -H "Content-Type: application/json" \
  -d '{
    "id_jugador": 1,
    "id_clan": 1,
    "nombre": "Thorin",
    "clase": "Guerrero",
    "nivel": 50,
    "faccion": "Alianza",
    "item_level": 400,
    "puntos_merito": 1000,
    "rol_clan": "Raider"
  }'
```

### Listar todos los personajes
```bash
curl http://localhost:8080/api/personajes
```

### Personajes por clan
```bash
curl http://localhost:8080/api/personajes/clan/1
```

### Personajes por clase
```bash
curl http://localhost:8080/api/personajes/clase/Guerrero
```

### Personajes por rol de clan
```bash
curl http://localhost:8080/api/personajes/rol/Raider
```

### Personajes con item_level mínimo
```bash
curl http://localhost:8080/api/personajes/itemlevel/350
```

### Actualizar puntos de mérito (DKP)
```bash
curl -X PUT "http://localhost:8080/api/personajes/1/merito?cantidad=100"
```

### Obtener personaje por jugador
```bash
curl http://localhost:8080/api/personajes/jugador/1
```

---

## 📝 Pruebas de Clanes

### Crear un clan
```bash
curl -X POST http://localhost:8080/api/clanes \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Los Defensores",
    "id_lider": 1
  }'
```

### Listar todos los clanes
```bash
curl http://localhost:8080/api/clanes
```

### Clan por ID
```bash
curl http://localhost:8080/api/clanes/1
```

### Clan por nombre
```bash
curl http://localhost:8080/api/clanes/nombre/Los%20Defensores
```

### Cambiar líder del clan (activa trigger de auditoría)
```bash
curl -X PUT "http://localhost:8080/api/clanes/1/lider?nuevoLider=2"
```

### Verificar si nombre de clan existe
```bash
curl http://localhost:8080/api/clanes/exists/Los%20Defensores
```

### Obtener ID del líder
```bash
curl http://localhost:8080/api/clanes/1/lider-id
```

---

## 📝 Pruebas de Raids

### Crear una raid
```bash
curl -X POST http://localhost:8080/api/raids \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Raid de Prueba",
    "fecha": "2026-05-01T20:00:00",
    "estado": "Programada",
    "item_level_requerido": 380,
    "cupos_tanque": 2,
    "cupos_healer": 2,
    "cupos_dps": 6
  }'
```

### Crear raid con inscripción masiva (usa SP)
```bash
curl -X POST "http://localhost:8080/api/raids/con-inscripcion-masiva?nombre=Prueba%20Raid&fecha=2026-05-01T20:00:00&itemLevel=380&tanques=2&heals=2&dps=6"
```

### Listar todas las raids
```bash
curl http://localhost:8080/api/raids
```

### Raids por estado
```bash
curl http://localhost:8080/api/raids/estado/Programada
```

### Raids programadas
```bash
curl http://localhost:8080/api/raids/programadas
```

### Inscribir personaje a raid (activa trigger de validación)
```bash
curl -X POST "http://localhost:8080/api/raids/1/inscribir?idPersonaje=1"
```

### Desinscribir personaje de raid
```bash
curl -X POST "http://localhost:8080/api/raids/1/desinscribir?idPersonaje=1"
```

### Ver inscripciones de una raid
```bash
curl http://localhost:8080/api/raids/1/inscripciones
```

### Conteo de inscripciones por estado
```bash
curl http://localhost:8080/api/raids/1/inscripciones-conteo
```

### Cambiar estado de raid
```bash
curl -X PUT "http://localhost:8080/api/raids/1/estado?estado=En%20curso"
```

---

## 📝 Pruebas de Items

### Crear un item
```bash
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Espada del Dragón",
    "item_lvl": 400,
    "ganancia_dkp": 50
  }'
```

### Listar todos los items
```bash
curl http://localhost:8080/api/items
```

### Items por item_lvl mínimo
```bash
curl http://localhost:8080/api/items/itemlevel/350
```

### Agregar clase permitida para item
```bash
curl -X POST "http://localhost:8080/api/items/1/clases-permitidas?clase=Guerrero"
```

### Verificar si clase está permitida
```bash
curl http://localhost:8080/api/items/1/clases-permitidas/verificar/Guerrero
```

### Obtener clases permitidas
```bash
curl http://localhost:8080/api/items/1/clases-permitidas
```

### Obtener clases permitidas completas
```bash
curl http://localhost:8080/api/items/1/clases-permitidas/completo
```

### Eliminar clase permitida
```bash
curl -X DELETE http://localhost:8080/api/items/1/clases-permitidas/Guerrero
```

---

## 📝 Pruebas de Inventario

### Agregar item al inventario
```bash
curl -X POST http://localhost:8080/api/personajes/1/inventario \
  -H "Content-Type: application/json" \
  -d '{
    "id_item": 1,
    "cantidad": 1,
    "equipado": false
  }'
```

### Ver inventario de personaje
```bash
curl http://localhost:8080/api/personajes/1/inventario
```

### Ver items equipados
```bash
curl http://localhost:8080/api/personajes/1/inventario/equipados
```

### Equipar item
```bash
curl -X PUT http://localhost:8080/api/personajes/1/inventario/1/equipar
```

### Desequipar item
```bash
curl -X PUT http://localhost:8080/api/personajes/1/inventario/1/desequipar
```

### Verificar si personaje tiene item
```bash
curl http://localhost:8080/api/personajes/1/inventario/verificar/1
```

### Contar items en inventario
```bash
curl http://localhost:8080/api/personajes/1/inventario/cantidad
```

---

## 📝 Pruebas de Ranking

### Obtener ranking actual
```bash
curl http://localhost:8080/api/ranking
```

### Actualizar ranking (REFRESH MATERIALIZED VIEW)
```bash
curl -X POST http://localhost:8080/api/ranking/refresh
```

### Ranking con límite
```bash
curl http://localhost:8080/api/ranking/limite/10
```

### Top N personajes
```bash
curl http://localhost:8080/api/ranking/top/5
```

### Ranking por clase
```bash
curl http://localhost:8080/api/ranking/clase/Guerrero
```

### Ranking por DKP mínimo
```bash
curl http://localhost:8080/api/ranking/dkp/500
```

### Estadísticas del ranking
```bash
curl http://localhost:8080/api/ranking/estadisticas
```

---

## 🔍 Verificación en la Base de Datos

### Conectar a PostgreSQL
```bash
docker exec -it postgres-mmorpg psql -U postgres -d dbd_guild
```

### Ver todas las tablas
```sql
\dt
```

### Ver datos de jugadores
```sql
SELECT * FROM Jugador;
```

### Ver datos de personajes
```sql
SELECT * FROM Personaje;
```

### Ver datos de clanes
```sql
SELECT * FROM Clan;
```

### Ver inscripciones de raids
```sql
SELECT * FROM Inscripcion_Raid;
```

### Ver inventario
```sql
SELECT * FROM Inventario;
```

### Ver historial de loot
```sql
SELECT * FROM Historial_Loot;
```

### Ver auditoría de liderazgo
```sql
SELECT * FROM Auditoria_Liderazgo;
```

### Ver clases permitidas para items
```sql
SELECT * FROM Item_Clase_Permitida;
```

### Ver ranking (vista materializada)
```sql
SELECT * FROM mv_ranking_clan;
```

---

## 🧪 Flujo de Prueba Completo

### 1. Crear un jugador
```bash
curl -X POST http://localhost:8080/api/jugadores \
  -H "Content-Type: application/json" \
  -d '{"username": "test_user", "password": "test123", "rol": "usuario"}'
```

### 2. Crear un clan
```bash
curl -X POST http://localhost:8080/api/clanes \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Test Clan", "id_lider": 1}'
```

### 3. Crear un personaje
```bash
curl -X POST http://localhost:8080/api/personajes \
  -H "Content-Type: application/json" \
  -d '{"id_jugador": 1, "id_clan": 1, "nombre": "TestHero", "clase": "Guerrero", "nivel": 50, "faccion": "Alianza", "item_level": 400, "puntos_merito": 1000, "rol_clan": "Raider"}'
```

### 4. Crear un item
```bash
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Test Sword", "item_lvl": 380, "ganancia_dkp": 50}'
```

### 5. Agregar clase permitida
```bash
curl -X POST "http://localhost:8080/api/items/1/clases-permitidas?clase=Guerrero"
```

### 6. Crear raid
```bash
curl -X POST http://localhost:8080/api/raids \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Test Raid", "fecha": "2026-05-01T20:00:00", "estado": "Programada", "item_level_requerido": 380, "cupos_tanque": 2, "cupos_healer": 2, "cupos_dps": 6}'
```

### 7. Inscribir personaje a raid
```bash
curl -X POST "http://localhost:8080/api/raids/1/inscribir?idPersonaje=1"
```

### 8. Agregar item al inventario
```bash
curl -X POST http://localhost:8080/api/personajes/1/inventario \
  -H "Content-Type: application/json" \
  -d '{"id_item": 1, "cantidad": 1, "equipado": false}'
```

### 9. Ver ranking
```bash
curl http://localhost:8080/api/ranking
```

### 10. Actualizar ranking
```bash
curl -X POST http://localhost:8080/api/ranking/refresh
```

---

## 🛠️ Herramientas Recomendadas

### Postman
1. Importar colección de endpoints
2. Configurar base URL: `http://localhost:8080`
3. Usar Collection Runner para pruebas automatizadas

### Insomnia
Similar a Postman, interfaz más limpia

### Thunder Client (VS Code)
Extensión de VS Code para pruebas de API

### curl en PowerShell
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/jugadores" -Method Get
```

---

## 📋 Checklist de Pruebas

- [ ] Crear jugador
- [ ] Crear clan
- [ ] Crear personaje
- [ ] Crear item
- [ ] Agregar clase permitida
- [ ] Crear raid
- [ ] Inscribir personaje a raid
- [ ] Agregar item al inventario
- [ ] Equipar item
- [ ] Ver ranking
- [ ] Actualizar ranking
- [ ] Cambiar líder de clan (verificar trigger)
- [ ] Validar item_level al inscribirse (verificar trigger)