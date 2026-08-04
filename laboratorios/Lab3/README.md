# Lab 2 - Gestor de Clanes y Raids con PostGIS + JPA

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
**Lab2** migra desde JDBC puro (Lab1) hacia **JPA + Hibernate Spatial + PostGIS**
para agregar **componentes espaciales** a la logica del juego.

### Diferencias con Lab1

| Aspecto | Lab1 | Lab2 |
|---------|------|------|
| **ORM** | JDBC puro (prohibido) | JPA + Hibernate Spatial |
| **BD** | PostgreSQL 15.3 | PostGIS 16-3.4 |
| **Models** | POJOs manuales (~960 lineas) | Entities con Lombok (~413 lineas) |
| **Repositories** | JdbcTemplate + RowMapper (~850 lineas) | JpaRepository (~268 lineas) |
| **Soporte espacial** | No existia | Geometry, Point, ST_DWithin, ST_Distance |
| **Docker** | Script manual | docker-compose con PostGIS + backend + frontend |

---

## Tecnologias

- **Base de datos**: PostGIS 16-3.4 (PostgreSQL + extensiones espaciales)
- **Backend**: Java 21 + Spring Boot 4.0.6
- **ORM**: Spring Data JPA + Hibernate Spatial + Hibernate Core
- **Lombok**: Reduce boilerplate en Entities
- **API REST**: Spring WebMVC
- **Seguridad**: JWT + BCrypt
- **Frontend**: React 19 + Vite + Axios + Leaflet

---

## Features Implementadas

### Migracion a JPA
- Models POJO a Entities con @Entity, @Table, Lombok
- Repositorios JDBC a Interfaces JpaRepository
- Services actualizados con @Transactional
- DataSeeder migrado a repositorios JPA
- SPs, Triggers, MV mantenidas en schema.sql

### Componente Espacial (PostGIS)
- Columnas Point(4326) en Clan (sede), Raid (boss), Personaje (ubicacion), Auditoria (suceso)
- Mapa 1000x1000 con imagen /mapa_juego.png y Leaflet CRS.Simple

### Tareas PostGIS
- **Clanes cercanos**: GET /api/clanes/cercanos?lat=X&lon=Y&distancia=Z (ST_DWithin)
- **Mapa de calor**: MV mv_calor_clanes + endpoint GET /api/clanes/mapa-calor
- **SP loot por proximidad**: sp_distribuir_botin_proximidad (solo entrega loot a menos de 50 uds del boss)
- **Formacion de grupos**: GET /api/personajes/healers-disponibles?tankId=X&distancia=Y (ST_DWithin)
- **Auditoria territorial**: Trigger captura coordenadas del nuevo lider + mapa Sedes de Poder

### Funcionalidades Adicionales
- Todos los personajes visibles en el mapa de raids
- Click en mapa para mover personaje (PATCH /api/personajes/{id}/mover)
- Unirse/salir de clanes con re-spawn en base de faccion
- Panel Admin con mapa clickeable para coordinar boss
- Login/registro JWT con roles (Admin/Usuario)
- Inscripcion a raids con validacion de cupos e item level
- Simulacion de batalla con distribucion de loot
- Historial de botin por jugador

---

## Guia de Despliegue

### Requisitos
- Docker 24+ con Docker Compose

### Levantar todo
```bash
docker compose up -d --build
```

### Acceso
| Servicio | URL |
|----------|-----|
| **Frontend** | http://localhost |
| **Backend API** | http://localhost:8080 |
| **PostGIS** | localhost:5435 |

### Credenciales de prueba
| Usuario | Contrasena | Rol |
|---------|-----------|-----|
| admin | 123456 | Admin |
| jugador2 | 123456 | Usuario |
| jugador3 | 123456 | Usuario |

---

## Paginas del Frontend

| Ruta | Contenido |
|------|-----------|
| / | Login / Registro |
| /personajes | Mis personajes + seleccionar activo |
| /raids | Mapa con raids y personajes + inscripcion |
| /inventario | Inventario del personaje activo |
| /historial | Historial de botin |
| /ranking | Ranking DKP + Mapa de Calor |
| /facciones | Tablas Alianza/Horda + Mapa Clanes + Sedes de Poder |
| /admin | Panel Admin (crear raids, items, simular batallas) |

---

## Estructura del Backend

```
backend/mmorpg/
  src/main/java/com/grupo3/mmorpg/
    models/           10 Entities JPA con Lombok
    repositories/     7 JpaRepository interfaces
    services/         Logica de negocio
    controllers/      REST endpoints
  src/main/resources/
    application.properties
    schema.sql        SPs, Triggers, MV, indices GIST
```

---

## Notas Tecnicas

- **SRID**: 4326 (WGS84) para compatibilidad con PostGIS, aunque el mapa usa CRS.Simple con coordenadas 0-1000
- **PostGIS**: Las extensiones se crean automaticamente via schema.sql
- **Indices GIST**: Creados en columnas espaciales de Clan, Raid, Auditoria_Liderazgo
- **Vistas Materializadas**: mv_ranking_clan (ranking de personajes) y mv_calor_clanes (mapa de calor)
- **Triggers**: 7 triggers incluyendo validacion de item level, auditoria de liderazgo (con coordenadas), y gestion de inventario
