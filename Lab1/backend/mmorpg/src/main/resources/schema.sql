-- ==============================================================================
-- 1. LIMPIEZA DE ESQUEMA (Permite reiniciar la app sin errores de duplicidad)
-- ==============================================================================
DROP MATERIALIZED VIEW IF EXISTS mv_ranking_clan CASCADE;
DROP TABLE IF EXISTS Historial_Loot CASCADE;
DROP TABLE IF EXISTS Inscripcion_Raid CASCADE;
DROP TABLE IF EXISTS Inventario CASCADE;
DROP TABLE IF EXISTS Auditoria_Liderazgo CASCADE;
DROP TABLE IF EXISTS Personaje CASCADE;
DROP TABLE IF EXISTS Clan CASCADE;
DROP TABLE IF EXISTS Raid CASCADE;
DROP TABLE IF EXISTS Item_Clase_Permitida CASCADE;
DROP TABLE IF EXISTS Item CASCADE;
DROP TABLE IF EXISTS Jugador CASCADE;

DROP PROCEDURE IF EXISTS sp_distribuir_botin;
DROP PROCEDURE IF EXISTS sp_crear_raid_e_invitar;
DROP FUNCTION IF EXISTS fn_validar_item_level;
DROP FUNCTION IF EXISTS fn_auditar_liderazgo;

-- ==============================================================================
-- 2. CREACIÓN DE TABLAS (DDL)
-- ==============================================================================

CREATE TABLE Jugador (
                         id_jugador SERIAL PRIMARY KEY,
                         username VARCHAR(50) UNIQUE NOT NULL,
                         password VARCHAR(255) NOT NULL,
                         rol VARCHAR(20) NOT NULL
);

CREATE TABLE Item (
                      id_item SERIAL PRIMARY KEY,
                      nombre VARCHAR(100) NOT NULL,
                      item_lvl INT NOT NULL,
                      ganancia_dkp INT DEFAULT 0
);

-- Tu nueva tabla del diagrama
CREATE TABLE Item_Clase_Permitida (
                                      id_item INT NOT NULL,
                                      clase_permitida VARCHAR(50) NOT NULL,
                                      PRIMARY KEY (id_item, clase_permitida),
                                      FOREIGN KEY (id_item) REFERENCES Item(id_item)
);

CREATE TABLE Raid (
                      id_raid SERIAL PRIMARY KEY,
                      nombre VARCHAR(100) NOT NULL,
                      fecha TIMESTAMP NOT NULL,
                      estado VARCHAR(20) NOT NULL,
                      item_level_requerido INT NOT NULL,
                      cupos_tanque INT NOT NULL,
                      cupos_healer INT NOT NULL,
                      cupos_dps INT NOT NULL
);

CREATE TABLE Clan (
                      id_clan SERIAL PRIMARY KEY,
                      nombre VARCHAR(100) UNIQUE NOT NULL,
                      id_lider INT NOT NULL,
                      FOREIGN KEY (id_lider) REFERENCES Jugador(id_jugador)
);

CREATE TABLE Personaje (
                           id_personaje SERIAL PRIMARY KEY,
                           id_jugador INT NOT NULL,
                           id_clan INT,
                           nombre VARCHAR(50) UNIQUE NOT NULL,
                           clase VARCHAR(50) NOT NULL,
                           nivel INT NOT NULL,
                           faccion VARCHAR(50) NOT NULL,
                           item_level INT NOT NULL,
                           puntos_merito INT DEFAULT 0,
                           rol_clan VARCHAR(50),
                           FOREIGN KEY (id_jugador) REFERENCES Jugador(id_jugador),
                           FOREIGN KEY (id_clan) REFERENCES Clan(id_clan)
);

CREATE TABLE Auditoria_Liderazgo (
                                     id_auditoria SERIAL PRIMARY KEY,
                                     id_clan INT NOT NULL,
                                     id_antiguo_lider INT NOT NULL,
                                     id_nuevo_lider INT NOT NULL,
                                     fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     FOREIGN KEY (id_clan) REFERENCES Clan(id_clan),
                                     FOREIGN KEY (id_antiguo_lider) REFERENCES Jugador(id_jugador),
                                     FOREIGN KEY (id_nuevo_lider) REFERENCES Jugador(id_jugador)
);

CREATE TABLE Inventario (
                            id_inventario SERIAL PRIMARY KEY,
                            id_item INT NOT NULL,
                            id_personaje INT NOT NULL,
                            cantidad INT DEFAULT 1,
                            equipado BOOLEAN DEFAULT FALSE,
                            FOREIGN KEY (id_item) REFERENCES Item(id_item),
                            FOREIGN KEY (id_personaje) REFERENCES Personaje(id_personaje)
);

CREATE TABLE Inscripcion_Raid (
                                  id_inscripcion SERIAL PRIMARY KEY,
                                  id_raid INT NOT NULL,
                                  id_personaje INT NOT NULL,
                                  estado VARCHAR(20) NOT NULL,
                                  asistio BOOLEAN DEFAULT FALSE,
                                  FOREIGN KEY (id_raid) REFERENCES Raid(id_raid),
                                  FOREIGN KEY (id_personaje) REFERENCES Personaje(id_personaje)
);

CREATE TABLE Historial_Loot (
                                id_historial SERIAL PRIMARY KEY,
                                id_raid INT NOT NULL,
                                id_personaje INT NOT NULL,
                                id_item INT NOT NULL,
                                fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                estado_loot VARCHAR(50),
                                FOREIGN KEY (id_raid) REFERENCES Raid(id_raid),
                                FOREIGN KEY (id_personaje) REFERENCES Personaje(id_personaje),
                                FOREIGN KEY (id_item) REFERENCES Item(id_item)
);

