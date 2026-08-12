# Lab 3 - Gestor de Clanes y Raids con MongoDB

###### Grupo 3 - TBD

## Integrantes
- Bruno Gutierrez
- Alejandra Silva
- Emilio Poblete
- Bastian Ramos
- Jesus Ganga

---

## Descripcion

MMORPG Manager que administra Clanes, Personajes, Raids, Inventario y Loot.
**Lab3** migra desde PostgreSQL/PostGIS (Lab2) hacia **MongoDB 6.0** desplegado
como **Replica Set** (primario + secundario), aprovechando las capacidades
avanzadas del motor documental: Schema Validation, Transacciones ACID
multi-documento, Aggregation Pipelines, Indexación y Change Streams.

### Diferencias con Lab2

| Aspecto | Lab2 | Lab3 |
|---------|------|------|
| **BD** | PostgreSQL + PostGIS | MongoDB 6.0 (Replica Set `rs0`) |
| **ORM** | JPA + Hibernate Spatial | Spring Data MongoDB |
| **Transacciones** | SQL | ACID multi-documento (`MongoTransactionManager`) |
| **Validación** | Triggers/SP en SQL | `$jsonSchema` a nivel de colección |
| **Ranking** | Vistas materializadas SQL | Aggregation Pipelines + colección materializada (`$merge`) |
| **Reactividad** | No existía | Change Streams (muerte del Boss → Loot automático) |

---

## Tecnologias

- **Base de datos**: MongoDB 6.0 en Replica Set (`rs0`: `mongo-primary` + `mongo-secondary`)
- **Backend**: Java 21 + Spring Boot 3.2.4 + Spring Data MongoDB
- **Seguridad**: JWT (jjwt) + BCrypt + RBAC (rutas por rol Admin/Usuario)
- **Frontend**: React 19 + Vite + Axios + Leaflet

---

## Las 6 tareas del enunciado (MongoDB Avanzado)

### 1. Modelado de Datos (embedding vs referencing)
Se decidió **referenciar** (colección propia `personajes`) en lugar de embeber
los personajes dentro del documento `jugadores`. Justificación: cada personaje
participa de forma independiente en múltiples raids, por lo que embeberlos haría
el documento del jugador crecer sin límite y complicaría consultar personajes de
forma aislada (por clan, clase o rol). Las inscripciones a raids también viven en
una colección propia (`inscripciones_raid`) con referencias a `raidId` y `personajeId`.
Referencias guardadas como Strings (ObjectId). Ver `Justificación del Modelado de Datos.docx`.

### 2. Validación de Esquema ($jsonSchema)
`MongoSchemaConfig` aplica validación **estricta** (`validationLevel: strict`,
`validationAction: error`) a la colección `historial_loot`:
- `required`: `raidId`, `personajeId`, `itemId`, `participoRaid`, `estadoPersonaje`
- `participoRaid` debe ser **`true`** (impide asignar loot a un personaje que no participó)
- `estadoPersonaje` solo `"Activo"` o `"Vivo"` (**rechaza personajes "Caido"**)

Como `$jsonSchema` no puede hacer `$lookup`, la regla se refuerza en el servicio
(`RaidService.entregarItem`): valida que el personaje esté inscrito en la raid y
que no esté caído antes de asignar el loot.

### 3. Transacción Multi-Documento (ACID)
- `MongoConfig` define el `MongoTransactionManager` (habilita `@Transactional` en MongoDB).
- `POST /api/raids/{id}/distribuir-loot-masivo` (`distribuirBotinMasivo`) distribuye
  un lote de ítems en **una única transacción**: descuenta DKP, actualiza inventario,
  inserta historial y marca asistencia.
- **Anti-duplicado ante concurrencia**: índice único `{raidId, itemId}` en
  `historial_loot` + manejo de `DuplicateKeyException` → un mismo ítem jamás se
  asigna a dos personajes.

