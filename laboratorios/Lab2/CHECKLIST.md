# 📋 CHECKLIST LAB 1 + LAB 2 — Grupo 3: Gestor de Clanes y Raids

> Estado al 20/07/2026

---

## 🟦 LAB 1 — Funcionalidades Base

### CRUD de Entidades

| # | Requisito | Estado | Endpoints |
|---|-----------|--------|-----------|
| 1 | CRUD Personajes (Clase, Nivel, Facción) | ✅ | `/api/personajes` |
| 2 | CRUD Items de Botín | ✅ | `/api/items` |
| 3 | CRUD Clanes | ✅ | `/api/clanes` |
| 4 | CRUD Raids | ✅ | `/api/raids` |
| 5 | CRUD Jugadores | ✅ | `/api/jugadores` |
| 6 | CRUD Inventario | ✅ | `/api/personajes/{id}/inventario` |
| 7 | Autenticación JWT (login/register) | ✅ | `/api/auth/login`, `/api/auth/register` |

### Procedimientos Almacenados (Lab1)

| # | Requisito | Estado |
|---|-----------|--------|
| 1 | `sp_distribuir_botin`: Distribuir ítem de botín deduciendo DKP | ✅ |
| 2 | `sp_crear_raid_e_invitar`: Crear raid e invitar Raider | ✅ |

### Triggers (Lab1)

| # | Requisito | Estado |
|---|-----------|--------|
| 1 | `trg_validar_ilvl`: Rechazar inscripción si Item Level bajo | ✅ |
| 2 | `trg_auditar_lider`: Auditar transferencia de liderazgo | ✅ |
| 3 | `trg_item_inicial`: ítem de bienvenida al crear personaje | ✅ |
| 4 | `trg_check_lider_dkp`: Ascenso automático por DKP | ✅ |
| 5 | `trg_equipo_unico`: No equipar 2 objetos | ✅ |
| 6 | `trg_actualizar_poder`: Actualizar poder al equipar | ✅ |
| 7 | `trg_recalcular_poder_por_nivel`: Recalcular poder al subir nivel | ✅ |

### Vistas Materializadas (Lab1)

| # | Requisito | Estado |
|---|-----------|--------|
| 1 | `mv_ranking_clan`: Ranking por asistencia y DKP | ✅ |

### Índices (Lab1)

| Columna | Estado |
|---------|--------|
| `Personaje(clase)` | ✅ |
| `Inscripcion_Raid(id_raid)` | ✅ |
| `Inventario(id_personaje)` | ✅ |

### API Adicional (Lab1)

| Endpoint | Estado |
|----------|--------|
| Calendario raids con cupos (`/api/raids/programadas`) | ✅ |
| Historial botín jugador (`/api/jugadores/{id}/historial-loot`) | ✅ |
| Inscribirse/desinscribirse (`/api/raids/{id}/inscribir`) | ✅ |

---

## 🟩 LAB 2 — Features Espaciales (PostGIS)

### Requisitos Técnicos

| Requisito | Estado |
|-----------|--------|
| PostgreSQL + PostGIS | ✅ `postgis/postgis:16-3.4` |
| Índice GIST para columnas espaciales | ✅ En `schema.sql` |
| ORM permitido (JPA + Hibernate Spatial) | ✅ Ya implementado |

### Componente Espacial — Entities con Point

| Entidad | Columna Point | Estado |
|---------|--------------|--------|
| `Clan` | `ubicacion` | ✅ |
| `Raid` | `ubicacionBoss` | ✅ |
| `Personaje` | `ubicacionActual` | ✅ |
| `AuditoriaLiderazgo` | `ubicacionSuceso` (trigger) | ✅ |

### DataSeeder — Coordenadas de prueba

| Elemento | Coordenadas (Y, X) |
|----------|-------------------|
| Clan Alianza | (150, 150) |
| Clan Horda | (850, 850) |
| Raid Asalto al Castillo (boss) | (800, 200) |
| Raid Cueva del Dragón (boss) | (200, 800) |
| Personaje Tato_Rey | (150, 150) — "Base Alianza" |
| Personaje Thrall | (850, 850) — "Base Horda" |

### Tareas PostGIS del Enunciado

| # | Tarea | Estado | Endpoint/Archivo |
|---|-------|--------|-----------------|
| 1 | **Clanes cercanos**: Endpoint que recibe GPS y devuelve clanes cercanos usando `ST_DWithin` | ✅ | `GET /api/clanes/cercanos?lat=X&lon=Y&distancia=Z` |
| 2 | **Mapa de calor**: Vista materializada de clanes mejor rankeados | ✅ | `mv_calor_clanes` + `GET /api/clanes/mapa-calor` |
| 3 | **SP loot por proximidad**: Distribuir loot solo a personajes dentro de 50 uds del boss | ✅ | `sp_distribuir_botin_proximidad` en `schema.sql` |
| 4 | **Formación de grupos**: Filtrar Healers en misma región que el Tanke | ✅ | Query en `PersonajeRepository` + endpoint |
| 5 | **Auditoría territorial**: Registrar coordenadas al transferir liderazgo | ✅ | Trigger `fn_auditar_liderazgo` modificado |

---

## 🔄 Frontend — Páginas

| Ruta | Página | Estado |
|------|--------|--------|
| `/` → login | Login/Registro | ✅ |
| `/personajes` | Mis Personajes + seleccionar activo | ✅ |
| `/raids` | Mapa raids + lista con inscripción | ✅ |
| `/inventario` | Inventario del personaje activo | ✅ |
| `/historial` | Historial de botín | ✅ |
| `/ranking` | Ranking DKP + Mapa de Calor | ✅ |
| `/facciones` | Tablas facciones + Mapa de Calor Clanes | ✅ |
| `/admin` | Panel Admin (solo Admin) | ✅ |

---

## ✅ PENDIENTE DE IMPLEMENTAR

### 1. Formación de Grupos (Healers + Tanke)

**Dónde**: `PersonajeRepository.java`, `PersonajeService.java`, `PersonajeController.java`
**Qué**: Endpoint `GET /api/personajes/healers-disponibles?tankId=X` que filtre Healers en la misma `region_mapa` (o a cierta distancia) que el Tanke líder. **(COMPLETADO)**

---

## 📊 Resumen

| Categoría | Hecho | Pendiente |
|-----------|-------|-----------|
| Backend Lab1 (CRUD, SPs, Triggers, MV) | 100% | 0 |
| Backend Lab2 (PostGIS) | 5/5 | 0 |
| Frontend | 100% funcional | — |

