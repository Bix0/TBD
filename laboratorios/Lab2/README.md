#  Lab 2 - Gestor de Clanes y Raids con PostGIS + JPA

###### Grupo 3 - TBD

## Integrantes
- Bruno Gutiérrez
- Alejandra Silva
- Emilio Poblete
- Bastian Ramos
- Jesús Ganga

---

##  Descripción

MMORPG Manager que administra Clanes, Personajes, Raids, Inventario y Loot.
**Lab2** migra desde JDBC puro (Lab1) hacia **JPA + Hibernate Spatial + PostGIS** para
agregar **componentes espaciales** a la lógica del juego.

### Diferencias con Lab1

| Aspecto | Lab1 | Lab2 |
|---------|------|------|
| **ORM** | ❌ JDBC puro (prohibido) | ✅ JPA + Hibernate Spatial |
| **BD** | PostgreSQL 15.3 | PostGIS 16-3.4 |
| **Models** | POJOs manuales (~960 líneas) | Entities con Lombok (~413 líneas) |
| **Repositories** | JdbcTemplate + RowMapper (~850 líneas) | JpaRepository (~268 líneas) |
| **Soporte espacial** | ❌ No existía | ✅ Geometry, Point, ST_DWithin |
| **Docker** | Script manual | `docker-compose` con PostGIS |

---

##  Tecnologías

- **Base de datos**: PostGIS 16-3.4 (PostgreSQL + extensiones espaciales)
- **Backend**: Java 21 + Spring Boot 4.0.6
- **ORM**: Spring Data JPA + Hibernate Spatial + Hibernate Core
- **Lombok**: Reduce boilerplate en Entities
- **API REST**: Spring WebMVC
- **Seguridad**: JWT + BCrypt
- **Frontend**: React 19 + Vite + Axios

---

##  Features del Lab2

###  Migración a JPA (Completada)

- [x] Models POJO → Entities con @Entity, @Table, Lombok
- [x] Repositorios JDBC → Interfaces JpaRepository
- [x] Services actualizados con @Transactional
- [x] DataSeeder migrado a repositorios JPA
- [x] SPs, Triggers, MV mantenidas en schema.sql

### ❌ Features Espaciales (Pendientes)

- [ ] Clanes con ubicación GPS — columna Point en Clan
- [ ] Raids con ubicación del Boss — columna Point en Raid
- [ ] Personaje con ubicación — columna Point en Personaje
- [ ] GET /api/clanes/cercanos?lat=X&lon=Y&radio=Z
- [ ] Mapa de calor de clanes — nueva MV
- [ ] SP de loot por proximidad con ST_DWithin
- [ ] Formación de grupos — Healers en región del Tanke
- [ ] Auditoría territorial — coordenadas al transferir liderazgo

---

##  Guía de Despliegue

### Requisitos
- Docker 24+ con Docker Compose
- Node.js 18+ (solo frontend)

### Levantar todo

docker compose up -d --build