### 4. Aggregation Pipeline
`GET /api/ranking/clanes-desempeno` (`RankingService.obtenerRankingClanesPorDesempeno`)
calcula el ranking de clanes por desempeño en raids con:
`$project` (conversión de IDs a ObjectId, asistencia a 0/1) → `$lookup`/`$unwind`
(personajes, clanes, raids) → `$group` por clan (suma de daño, suma de asistencia,
promedio de tiempo de finalización) → `$sort` por daño desc / tiempo asc.

### 5. Índices
| Tipo | Colección | Índice |
|------|-----------|--------|
| Compuesto | `personajes` | `{clanId, clase, rolClan}` (filtrar disponibles por clase/rol dentro del clan) |
| Único | `personajes` / `raids` / `items` / `clanes` | `nombre` |
| Único | `jugadores` | `username` |
| Único compuesto | `inscripciones_raid` | `{raidId, personajeId}` |
| Único compuesto | `historial_loot` | `{raidId, itemId}` |
| TTL | `raid_events` | `timestamp` (expira en 1 hora) |
| Texto | `items` | `clasesPermitidas` |
| Geoespacial (2dsphere) | `personajes.ubicacionActual`, `raids.ubicacionBoss`, `clanes.ubicacion` | consultas `$near` |

### 6. Change Streams (Reactividad)
- `ChangeStreamConfig` escucha la colección **`raid_events`** con filtro
  `operationType=insert` + `eventType=BOSS_DEATH`.
- `RaidBossListener` al detectar la muerte del Boss:
  1. Llama a `LootService.distributeBossLoot`: reparte **1 ítem distinto por
     inscrito** cercano al boss (radio de 5 uds, los mejores ítems al mejor
     desempeño; el índice único `{raidId, itemId}` evita repeticiones).
  2. Actualiza la colección materializada **`clanes_top_ranking`** con `$merge`
     (pipeline por DKP).
- Se dispara desde `POST /api/raids/{id}/evento-muerte-boss` (o el botón
  "💀 Muerte del Boss (ChangeStream)" del Panel Admin).

---

## Guia de Despliegue

### Requisitos
- Docker 24+ con Docker Compose

### Levantar todo

1. Abre una terminal y **asegúrate de estar posicionado en esta misma carpeta** (la carpeta raíz del `Lab3`, donde se encuentra el archivo `docker-compose.yml`).
2. Ejecuta el siguiente comando para construir y levantar todos los contenedores (Frontend, Backend y Base de Datos):

```bash
docker compose up -d --build
```

El replica set de MongoDB se inicializará solo de forma automática (gracias al contenedor `mongo-setup` con retry + healthchecks). Puede tardar unos segundos la primera vez.

> **Nota:** Para apagar el sistema y limpiar todo al terminar de revisar, ejecuta `docker compose down -v`.

### Acceso
| Servicio | URL |
|----------|-----|
| **Frontend** | http://localhost |
| **Backend API** | http://localhost:8080 |
| **MongoDB primario** | localhost:27017 (RS `rs0`) |
| **MongoDB secundario** | localhost:27018 |

### Credenciales de prueba
| Usuario | Contrasena | Rol |
|---------|-----------|-----|
| admin | 123456 | Admin |
| jugador2 | 123456 | Usuario |
| jugador3 | 123456 | Usuario |

