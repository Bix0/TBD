package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Inventario;
import com.grupo3.mmorpg.repositories.InventarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de negocio relacionadas con Inventario
 * Utiliza InventarioRepository para acceder a los datos
 */
@Service
public class InventarioService {
    
    private final InventarioRepository inventarioRepository;
    
    public InventarioService(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    // CRUD BASICOS
    /**
     * Crea un nuevo registro de inventario
     * @param inventario Objeto Inventario con los datos a guardar
     * @return Número de filas afectadas (1 si se creó correctamente)
     */
    public int crearInventario(Inventario inventario) {
        return inventarioRepository.create(inventario);
    }
    
    /**
     * Obtiene un registro de inventario por su ID
     * @param id ID del registro
     * @return Optional con el registro si existe
     */
    public Optional<Inventario> obtenerInventario(Long id) {
        return inventarioRepository.findById(id);
    }
    
    /**
     * Obtiene todos los registros de inventario
     * @return Lista de todos los registros
     */
    public List<Inventario> obtenerTodosLosInventarios() {
        return inventarioRepository.findAll();
    }
    
    /**
     * Actualiza un registro de inventario existente
     * @param inventario Objeto Inventario con los datos actualizados
     * @return Número de filas afectadas
     */
    public int actualizarInventario(Inventario inventario) {
        if (!inventarioRepository.findById(inventario.getId_inventario()).isPresent()) {
            throw new IllegalArgumentException("Registro de inventario no encontrado");
        }
        return inventarioRepository.update(inventario);
    }
    
    /**
     * Elimina un registro de inventario por su ID
     * @param id ID del registro a eliminar
     * @return Número de filas afectadas
     */
    public int eliminarInventario(Long id) {
        return inventarioRepository.deleteById(id);
    }
    

    // METODOS ESPECIFICOS
    /**
     * Obtiene todos los items del inventario de un personaje
     * @param personajeId ID del personaje
     * @return Lista de items del inventario
     */
    public List<Inventario> obtenerInventarioPorPersonaje(Long personajeId) {
        return inventarioRepository.findByPersonajeId(personajeId);
    }
    
    /**
     * Obtiene los items equipados de un personaje
     * @param personajeId ID del personaje
     * @return Lista de items equipados
     */
    public List<Inventario> obtenerItemsEquipados(Long personajeId) {
        return inventarioRepository.findEquipadosByPersonajeId(personajeId);
    }
    
    /**
     * Obtiene un item específico en el inventario de un personaje
     * @param personajeId ID del personaje
     * @param itemId ID del item
     * @return Optional con el registro si existe
     */
    public Optional<Inventario> obtenerItemEnInventario(Long personajeId, Long itemId) {
        return inventarioRepository.findByPersonajeAndItem(personajeId, itemId);
    }
    
    /**
     * Equipa un item en el inventario
     * @param idInventario ID del registro de inventario
     * @return Número de filas afectadas
     */
    public int equiparItem(Long idInventario) {
        return inventarioRepository.equiparItem(idInventario);
    }
    
    /**
     * Desequipa un item en el inventario
     * @param idInventario ID del registro de inventario
     * @return Número de filas afectadas
     */
    public int desequiparItem(Long idInventario) {
        return inventarioRepository.desequiparItem(idInventario);
    }
    
    /**
     * Aumenta la cantidad de un item en el inventario
     * @param idInventario ID del registro de inventario
     * @param cantidad Cantidad a agregar
     * @return Número de filas afectadas
     */
    public int aumentarCantidadItem(Long idInventario, Integer cantidad) {
        return inventarioRepository.aumentarCantidad(idInventario, cantidad);
    }
    
    /**
     * Verifica si un personaje tiene un item en su inventario
     * @param personajeId ID del personaje
     * @param itemId ID del item
     * @return true si el personaje tiene el item, false en caso contrario
     */
    public boolean personajeTieneItem(Long personajeId, Long itemId) {
        return inventarioRepository.tieneItem(personajeId, itemId);
    }
    
    /**
     * Cuenta el total de items en el inventario de un personaje
     * @param personajeId ID del personaje
     * @return Número total de items
     */
    public int contarItemsEnInventario(Long personajeId) {
        return inventarioRepository.contarItems(personajeId);
    }
}