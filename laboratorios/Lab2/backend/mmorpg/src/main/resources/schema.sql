-- ============================================
-- schema.sql para Lab2 (JPA + Hibernate Spatial)
-- ============================================
-- NOTA: Las tablas son creadas por Hibernate (ddl-auto=update)
-- Este archivo solo contiene objetos que JPA NO maneja:
--   - Extensiones PostGIS
--   - Índices adicionales
--   - Procedimientos almacenados
--   - Vistas materializadas
--   - Triggers + funciones
-- ============================================

-- 0. EXTENSIÓN POSTGIS (necesaria para tipos espaciales)
CREATE EXTENSION IF NOT EXISTS postgis//

-- 1. ÍNDICES DE RENDIMIENTO (complementan los que crea Hibernate)
CREATE INDEX IF NOT EXISTS idx_personaje_clase ON Personaje(clase)//
CREATE INDEX IF NOT EXISTS idx_inscripcion_raid ON Inscripcion_Raid(id_raid)//
CREATE INDEX IF NOT EXISTS idx_inventario_personaje ON Inventario(id_personaje)//

-- 2. VISTA MATERIALIZADA (Ranking de personajes)
DROP MATERIALIZED VIEW IF EXISTS mv_ranking_clan CASCADE//
CREATE MATERIALIZED VIEW mv_ranking_clan AS
SELECT p.id_personaje, p.nombre, p.clase, p.puntos_merito AS dkp_actual,
       COUNT(i.id_inscripcion) AS total_raids_asistidas
FROM Personaje p
JOIN Inscripcion_Raid i ON p.id_personaje = i.id_personaje
WHERE i.asistio = TRUE
GROUP BY p.id_personaje, p.nombre, p.clase, p.puntos_merito
ORDER BY total_raids_asistidas DESC, dkp_actual DESC//

-- 3. PROCEDIMIENTOS ALMACENADOS
DROP PROCEDURE IF EXISTS sp_distribuir_botin(INT, INT, INT, INT)//
DROP PROCEDURE IF EXISTS sp_distribuir_botin(BIGINT, BIGINT, BIGINT, INT)//

CREATE OR REPLACE PROCEDURE sp_distribuir_botin(
    p_id_personaje BIGINT, p_id_item BIGINT, p_id_raid BIGINT, p_costo_dkp INT
)
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE Personaje SET puntos_merito = puntos_merito - p_costo_dkp WHERE id_personaje = p_id_personaje;
    INSERT INTO Historial_Loot (id_raid, id_personaje, id_item, estado_loot)
    VALUES (p_id_raid, p_id_personaje, p_id_item, 'Botín Ganado');
    INSERT INTO Inventario (id_item, id_personaje, cantidad, equipado)
    VALUES (p_id_item, p_id_personaje, 1, FALSE);
    UPDATE Raid SET estado = 'Completada' WHERE id_raid = p_id_raid;
    UPDATE Inscripcion_Raid SET asistio = TRUE, estado = 'Completada' WHERE id_raid = p_id_raid;
END;
$$//

CREATE OR REPLACE PROCEDURE sp_crear_raid_e_invitar(
    p_nombre VARCHAR, p_fecha TIMESTAMP, p_item_level INT,
    p_tanques INT, p_healers INT, p_dps INT
)
LANGUAGE plpgsql AS $$
DECLARE v_id_raid_nueva INT;
BEGIN
    INSERT INTO Raid (nombre, fecha, estado, item_level_requerido, cupos_tanque, cupos_healer, cupos_dps)
    VALUES (p_nombre, p_fecha, 'Programada', p_item_level, p_tanques, p_healers, p_dps)
    RETURNING id_raid INTO v_id_raid_nueva;
    INSERT INTO Inscripcion_Raid (id_raid, id_personaje, estado, asistio)
    SELECT v_id_raid_nueva, id_personaje, 'Pendiente', FALSE
    FROM Personaje WHERE rol_clan = 'Raider';
END;
$$//

-- 4. TRIGGERS

-- T1: Validar Item Level al inscribirse a Raid
CREATE OR REPLACE FUNCTION fn_validar_item_level() RETURNS TRIGGER AS $$
DECLARE v_ilvl_personaje INT; v_ilvl_raid INT;
BEGIN
    SELECT item_level INTO v_ilvl_personaje FROM Personaje WHERE id_personaje = NEW.id_personaje;
    SELECT item_level_requerido INTO v_ilvl_raid FROM Raid WHERE id_raid = NEW.id_raid;
    IF v_ilvl_personaje < v_ilvl_raid THEN RAISE EXCEPTION 'Poder Insuficiente'; END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql//
DROP TRIGGER IF EXISTS trg_validar_ilvl ON Inscripcion_Raid//
CREATE TRIGGER trg_validar_ilvl BEFORE INSERT ON Inscripcion_Raid FOR EACH ROW EXECUTE FUNCTION fn_validar_item_level()//