### Endpoints principales
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/auth/login` | Login JWT |
| GET | `/api/personajes`, `/api/items`, `/api/clanes`, `/api/raids` | CRUD / listados |
| POST | `/api/raids/{id}/inscribir` | Inscripción con validación de item level y cupos |
| POST | `/api/raids/{id}/distribuir-loot-masivo` | Distribución de loot transaccional (lote) |
| POST | `/api/raids/{id}/evento-muerte-boss` | Dispara el ChangeStream (loot + ranking) |
| GET | `/api/ranking/clanes-desempeno` | Aggregation Pipeline: ranking por clan |
| GET | `/api/clanes/auditoria` | Historial de cambios de liderazgo (Sedes de Poder) |
| GET | `/api/clanes/mapa-calor` | Mapa de calor con DKP real por clan |

### Ejemplos de JSON (Documentación de la API)

**1. Login JWT (`POST /api/auth/login`)**
```json
{
  "username": "admin",
  "password": "123456"
}
```

**2. Crear un Personaje (`POST /api/personajes`)**
```json
{
  "jugadorId": "60d5ec49f1b2c3d4e5f6a7b8",
  "clanId": "60d5ec49f1b2c3d4e5f6a7b9",
  "nombre": "Gandalf",
  "clase": "Mago",
  "nivel": 60,
  "faccion": "La Alianza",
  "itemLevel": 150,
  "rolClan": "Healer",
  "estado": "Activo"
}
```

**3. Distribuir Loot Masivo (`POST /api/raids/{id}/distribuir-loot-masivo`)**
*Demuestra la transacción atómica multi-documento (recibe una lista).*
```json
[
  {
    "idPersonaje": "60d5ec49f1b2c3d4e5f6a7b8",
    "idItem": "60d5ec49f1b2c3d4e5f6a7c1",
    "costoDkp": 50
  },
  {
    "idPersonaje": "60d5ec49f1b2c3d4e5f6a7b9",
    "idItem": "60d5ec49f1b2c3d4e5f6a7c2",
    "costoDkp": 100
  }
]
```

**4. Inscribir Personaje a Raid (`POST /api/raids/{id}/inscribir?idPersonaje=...`)**
*(Este endpoint recibe el ObjectId del personaje por parámetro URL `?idPersonaje=`, no requiere Body JSON).*

*(Puedes ver la documentación extendida en `backend/mmorpg/API_ENDPOINTS.md`)*

---

## Paginas del Frontend

| Ruta | Contenido |
|------|-----------|
| / | Login / Registro |
| /personajes | Mis personajes + seleccionar activo + unirse/salir de clan |
| /raids | Mapa de raids, inscripción y rango de loot del boss |
| /inventario | Inventario del personaje activo |
| /historial | Historial de botín |
| /ranking | Ranking DKP global + Ranking por Clan (desempeño en raids) |
| /facciones | Tablas Alianza/Horda + Mapa (calor / clanes cercanos / sedes de poder) + Historial de Reyes |
| /admin | Panel Admin (crear raids/items, simulador, botón Muerte del Boss) |

---

## Estructura del Backend

```
backend/mmorpg/
  src/main/java/com/grupo3/mmorpg/
    config/           MongoConfig (transacciones), MongoSchemaConfig ($jsonSchema + índices),
                      ChangeStreamConfig (listener raid_events)
    listeners/        RaidBossListener (muerte del Boss → loot + ranking)
    models/           Documentos MongoDB (Jugador, Personaje, Clan, Raid, Item,
                      InscripcionRaid, Inventario, HistorialLoot, AuditoriaLiderazgo)
    repositories/     Interfaces MongoRepository (incluye AuditoriaLiderazgoRepository)
    services/         LootService, RaidService, RankingService, ClanService, ...
    controllers/      REST endpoints
```

---

## Notas Tecnicas

- **Proximidad del loot**: 50 uds en el mundo 0-1000 del Lab2 = 5% del mapa; al
  escalar a 0-90 en MongoDB equivale a ~4.5 uds (se usa 5). El círculo rojo del
  mapa de raids muestra el rango real.
- **Ascenso de líder por DKP**: al inyectar DKP, si un personaje supera al líder
  de su clan, asume el liderazgo automáticamente y queda auditado (Historial de Reyes).
- **Desempeño por raid**: al completarse una raid se generan `danoTotal` y
  `tiempoFinalizacionMinutos` aleatorios (20-80 min, 10k-100k de daño) para
  alimentar el ranking por clan.
