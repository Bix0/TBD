package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Jugador;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JugadorRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Jugador> JUGADOR_ROW_MAPPER = (rs, rowNum) -> {
        Jugador jugador = new Jugador();
        jugador.setId_jugador(rs.getLong("id_jugador"));
        jugador.setUsername(rs.getString("username"));
        jugador.setPassword(rs.getString("password"));
        jugador.setRol(rs.getString("rol"));
        return jugador;
    };

    public JugadorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int create(Jugador jugador) {
        String sql = "INSERT INTO Jugador (username, password, rol) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql, jugador.getUsername(), jugador.getPassword(), jugador.getRol());
    }

    public Optional<Jugador> findById(Long id) {
        String sql = "SELECT * FROM Jugador WHERE id_jugador = ?";
        List<Jugador> result = jdbcTemplate.query(sql,  JUGADOR_ROW_MAPPER, id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public List<Jugador> findAll() {
        String sql = "SELECT * FROM Jugador ORDER BY id_jugador";
        return jdbcTemplate.query(sql, JUGADOR_ROW_MAPPER);
    }

    public int update(Jugador jugador) {
        String sql = "UPDATE Jugador SET username = ?, password = ?, rol = ? WHERE id_jugador = ?";
        return jdbcTemplate.update(sql, jugador.getUsername(), jugador.getPassword(), jugador.getRol(), jugador.getId_jugador());
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM Jugador WHERE id_jugador = ?";
        return jdbcTemplate.update(sql, id);
    }

    public Optional<Jugador> findByUsername(String username) {
        String sql = "SELECT * FROM Jugador WHERE username = ?";
        List<Jugador> result = jdbcTemplate.query(sql, JUGADOR_ROW_MAPPER, username);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM Jugador WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    // Historial de botin de un jugador especifico
    public List<Object[]> obtenerHistorialBotinJugador(Long idJugador) {
        String sql = "SELECT h.fecha, p.nombre AS personaje, i.nombre AS item, r.nombre AS raid, h.estado_loot " +
                "FROM Historial_Loot h " +
                "JOIN Personaje p ON h.id_personaje = p.id_personaje " +
                "JOIN Item i ON h.id_item = i.id_item " +
                "JOIN Raid r ON h.id_raid = r.id_raid " +
                "WHERE p.id_jugador = ? ORDER BY h.fecha DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
                        rs.getTimestamp("fecha"),
                        rs.getString("personaje"),
                        rs.getString("item"),
                        rs.getString("raid"),
                        rs.getString("estado_loot")
                }, idJugador);
    }
}