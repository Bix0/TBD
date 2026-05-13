package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Inventario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones JDBC con la tabla Inventario
 * Utiliza JdbcTemplate para ejecutar consultas SQL
 */
@Repository
public class InventarioRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    // RowMapper para mapear resultados de ResultSet a objetos Inventario
    private static final RowMapper<Inventario> INVENTARIO_ROW_MAPPER = (rs, rowNum) -> {
        Inventario inventario = new Inventario();
        inventario.setId_inventario(rs.getLong("id_inventario"));
        inventario.setId_item(rs.getLong("id_item"));
        inventario.setId_personaje(rs.getLong("id_personaje"));
        inventario.setCantidad(rs.getInt("cantidad"));
        inventario.setEquipado(rs.getBoolean("equipado"));
        return inventario;
    };
    
    public InventarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    

    //CRUD BASICOS
    /**
     * Crea un nuevo registro de inventario
     * INSERT INTO Inventario (id_item, id_personaje, cantidad, equipado) VALUES (?, ?, ?, ?)
     */
    public int create(Inventario inventario) {
        String sql = "INSERT INTO Inventario (id_item, id_personaje, cantidad, equipado) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
            inventario.getId_item(),
            inventario.getId_personaje(),
            inventario.getCantidad(),
            inventario.getEquipado()
        );
    }
    
    /**
     * Busca un registro de inventario por su ID
     * SELECT * FROM Inventario WHERE id_inventario = ?
     */
    public Optional<Inventario> findById(Long id) {
        String sql = "SELECT * FROM Inventario WHERE id_inventario = ?";
        List<Inventario> result = jdbcTemplate.query(sql, new Object[]{id}, INVENTARIO_ROW_MAPPER);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
    
    /**
     * Obtiene todos los registros de inventario
     * SELECT * FROM Inventario ORDER BY id_inventario
     */
    public List<Inventario> findAll() {
        String sql = "SELECT * FROM Inventario ORDER BY id_inventario";
        return jdbcTemplate.query(sql, INVENTARIO_ROW_MAPPER);
    }
    
    /**
     * Actualiza un registro de inventario
     * UPDATE Inventario SET id_item = ?, id_personaje = ?, cantidad = ?, equipado = ? WHERE id_inventario = ?
     */
    public int update(Inventario inventario) {
        String sql = "UPDATE Inventario SET id_item = ?, id_personaje = ?, cantidad = ?, equipado = ? WHERE id_inventario = ?";
        return jdbcTemplate.update(sql,
            inventario.getId_item(),
            inventario.getId_personaje(),
            inventario.getCantidad(),
            inventario.getEquipado(),
            inventario.getId_inventario()
        );
    }
    
    /**
     * Elimina un registro de inventario por su ID
     * DELETE FROM Inventario WHERE id_inventario = ?
     */
    public int deleteById(Long id) {
        String sql = "DELETE FROM Inventario WHERE id_inventario = ?";
        return jdbcTemplate.update(sql, id);
    }

    //METODOS ESPECIFICOS
    /**
     * Obtiene todos los items del inventario de un personaje
     * SELECT * FROM Inventario WHERE id_personaje = ? ORDER BY id_inventario
     */
    public List<Inventario> findByPersonajeId(Long personajeId) {
        String sql = "SELECT * FROM Inventario WHERE id_personaje = ? ORDER BY id_inventario";
        return jdbcTemplate.query(sql, new Object[]{personajeId}, INVENTARIO_ROW_MAPPER);
    }
    
    /**
     * Obtiene los items equipados de un personaje
     * SELECT * FROM Inventario WHERE id_personaje = ? AND equipado = TRUE ORDER BY id_inventario
     */
    public List<Inventario> findEquipadosByPersonajeId(Long personajeId) {
        String sql = "SELECT * FROM Inventario WHERE id_personaje = ? AND equipado = TRUE ORDER BY id_inventario";
        return jdbcTemplate.query(sql, new Object[]{personajeId}, INVENTARIO_ROW_MAPPER);
    }
    
    /**
     * Obtiene un item específico en el inventario de un personaje
     * SELECT * FROM Inventario WHERE id_personaje = ? AND id_item = ?
     */
    public Optional<Inventario> findByPersonajeAndItem(Long personajeId, Long itemId) {
        String sql = "SELECT * FROM Inventario WHERE id_personaje = ? AND id_item = ?";
        List<Inventario> result = jdbcTemplate.query(sql, new Object[]{personajeId, itemId}, INVENTARIO_ROW_MAPPER);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
    
    /**
     * Equipa un item en el inventario
     * UPDATE Inventario SET equipado = TRUE WHERE id_inventario = ?
     */
    public int equiparItem(Long idInventario) {
        String sql = "UPDATE Inventario SET equipado = TRUE WHERE id_inventario = ?";
        return jdbcTemplate.update(sql, idInventario);
    }
    
    /**
     * Desequipa un item en el inventario
     * UPDATE Inventario SET equipado = FALSE WHERE id_inventario = ?
     */
    public int desequiparItem(Long idInventario) {
        String sql = "UPDATE Inventario SET equipado = FALSE WHERE id_inventario = ?";
        return jdbcTemplate.update(sql, idInventario);
    }
    
    /**
     * Aumenta la cantidad de un item en el inventario
     * UPDATE Inventario SET cantidad = cantidad + ? WHERE id_inventario = ?
     */
    public int aumentarCantidad(Long idInventario, Integer cantidad) {
        String sql = "UPDATE Inventario SET cantidad = cantidad + ? WHERE id_inventario = ?";
        return jdbcTemplate.update(sql, cantidad, idInventario);
    }
    
    /**
     * Verifica si un personaje tiene un item en su inventario
     * SELECT COUNT(*) FROM Inventario WHERE id_personaje = ? AND id_item = ?
     */
    public boolean tieneItem(Long personajeId, Long itemId) {
        String sql = "SELECT COUNT(*) FROM Inventario WHERE id_personaje = ? AND id_item = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{personajeId, itemId}, Integer.class);
        return count != null && count > 0;
    }
    
    /**
     * Cuenta el total de items en el inventario de un personaje
     * SELECT COUNT(*) FROM Inventario WHERE id_personaje = ?
     */
    public int contarItems(Long personajeId) {
        String sql = "SELECT COUNT(*) FROM Inventario WHERE id_personaje = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{personajeId}, Integer.class);
        return count != null ? count : 0;
    }
}