-- ==============================================================================
-- 3. ÍNDICES DE RENDIMIENTO (Requerimiento 8)
-- ==============================================================================
-- Acelera la búsqueda de personajes por clase para armar grupos
CREATE INDEX idx_personaje_clase ON Personaje(clase);
-- Acelera la búsqueda de quiénes van a una raid específica
CREATE INDEX idx_inscripcion_raid ON Inscripcion_Raid(id_raid);


-- ==============================================================================
-- 4. LÓGICA DE SERVIDOR: TRIGGERS
-- ==============================================================================

-- Requerimiento 5 (Trigger 1): Rechazar inscripción si el nivel de equipo es menor al requerido
CREATE OR REPLACE FUNCTION fn_validar_item_level() RETURNS TRIGGER AS $$
DECLARE
v_ilvl_personaje INT;
    v_ilvl_raid INT;
BEGIN
SELECT item_level INTO v_ilvl_personaje FROM Personaje WHERE id_personaje = NEW.id_personaje;
SELECT item_level_requerido INTO v_ilvl_raid FROM Raid WHERE id_raid = NEW.id_raid;

IF v_ilvl_personaje < v_ilvl_raid THEN
        RAISE EXCEPTION 'Inscripción rechazada: El Personaje tiene iLvl %, pero la Raid exige %', v_ilvl_personaje, v_ilvl_raid;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_ilvl
    BEFORE INSERT ON Inscripcion_Raid
    FOR EACH ROW EXECUTE FUNCTION fn_validar_item_level();


-- Requerimiento 6 (Trigger 2): Auditar cambio de liderazgo en el Clan
CREATE OR REPLACE FUNCTION fn_auditar_liderazgo() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.id_lider IS DISTINCT FROM NEW.id_lider THEN
        INSERT INTO Auditoria_Liderazgo (id_clan, id_antiguo_lider, id_nuevo_lider)
        VALUES (NEW.id_clan, OLD.id_lider, NEW.id_lider);
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_auditar_lider
    AFTER UPDATE OF id_lider ON Clan
    FOR EACH ROW EXECUTE FUNCTION fn_auditar_liderazgo();


-- ==============================================================================
-- 5. LÓGICA DE SERVIDOR: PROCEDIMIENTOS ALMACENADOS (Transacciones Atómicas)
-- ==============================================================================

-- Requerimiento 3 (SP 1): Distribuir loot y deducir DKP
CREATE OR REPLACE PROCEDURE sp_distribuir_botin(
    p_id_personaje INT,
    p_id_item INT,
    p_id_raid INT,
    p_costo_dkp INT
) LANGUAGE plpgsql AS $$
BEGIN
    -- 1. Restar el DKP al personaje
UPDATE Personaje SET puntos_merito = puntos_merito - p_costo_dkp WHERE id_personaje = p_id_personaje;

-- 2. Guardar en el historial inmutable
INSERT INTO Historial_Loot (id_raid, id_personaje, id_item, estado_loot)
VALUES (p_id_raid, p_id_personaje, p_id_item, 'Entregado');

-- 3. Agregar a la mochila (Inventario) del personaje
INSERT INTO Inventario (id_item, id_personaje, cantidad, equipado)
VALUES (p_id_item, p_id_personaje, 1, FALSE);
END;
$$;


-- Requerimiento 4 (SP 2): Generar Raid e invitar masivamente a los Raiders
CREATE OR REPLACE PROCEDURE sp_crear_raid_e_invitar(
    p_nombre VARCHAR,
    p_fecha TIMESTAMP,
    p_item_level INT,
    p_tanques INT,
    p_healers INT,
    p_dps INT
) LANGUAGE plpgsql AS $$
DECLARE
v_id_raid_nueva INT;
BEGIN
    -- 1. Crear el evento
INSERT INTO Raid (nombre, fecha, estado, item_level_requerido, cupos_tanque, cupos_healer, cupos_dps)
VALUES (p_nombre, p_fecha, 'Programada', p_item_level, p_tanques, p_healers, p_dps)
    RETURNING id_raid INTO v_id_raid_nueva;

-- 2. Invitar masivamente a todos los que tengan el rol 'Raider'
INSERT INTO Inscripcion_Raid (id_raid, id_personaje, estado, asistio)
SELECT v_id_raid_nueva, id_personaje, 'Pendiente', FALSE
FROM Personaje WHERE rol_clan = 'Raider';
END;
$$;


-- ==============================================================================
-- 6. VISTAS MATERIALIZADAS (Reportes Complejos)
-- ==============================================================================

-- Requerimiento 7: Ranking del clan por asistencia perfecta
CREATE MATERIALIZED VIEW mv_ranking_clan AS
SELECT
    p.id_personaje,
    p.nombre,
    p.clase,
    p.puntos_merito AS dkp_actual,
    COUNT(i.id_inscripcion) AS total_raids_asistidas
FROM Personaje p
         JOIN Inscripcion_Raid i ON p.id_personaje = i.id_personaje
WHERE i.asistio = TRUE
GROUP BY p.id_personaje, p.nombre, p.clase, p.puntos_merito
ORDER BY total_raids_asistidas DESC, dkp_actual DESC;

-- Nota: Para refrescar esta vista en el futuro, Java debe enviar el comando:
-- REFRESH MATERIALIZED VIEW mv_ranking_clan;

-- ==============================================================================
-- 7. NOTA: Los datos de prueba (Jugadores, Clan, Personajes, Raids)
--    se insertan desde Java (DataSeeder.java) para que las contraseñas
--    se hasheen con BCrypt antes de guardarse.
-- ==============================================================================
