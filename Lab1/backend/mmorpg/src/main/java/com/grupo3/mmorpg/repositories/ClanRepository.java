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

    //CRUD
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

    //METODOS ESPECIFICOS

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

    public List<Object[]> obtenerAuditoriaLiderazgo() {
        // Usamos LEFT JOIN y COALESCE por si es el Primer Rey y no destronó a nadie
        String sql = "SELECT a.id_auditoria, c.nombre AS clan, COALESCE(p1.nombre, 'Nadie') AS antiguo, p2.nombre AS nuevo, a.fecha_cambio " +
                "FROM Auditoria_Liderazgo a " +
                "JOIN Clan c ON a.id_clan = c.id_clan " +
                "LEFT JOIN Personaje p1 ON a.id_antiguo_lider = p1.id_personaje " +
                "JOIN Personaje p2 ON a.id_nuevo_lider = p2.id_personaje " +
                "ORDER BY a.fecha_cambio DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
                rs.getLong("id_auditoria"),
                rs.getString("clan"),
                rs.getString("antiguo"),
                rs.getString("nuevo"),
                rs.getTimestamp("fecha_cambio")
        });
    }
}