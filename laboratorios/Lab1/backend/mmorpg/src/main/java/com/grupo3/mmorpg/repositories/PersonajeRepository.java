package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Personaje;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones JDBC con la tabla Personaje
 * Utiliza JdbcTemplate para ejecutar consultas SQL
 */
@Repository
public class PersonajeRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    // RowMapper para mapear resultados de ResultSet a objetos Personaje
    private static final RowMapper<Personaje> PERSONAJE_ROW_MAPPER = (rs, rowNum) -> {
        Personaje personaje = new Personaje();
        personaje.setId_personaje(rs.getLong("id_personaje"));
        personaje.setId_jugador(rs.getLong("id_jugador"));
        personaje.setId_clan(rs.getLong("id_clan"));
        personaje.setNombre(rs.getString("nombre"));
        personaje.setClase(rs.getString("clase"));
        personaje.setNivel(rs.getInt("nivel"));
        personaje.setFaccion(rs.getString("faccion"));
        personaje.setItem_level(rs.getInt("item_level"));
        personaje.setPuntos_merito(rs.getInt("puntos_merito"));
        personaje.setRol_clan(rs.getString("rol_clan"));
        return personaje;
    };
    
    public PersonajeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //CRUD
    /**
     * Crea un nuevo personaje en la base de datos
     * INSERT INTO Personaje (id_jugador, id_clan, nombre, clase, nivel, faccion, item_level, puntos_merito, rol_clan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
     */
    public int create(Personaje personaje) {
        String sql = "INSERT INTO Personaje (id_jugador, id_clan, nombre, clase, nivel, faccion, item_level, puntos_merito, rol_clan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
            personaje.getId_jugador(),
            personaje.getId_clan(),
            personaje.getNombre(),
            personaje.getClase(),
            personaje.getNivel(),
            personaje.getFaccion(),
            personaje.getItem_level(),
            personaje.getPuntos_merito(),
            personaje.getRol_clan()
        );
    }
    
    /**
     * Busca un personaje por su ID
     * SELECT * FROM Personaje WHERE id_personaje = ?
     */
    public Optional<Personaje> findById(Long id) {
        String sql = "SELECT * FROM Personaje WHERE id_personaje = ?";
        List<Personaje> result = jdbcTemplate.query(sql, PERSONAJE_ROW_MAPPER, id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
    
    /**
     * Obtiene todos los personajes
     * SELECT * FROM Personaje ORDER BY id_personaje
     */
    public List<Personaje> findAll() {
        String sql = "SELECT * FROM Personaje ORDER BY id_personaje";
        return jdbcTemplate.query(sql, PERSONAJE_ROW_MAPPER);
    }
    
    /**
     * Actualiza un personaje existente
     * UPDATE Personaje SET id_jugador = ?, id_clan = ?, nombre = ?, clase = ?, nivel = ?, faccion = ?, item_level = ?, puntos_merito = ?, rol_clan = ? WHERE id_personaje = ?
     */
    public int update(Personaje personaje) {
        String sql = "UPDATE Personaje SET id_jugador = ?, id_clan = ?, nombre = ?, clase = ?, nivel = ?, faccion = ?, item_level = ?, puntos_merito = ?, rol_clan = ? WHERE id_personaje = ?";
        return jdbcTemplate.update(sql,
            personaje.getId_jugador(),
            personaje.getId_clan(),
            personaje.getNombre(),
            personaje.getClase(),
            personaje.getNivel(),
            personaje.getFaccion(),
            personaje.getItem_level(),
            personaje.getPuntos_merito(),
            personaje.getRol_clan(),
            personaje.getId_personaje()
        );
    }
    
    /**
     * Elimina un personaje por su ID
     * DELETE FROM Personaje WHERE id_personaje = ?
     */
    public int deleteById(Long id) {
        String sql = "DELETE FROM Personaje WHERE id_personaje = ?";
        return jdbcTemplate.update(sql, id);
    }

    // METODOS PERSONAJE
    /**
     * Obtiene todos los personajes de un clan específico
     * SELECT * FROM Personaje WHERE id_clan = ? ORDER BY id_personaje
     */
    public List<Personaje> findAllByClanId(Long clanId) {
        String sql = "SELECT * FROM Personaje WHERE id_clan = ? ORDER BY id_personaje";
        return jdbcTemplate.query(sql, PERSONAJE_ROW_MAPPER, clanId);
    }
    
    /**
     * Obtiene personajes por clase
     * SELECT * FROM Personaje WHERE clase = ? ORDER BY id_personaje
     * Útil para armar grupos según clase
     */
    public List<Personaje> findByClase(String clase) {
        String sql = "SELECT * FROM Personaje WHERE clase = ? ORDER BY id_personaje";
        return jdbcTemplate.query(sql, PERSONAJE_ROW_MAPPER, clase);
    }
    
    /**
     * Obtiene personajes por rol de clan
     * SELECT * FROM Personaje WHERE rol_clan = ? ORDER BY id_personaje
     */
    public List<Personaje> findByRolClan(String rolClan) {
        String sql = "SELECT * FROM Personaje WHERE rol_clan = ? ORDER BY id_personaje";
        return jdbcTemplate.query(sql, PERSONAJE_ROW_MAPPER, rolClan);
    }
    
    /**
     * Obtiene personajes con item_level mayor o igual a un valor
     * SELECT * FROM Personaje WHERE item_level >= ? ORDER BY item_level DESC
     */
    public List<Personaje> findByItemLevelMin(Integer itemLevel) {
        String sql = "SELECT * FROM Personaje WHERE item_level >= ? ORDER BY item_level DESC";
        return jdbcTemplate.query(sql, PERSONAJE_ROW_MAPPER, itemLevel);
    }
    
    /**
     * Actualiza los puntos de mérito (DKP) de un personaje
     * UPDATE Personaje SET puntos_merito = puntos_merito - ? WHERE id_personaje = ?
     */
    public int updatePuntosMerito(Long idPersonaje, Integer cantidad) {
        String sql = "UPDATE Personaje SET puntos_merito = puntos_merito - ? WHERE id_personaje = ?";
        return jdbcTemplate.update(sql, cantidad, idPersonaje);
    }
    
    /**
     * Obtiene el personaje de un jugador específico
     * SELECT * FROM Personaje WHERE id_jugador = ? ORDER BY id_personaje LIMIT 1
     */
    public Optional<Personaje> findByJugadorId(Long jugadorId) {
        String sql = "SELECT * FROM Personaje WHERE id_jugador = ? ORDER BY id_personaje LIMIT 1";
        List<Personaje> result = jdbcTemplate.query(sql, PERSONAJE_ROW_MAPPER, jugadorId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
    public List<Personaje> findAllByJugadorIdList(Long jugadorId) {
        String sql = "SELECT * FROM Personaje WHERE id_jugador = ? ORDER BY id_personaje";
        return jdbcTemplate.query(sql, PERSONAJE_ROW_MAPPER, jugadorId);
    }

}