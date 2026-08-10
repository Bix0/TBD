package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Item;
import com.grupo3.mmorpg.models.ItemClasePermitida;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones JDBC con las tablas Item e Item_Clase_Permitida
 * Utiliza JdbcTemplate para ejecutar consultas SQL
 */
@Repository
public class ItemRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    // RowMapper para mapear resultados de ResultSet a objetos Item
    private static final RowMapper<Item> ITEM_ROW_MAPPER = (rs, rowNum) -> {
        Item item = new Item();
        item.setId_item(rs.getLong("id_item"));
        item.setNombre(rs.getString("nombre"));
        item.setItem_lvl(rs.getInt("item_lvl"));
        item.setGanancia_dkp(rs.getInt("ganancia_dkp"));
        return item;
    };
    
    // RowMapper para mapear resultados de ResultSet a objetos ItemClasePermitida
    private static final RowMapper<ItemClasePermitida> ITEM_CLASE_ROW_MAPPER = (rs, rowNum) -> {
        ItemClasePermitida itemClase = new ItemClasePermitida();
        itemClase.setId_item(rs.getLong("id_item"));
        itemClase.setClase_permitida(rs.getString("clase_permitida"));
        return itemClase;
    };
    
    public ItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // CRUD PARA ITEM
    /**
     * Crea un nuevo item en la base de datos
     * INSERT INTO Item (nombre, item_lvl, ganancia_dkp) VALUES (?, ?, ?)
     */
    public int create(Item item) {
        String sql = "INSERT INTO Item (nombre, item_lvl, ganancia_dkp) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql, item.getNombre(), item.getItem_lvl(), item.getGanancia_dkp());
    }
    
    /**
     * Busca un item por su ID
     * SELECT * FROM Item WHERE id_item = ?
     */
    public Optional<Item> findById(Long id) {
        String sql = "SELECT * FROM Item WHERE id_item = ?";
        List<Item> result = jdbcTemplate.query(sql, ITEM_ROW_MAPPER, id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
    
    /**
     * Obtiene todos los items
     * SELECT * FROM Item ORDER BY id_item
     */
    public List<Item> findAll() {
        String sql = "SELECT * FROM Item ORDER BY id_item";
        return jdbcTemplate.query(sql, ITEM_ROW_MAPPER);
    }
    
    /**
     * Actualiza un item existente
     * UPDATE Item SET nombre = ?, item_lvl = ?, ganancia_dkp = ? WHERE id_item = ?
     */
    public int update(Item item) {
        String sql = "UPDATE Item SET nombre = ?, item_lvl = ?, ganancia_dkp = ? WHERE id_item = ?";
        return jdbcTemplate.update(sql, item.getNombre(), item.getItem_lvl(), item.getGanancia_dkp(), item.getId_item());
    }
    
    /**
     * Elimina un item por su ID
     * DELETE FROM Item WHERE id_item = ?
     */
    public int deleteById(Long id) {
        String sql = "DELETE FROM Item WHERE id_item = ?";
        return jdbcTemplate.update(sql, id);
    }

    // METODOS PARA ITEM

    /**
     * Busca un item por nombre
     * SELECT * FROM Item WHERE nombre = ?
     */
    public Optional<Item> findByName(String nombre) {
        String sql = "SELECT * FROM Item WHERE nombre = ?";
        List<Item> result = jdbcTemplate.query(sql, ITEM_ROW_MAPPER, nombre);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
    
    /**
     * Obtiene items por item_lvl mínimo
     * SELECT * FROM Item WHERE item_lvl >= ? ORDER BY item_lvl DESC
     */
    public List<Item> findByItemLevelMin(Integer itemLevel) {
        String sql = "SELECT * FROM Item WHERE item_lvl >= ? ORDER BY item_lvl DESC";
        return jdbcTemplate.query(sql, ITEM_ROW_MAPPER, itemLevel);
    }
    
    // METODOS PARA ITEM_CLASE_PERMITIDA (Falta implementar)
    /**
     * Agrega una clase permitida para un item
     * INSERT INTO Item_Clase_Permitida (id_item, clase_permitida) VALUES (?, ?)
     */
    public int agregarClasePermitida(Long idItem, String clase) {
        String sql = "INSERT INTO Item_Clase_Permitida (id_item, clase_permitida) VALUES (?, ?)";
        return jdbcTemplate.update(sql, idItem, clase);
    }
    
    /**
     * Elimina una clase permitida para un item
     * DELETE FROM Item_Clase_Permitida WHERE id_item = ? AND clase_permitida = ?
     */
    public int eliminarClasePermitida(Long idItem, String clase) {
        String sql = "DELETE FROM Item_Clase_Permitida WHERE id_item = ? AND clase_permitida = ?";
        return jdbcTemplate.update(sql, idItem, clase);
    }
    
    /**
     * Obtiene todas las clases permitidas para un item
     * SELECT clase_permitida FROM Item_Clase_Permitida WHERE id_item = ?
     */
    public List<String> getClasesPermitidas(Long idItem) {
        String sql = "SELECT clase_permitida FROM Item_Clase_Permitida WHERE id_item = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("clase_permitida"), idItem);
    }
    
    /**
     * Verifica si una clase está permitida para un item
     * SELECT COUNT(*) FROM Item_Clase_Permitida WHERE id_item = ? AND clase_permitida = ?
     */
    public boolean isClasePermitida(Long idItem, String clase) {
        String sql = "SELECT COUNT(*) FROM Item_Clase_Permitida WHERE id_item = ? AND clase_permitida = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idItem, clase);
        return count != null && count > 0;
    }
    
    /**
     * Obtiene todas las clases permitidas para un item
     * SELECT * FROM Item_Clase_Permitida WHERE id_item = ?
     */
    public List<ItemClasePermitida> getClasesPermitidasCompleto(Long idItem) {
        String sql = "SELECT * FROM Item_Clase_Permitida WHERE id_item = ?";
        return jdbcTemplate.query(sql, ITEM_CLASE_ROW_MAPPER, idItem);
    }
}