-- T2: Auditar Liderazgo (Historial de Reyes)
CREATE OR REPLACE FUNCTION fn_auditar_liderazgo() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.id_lider IS DISTINCT FROM NEW.id_lider THEN
        INSERT INTO Auditoria_Liderazgo (id_clan, id_antiguo_lider, id_nuevo_lider)
        VALUES (NEW.id_clan, OLD.id_lider, NEW.id_lider);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql//
DROP TRIGGER IF EXISTS trg_auditar_lider ON Clan//
CREATE TRIGGER trg_auditar_lider AFTER UPDATE OF id_lider ON Clan FOR EACH ROW EXECUTE FUNCTION fn_auditar_liderazgo()//

-- T3: Entregar Ítem de Bienvenida al Crear Personaje
CREATE OR REPLACE FUNCTION fn_entregar_item_inicial() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO Inventario (id_item, id_personaje, cantidad, equipado)
    VALUES (1, NEW.id_personaje, 1, TRUE);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql//
DROP TRIGGER IF EXISTS trg_item_inicial ON Personaje//
CREATE TRIGGER trg_item_inicial AFTER INSERT ON Personaje FOR EACH ROW EXECUTE FUNCTION fn_entregar_item_inicial()//

-- T4: Sistema de Ascenso a Líder automático por DKP
CREATE OR REPLACE FUNCTION fn_check_lider_dkp() RETURNS TRIGGER AS $$
DECLARE
    v_lider_actual_personaje INT;
    v_lider_actual_dkp INT;
    v_clan_real INT;
BEGIN
    v_clan_real := COALESCE(NEW.id_clan, (CASE WHEN NEW.faccion = 'Alianza' THEN 1 ELSE 2 END));
    SELECT id_lider INTO v_lider_actual_personaje FROM Clan WHERE id_clan = v_clan_real;
    SELECT COALESCE(MAX(puntos_merito), 0) INTO v_lider_actual_dkp FROM Personaje WHERE id_personaje = v_lider_actual_personaje;

    IF NEW.puntos_merito > v_lider_actual_dkp AND NEW.id_personaje != v_lider_actual_personaje THEN
        UPDATE Clan SET id_lider = NEW.id_personaje WHERE id_clan = v_clan_real;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql//
DROP TRIGGER IF EXISTS trg_check_lider_dkp ON Personaje//
CREATE TRIGGER trg_check_lider_dkp AFTER UPDATE OF puntos_merito ON Personaje FOR EACH ROW EXECUTE FUNCTION fn_check_lider_dkp()//

-- T5: Evitar Equipar 2 Objetos a la vez
CREATE OR REPLACE FUNCTION fn_equipo_unico() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.equipado = TRUE THEN
        UPDATE Inventario SET equipado = FALSE
        WHERE id_personaje = NEW.id_personaje AND id_inventario <> NEW.id_inventario AND equipado = TRUE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql//
DROP TRIGGER IF EXISTS trg_equipo_unico ON Inventario//
CREATE TRIGGER trg_equipo_unico AFTER UPDATE OF equipado ON Inventario FOR EACH ROW EXECUTE FUNCTION fn_equipo_unico()//

-- T6: Actualizar Poder Total al Equipar/Desequipar Armas
CREATE OR REPLACE FUNCTION fn_actualizar_poder() RETURNS TRIGGER AS $$
DECLARE v_personaje_id INT; v_poder_armas INT;
BEGIN
    v_personaje_id := COALESCE(NEW.id_personaje, OLD.id_personaje);
    SELECT COALESCE(SUM(i.item_lvl), 0) INTO v_poder_armas
    FROM Inventario inv JOIN Item i ON inv.id_item = i.id_item
    WHERE inv.id_personaje = v_personaje_id AND inv.equipado = TRUE;
    UPDATE Personaje SET item_level = nivel + v_poder_armas WHERE id_personaje = v_personaje_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql//
DROP TRIGGER IF EXISTS trg_actualizar_poder ON Inventario//
CREATE TRIGGER trg_actualizar_poder AFTER INSERT OR UPDATE OF equipado OR DELETE ON Inventario FOR EACH ROW EXECUTE FUNCTION fn_actualizar_poder()//

-- T7: Recalcular Poder Total al Subir/Bajar de Nivel manualmente
CREATE OR REPLACE FUNCTION fn_recalcular_poder_por_nivel() RETURNS TRIGGER AS $$
DECLARE v_poder_armas INT;
BEGIN
    SELECT COALESCE(SUM(i.item_lvl), 0) INTO v_poder_armas
    FROM Inventario inv JOIN Item i ON inv.id_item = i.id_item
    WHERE inv.id_personaje = NEW.id_personaje AND inv.equipado = TRUE;
    UPDATE Personaje SET item_level = NEW.nivel + v_poder_armas WHERE id_personaje = NEW.id_personaje;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql//
DROP TRIGGER IF EXISTS trg_recalcular_poder_por_nivel ON Personaje//
CREATE TRIGGER trg_recalcular_poder_por_nivel AFTER UPDATE OF nivel ON Personaje FOR EACH ROW
WHEN (OLD.nivel IS DISTINCT FROM NEW.nivel) EXECUTE FUNCTION fn_recalcular_poder_por_nivel()//
