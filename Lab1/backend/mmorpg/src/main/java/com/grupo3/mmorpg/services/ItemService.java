package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Item;
import com.grupo3.mmorpg.models.ItemClasePermitida;
import com.grupo3.mmorpg.repositories.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de negocio relacionadas con Items
 * Utiliza ItemRepository para acceder a los datos
 */
@Service
public class ItemService {
    
    private final ItemRepository itemRepository;
    
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }
    
    // CRUD PARA ITEM

    /**
     * Crea un nuevo item
     * @param item Objeto Item con los datos a guardar
     * @return Número de filas afectadas (1 si se creó correctamente)
     */
    public int crearItem(Item item) {
        return itemRepository.create(item);
    }
    
    /**
     * Obtiene un item por su ID
     * @param id ID del item
     * @return Optional con el item si existe
     */
    public Optional<Item> obtenerItem(Long id) {
        return itemRepository.findById(id);
    }
    
    /**
     * Obtiene todos los items
     * @return Lista de todos los items
     */
    public List<Item> obtenerTodosLosItems() {
        return itemRepository.findAll();
    }
    
    /**
     * Actualiza un item existente
     * @param item Objeto Item con los datos actualizados
     * @return Número de filas afectadas
     */
    public int actualizarItem(Item item) {
        if (!itemRepository.findById(item.getId_item()).isPresent()) {
            throw new IllegalArgumentException("Item no encontrado");
        }
        return itemRepository.update(item);
    }
    
    /**
     * Elimina un item por su ID
     * @param id ID del item a eliminar
     * @return Número de filas afectadas
     */
    public int eliminarItem(Long id) {
        return itemRepository.deleteById(id);
    }

    // METODOS ESPECIFICOS PARA ITEM

    /**
     * Busca un item por nombre
     * @param nombre Nombre del item
     * @return Optional con el item si existe
     */
    public Optional<Item> buscarPorNombre(String nombre) {
        return itemRepository.findByName(nombre);
    }
    
    /**
     * Obtiene items con item_lvl mínimo
     * @param itemLevel Item level mínimo
     * @return Lista de items que cumplen el requisito
     */
    public List<Item> obtenerPorItemLevelMin(Integer itemLevel) {
        return itemRepository.findByItemLevelMin(itemLevel);
    }

    // METODOS PARA ITEM_CLASE_PERMITIDA (falta implementar)

    /**
     * Agrega una clase permitida para un item
     * @param idItem ID del item
     * @param clase Clase a agregar
     * @return Número de filas afectadas
     */
    public int agregarClasePermitida(Long idItem, String clase) {
        return itemRepository.agregarClasePermitida(idItem, clase);
    }
    
    /**
     * Elimina una clase permitida para un item
     * @param idItem ID del item
     * @param clase Clase a eliminar
     * @return Número de filas afectadas
     */
    public int eliminarClasePermitida(Long idItem, String clase) {
        return itemRepository.eliminarClasePermitida(idItem, clase);
    }
    
    /**
     * Obtiene todas las clases permitidas para un item
     * @param idItem ID del item
     * @return Lista de clases permitidas
     */
    public List<String> obtenerClasesPermitidas(Long idItem) {
        return itemRepository.getClasesPermitidas(idItem);
    }
    
    /**
     * Verifica si una clase está permitida para un item
     * @param idItem ID del item
     * @param clase Clase a verificar
     * @return true si la clase está permitida, false en caso contrario
     */
    public boolean esClasePermitida(Long idItem, String clase) {
        return itemRepository.isClasePermitida(idItem, clase);
    }
    
    /**
     * Obtiene todas las clases permitidas para un item (con objeto completo)
     * @param idItem ID del item
     * @return Lista de objetos ItemClasePermitida
     */
    public List<ItemClasePermitida> obtenerClasesPermitidasCompleto(Long idItem) {
        return itemRepository.getClasesPermitidasCompleto(idItem);
    }
}