package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Raid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones JDBC con la tabla Raid
 * Utiliza JdbcTemplate para ejecutar consultas SQL
 * Incluye métodos para procedimientos almacenados
 */
@Repository
public class RaidRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    // RowMapper para mapear resultados de ResultSet a objetos Raid
    private static final RowMapper<Raid> RAID_ROW_MAPPER = (rs, rowNum) -> {
        Raid raid = new Raid();
        raid.setId_raid(rs.getLong("id_raid"));
        raid.setNombre(rs.getString("nombre"));
        raid.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        raid.setEstado(rs.getString("estado"));
        raid.setItem_level_requerido(rs.getInt("item_level_requerido"));
        raid.setCupos_tanque(rs.getInt("cupos_tanque"));
        raid.setCupos_healer(rs.getInt("cupos_healer"));
        raid.setCupos_dps(rs.getInt("cupos_dps"));
        return raid;
    };
    
    public RaidRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    // ============================================================================
    // CRUD BÁSICOS
    // ============================================================================
    
    /**
     * Crea una nueva raid en la base de datos
     * INSERT INTO Raid (nombre, fecha, estado, item_level_requerido, cupos_tanque, cupos_healer, cupos_dps) VALUES (?, ?, ?, ?, ?, ?, ?)
     */
    public int create(Raid raid) {
        String sql = "INSERT INTO Raid (nombre, fecha, estado, item_level_requerido, cupos_tanque, cupos_healer, cupos_dps) VALUES (?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
            raid.getNombre(),
            raid.getFecha(),
            raid.getEstado(),
            raid.getItem_level_requerido(),
            raid.getCupos_tanque(),
            raid.getCupos_healer(),
            raid.getCupos_dps()
        );
    }

    /**
     * Busca una raid por su ID
     * SELECT * FROM Raid WHERE id_raid = ?
     */
    public Optional<Raid> findById(Long id) {
        return null;
    }
    
    /**
     * Obtiene todas las raids
     * SELECT * FROM Raid ORDER BY fecha DESC
     */
    public List<Raid> findAll() {
        String sql = "SELECT * FROM Raid ORDER BY fecha DESC";
        return jdbcTemplate.query(sql, RAID_ROW_MAPPER);
    }
    
    /**
     * Actualiza una raid existente
     * UPDATE Raid SET nombre = ?, fecha = ?, estado = ?, item_level_requerido = ?, cupos_tanque = ?, cupos_healer = ?, cupos_dps = ? WHERE id_raid = ?
     */
    public int update(Raid raid) {
        String sql = "UPDATE Raid SET nombre = ?, fecha = ?, estado = ?, item_level_requerido = ?, cupos_tanque = ?, cupos_healer = ?, cupos_dps = ? WHERE id_raid = ?";
        return jdbcTemplate.update(sql,
            raid.getNombre(),
            raid.getFecha(),
            raid.getEstado(),
            raid.getItem_level_requerido(),
            raid.getCupos_tanque(),
            raid.getCupos_healer(),
            raid.getCupos_dps(),
            raid.getId_raid()
        );
    }
    
    /**
     * Cambia el estado de una raid
     * UPDATE Raid SET estado = ? WHERE id_raid = ?
     */
    public int updateEstado(Long idRaid, String estado) {
        String sql = "UPDATE Raid SET estado = ? WHERE id_raid = ?";
        return jdbcTemplate.update(sql, estado, idRaid);
    }
    
    /**
     * Elimina una raid por su ID
     * DELETE FROM Raid WHERE id_raid = ?
     */
    public int deleteById(Long id) {
        String sql = "DELETE FROM Raid WHERE id_raid = ?";
        return jdbcTemplate.update(sql, id);
    }
    
    // ============================================================================
    // MÉTODOS ESPECÍFICOS
    // ============================================================================
    
    /**
     * Obtiene raids por estado
     * SELECT * FROM Raid WHERE estado = ? ORDER BY fecha DESC
     */
    public List<Raid> findByEstado(String estado) {
        String sql = "SELECT * FROM Raid WHERE estado = ? ORDER BY fecha DESC";
        return jdbcTemplate.query(sql, new Object[]{estado}, RAID_ROW_MAPPER);
    }
    
    /**
     * Obtiene raids con item_level_requerido mayor o igual a un valor
     * SELECT * FROM Raid WHERE item_level_requerido >= ? ORDER BY item_level_requerido DESC
     */
    public List<Raid> findByItemLevelMin(Integer itemLevel) {
        String sql = "SELECT * FROM Raid WHERE item_level_requerido >= ? ORDER BY item_level_requerido DESC";
        return jdbcTemplate.query(sql, new Object[]{itemLevel}, RAID_ROW_MAPPER);
    }
    
    /**
     * Obtiene raids programadas (estado = 'Programada')
     * SELECT * FROM Raid WHERE estado = 'Programada' ORDER BY fecha ASC
     */
    public List<Raid> findProgramadas() {
        String sql = "SELECT * FROM Raid WHERE estado = 'Programada' ORDER BY fecha ASC";
        return jdbcTemplate.query(sql, RAID_ROW_MAPPER);
    }
    
    /**
     * Crea una raid y realiza inscripción masiva usando el procedimiento almacenado
     * CALL sp_crear_raid_e_invitar(p_nombre, p_fecha, p_item_level, p_tanques, p_healers, p_dps)
     * Este SP crea la raid e invita automáticamente a todos los personajes con rol_clan = 'Raider'
     */
    public Long crearRaidConInscripcionMasiva(String nombre, LocalDateTime fecha, 
                                               Integer itemLevel, Integer tanques, 
                                               Integer heals, Integer dps) {
        String sql = "CALL sp_crear_raid_e_invitar(?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, nombre, fecha, itemLevel, tanques, heals, dps);
        
        // Obtener el ID de la raid recién creada
        String sqlGetId = "SELECT id_raid FROM Raid WHERE nombre = ? ORDER BY id_raid DESC LIMIT 1";
        List<Long> result = jdbcTemplate.query(sqlGetId, new Object[]{nombre},
            (rs, rowNum) -> rs.getLong("id_raid"));
        return result.isEmpty() ? null : result.get(0);
    }
    
    /**
     * Inscribe un personaje a una raid
     * INSERT INTO Inscripcion_Raid (id_raid, id_personaje, estado, asistio) VALUES (?, ?, 'Pendiente', FALSE)
     * Este INSERT activa el trigger trg_validar_ilvl
     */
    public int inscribirPersonaje(Long idRaid, Long idPersonaje) {
        String sql = "INSERT INTO Inscripcion_Raid (id_raid, id_personaje, estado, asistio) VALUES (?, ?, 'Pendiente', FALSE)";
        return jdbcTemplate.update(sql, idRaid, idPersonaje);
    }
    
    /**
     * Desinscribe un personaje de una raid
     * DELETE FROM Inscripcion_Raid WHERE id_raid = ? AND id_personaje = ?
     */
    public int desinscribirPersonaje(Long idRaid, Long idPersonaje) {
        String sql = "DELETE FROM Inscripcion_Raid WHERE id_raid = ? AND id_personaje = ?";
        return jdbcTemplate.update(sql, idRaid, idPersonaje);
    }
    
    /**
     * Obtiene las inscripciones de una raid
     * SELECT * FROM Inscripcion_Raid WHERE id_raid = ? ORDER BY id_inscripcion
     */
    public List<Object[]> getInscripcionesRaid(Long idRaid) {
        String sql = "SELECT ir.id_inscripcion, ir.id_personaje, p.nombre, p.clase, ir.estado, ir.asistio " +
                     "FROM Inscripcion_Raid ir " +
                     "JOIN Personaje p ON ir.id_personaje = p.id_personaje " +
                     "WHERE ir.id_raid = ? ORDER BY ir.id_inscripcion";
        return jdbcTemplate.query(sql, new Object[]{idRaid},
            (rs, rowNum) -> new Object[]{
                rs.getLong("id_inscripcion"),
                rs.getLong("id_personaje"),
                rs.getString("nombre"),
                rs.getString("clase"),
                rs.getString("estado"),
                rs.getBoolean("asistio")
            });
    }
    
    /**
     * Cuenta las inscripciones por estado para una raid
     * SELECT estado, COUNT(*) FROM Inscripcion_Raid WHERE id_raid = ? GROUP BY estado
     */
    public List<Object[]> contarInscripcionesPorEstado(Long idRaid) {
        String sql = "SELECT estado, COUNT(*) FROM Inscripcion_Raid WHERE id_raid = ? GROUP BY estado";
        return jdbcTemplate.query(sql, new Object[]{idRaid},
            (rs, rowNum) -> new Object[]{rs.getString("estado"), rs.getInt("COUNT(*)")});
    }

    /**
     * Guarda los cupos por clase modificados de la raid
     * "UPDATE Raid SET cupos_tanque = ?, cupos_healer = ?, cupos_dps = ? WHERE id_raid = ?"
     */
    public void saveCupos(Raid raid) {
        String sql = "UPDATE Raid SET cupos_tanque = ?, cupos_healer = ?, cupos_dps = ? WHERE id_raid = ?";

        int filasAfectadas = jdbcTemplate.update(sql,
                raid.getCupos_tanque(),
                raid.getCupos_healer(),
                raid.getCupos_dps(),
                raid.getId_raid()
        );

        if (filasAfectadas == 0) {
            throw new RuntimeException("No se pudo actualizar la Raid. ID no encontrado.");
        }
    }
}