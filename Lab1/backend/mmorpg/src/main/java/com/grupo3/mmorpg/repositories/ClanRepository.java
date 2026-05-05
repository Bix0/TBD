package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Clan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones JDBC con la tabla Clan
 * Utiliza JdbcTemplate para ejecutar consultas SQL
 */
@Repository
public class ClanRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    // RowMapper para mapear resultados de ResultSet a objetos Clan
    private static final RowMapper<Clan> CLAN_ROW_MAPPER = (rs, rowNum) -> {
        Clan clan = new Clan();
        clan.setId_clan(rs.getLong("id_clan"));
        clan.setNombre(rs.getString("nombre"));
        clan.setId_lider(rs.getLong("id_lider"));
        return clan;
    };
    
    public ClanRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    // ============================================================================
    // CRUD BÁSICOS
    // ============================================================================
    
    /**
     * Crea un nuevo clan en la base de datos
     * INSERT INTO Clan (nombre, id_lider) VALUES (?, ?)
     */
    public int create(Clan clan) {
        String sql = "INSERT INTO Clan (nombre, id_lider) VALUES (?, ?)";
        return jdbcTemplate.update(sql, clan.getNombre(), clan.getId_lider());
    }
    
    /**
     * Busca un clan por su ID
     * SELECT * FROM Clan WHERE id_clan = ?
     */
    public Optional<Clan> findById(Long id) {
        String sql = "SELECT * FROM Clan WHERE id_clan = ?";
        List<Clan> result = jdbcTemplate.query(sql, new Object[]{id}, CLAN_ROW_MAPPER);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
    
    /**
     * Obtiene todos los clanes
     * SELECT * FROM Clan ORDER BY id_clan
     */
    public List<Clan> findAll() {
        String sql = "SELECT * FROM Clan ORDER BY id_clan";
        return jdbcTemplate.query(sql, CLAN_ROW_MAPPER);
    }
    
    /**
     * Actualiza un clan existente
     * UPDATE Clan SET nombre = ?, id_lider = ? WHERE id_clan = ?
     */
    public int update(Clan clan) {
        String sql = "UPDATE Clan SET nombre = ?, id_lider = ? WHERE id_clan = ?";
        return jdbcTemplate.update(sql, clan.getNombre(), clan.getId_lider(), clan.getId_clan());
    }
    
    /**
     * Cambia el líder de un clan
     * UPDATE Clan SET id_lider = ? WHERE id_clan = ?
     * Este método activa el trigger trg_auditar_lider
     */
    public int updateLider(Long idClan, Long nuevoLider) {
        String sql = "UPDATE Clan SET id_lider = ? WHERE id_clan = ?";
        return jdbcTemplate.update(sql, nuevoLider, idClan);
    }
    
    /**
     * Elimina un clan por su ID
     * DELETE FROM Clan WHERE id_clan = ?
     */
    public int deleteById(Long id) {
        String sql = "DELETE FROM Clan WHERE id_clan = ?";
        return jdbcTemplate.update(sql, id);
    }
    
    // ============================================================================
    // MÉTODOS ESPECÍFICOS
    // ============================================================================
    
    /**
     * Busca un clan por su nombre
     * SELECT * FROM Clan WHERE nombre = ?
     */
    public Optional<Clan> findByName(String nombre) {
        String sql = "SELECT * FROM Clan WHERE nombre = ?";
        List<Clan> result = jdbcTemplate.query(sql, new Object[]{nombre}, CLAN_ROW_MAPPER);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
    
    /**
     * Verifica si un nombre de clan ya existe
     * SELECT COUNT(*) FROM Clan WHERE nombre = ?
     */
    public boolean existsByName(String nombre) {
        String sql = "SELECT COUNT(*) FROM Clan WHERE nombre = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{nombre}, Integer.class);
        return count != null && count > 0;
    }
    
    /**
     * Obtiene el ID del líder de un clan
     * SELECT id_lider FROM Clan WHERE id_clan = ?
     */
    public Optional<Long> getLiderId(Long idClan) {
        String sql = "SELECT id_lider FROM Clan WHERE id_clan = ?";
        List<Long> result = jdbcTemplate.query(sql, new Object[]{idClan}, 
            (rs, rowNum) -> rs.getLong("id_lider"));
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    /**
     * Agrega un personaje a un clan asignándole el ID del clan.
     * UPDATE Personaje SET id_clan = ?, rol_clan = 'Iniciado' WHERE id_personaje = ?
     */
    public int añadirMiembro(Long idClan, Long idPersonaje) {
        String sql = "UPDATE Personaje SET id_clan = ?, rol_clan = 'Iniciado' WHERE id_personaje = ?";
        return jdbcTemplate.update(sql, idClan, idPersonaje);
    }

    /**
     * Elimina a un personaje del clan (pone su id_clan en NULL).
     * UPDATE Personaje SET id_clan = NULL, rol_clan = NULL WHERE id_personaje = ?
     */
    public int eliminarMiembro(Long idPersonaje) {
        String sql = "UPDATE Personaje SET id_clan = NULL, rol_clan = NULL WHERE id_personaje = ?";
        return jdbcTemplate.update(sql, idPersonaje);
    }

    /**
     * Verifica si un personaje ya tiene clan.
     * SELECT COUNT(*) FROM Personaje WHERE id_personaje = ? AND id_clan IS NOT NULL
     */
    public boolean personajeTieneClan(Long idPersonaje) {
        String sql = "SELECT COUNT(*) FROM Personaje WHERE id_personaje = ? AND id_clan IS NOT NULL";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idPersonaje);
        return count != null && count > 0;
    }
}