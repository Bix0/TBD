package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Item;
import com.grupo3.mmorpg.models.ItemClasePermitida;
import com.grupo3.mmorpg.models.ItemClasePermitida.ItemClasePermitidaId;
import com.grupo3.mmorpg.repositories.ItemClasePermitidaRepository;
import com.grupo3.mmorpg.repositories.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de negocio relacionadas con Items
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemClasePermitidaRepository itemClasePermitidaRepository;

    public ItemService(ItemRepository itemRepository,
                       ItemClasePermitidaRepository itemClasePermitidaRepository) {
        this.itemRepository = itemRepository;
        this.itemClasePermitidaRepository = itemClasePermitidaRepository;
    }

    @Transactional
    public Item crearItem(Item item) {
        return itemRepository.save(item);
    }

    public Optional<Item> obtenerItem(Long id) {
        return itemRepository.findById(id);
    }

    public List<Item> obtenerTodosLosItems() {
        return itemRepository.findAll();
    }

    @Transactional
    public Item actualizarItem(Item item) {
        if (!itemRepository.findById(item.getIdItem()).isPresent()) {
            throw new IllegalArgumentException("Item no encontrado");
        }
        return itemRepository.save(item);
    }

    @Transactional
    public void eliminarItem(Long id) {
        itemRepository.deleteById(id);
    }

    public Optional<Item> buscarPorNombre(String nombre) {
        return itemRepository.findByNombre(nombre);
    }

    public List<Item> obtenerPorItemLevelMin(Integer itemLevel) {
        return itemRepository.findByItemLevelMin(itemLevel);
    }

    // === ITEM_CLASE_PERMITIDA ===

    @Transactional
    public ItemClasePermitida agregarClasePermitida(Long idItem, String clase) {
        ItemClasePermitidaId id = new ItemClasePermitidaId(idItem, clase);
        Item item = itemRepository.findById(idItem)
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));
        ItemClasePermitida icp = new ItemClasePermitida(id, item);
        return itemClasePermitidaRepository.save(icp);
    }

    @Transactional
    public void eliminarClasePermitida(Long idItem, String clase) {
        itemClasePermitidaRepository.deleteByItemIdItemAndIdClasePermitida(idItem, clase);
    }

    public List<String> obtenerClasesPermitidas(Long idItem) {
        return itemClasePermitidaRepository.findClasesPermitidasByItemId(idItem);
    }

    public boolean esClasePermitida(Long idItem, String clase) {
        return itemClasePermitidaRepository.existsByItemIdAndClasePermitida(idItem, clase);
    }

    public List<ItemClasePermitida> obtenerClasesPermitidasCompleto(Long idItem) {
        return itemClasePermitidaRepository.findByItemIdItem(idItem);
    }
}
