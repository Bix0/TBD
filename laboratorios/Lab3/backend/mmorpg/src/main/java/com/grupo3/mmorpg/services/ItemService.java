package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Item;
import com.grupo3.mmorpg.repositories.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de negocio relacionadas con Items en MongoDB
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional
    public Item crearItem(Item item) {
        if (item.getClasesPermitidas() == null) {
            item.setClasesPermitidas(new ArrayList<>());
        }
        return itemRepository.save(item);
    }

    public Optional<Item> obtenerItem(String id) {
        return itemRepository.findById(id);
    }

    public List<Item> obtenerTodosLosItems() {
        return itemRepository.findAll();
    }

    @Transactional
    public Item actualizarItem(Item item) {
        if (!itemRepository.existsById(item.getIdItem())) {
            throw new IllegalArgumentException("Item no encontrado");
        }
        return itemRepository.save(item);
    }

    @Transactional
    public void eliminarItem(String id) {
        itemRepository.deleteById(id);
    }

    public Optional<Item> buscarPorNombre(String nombre) {
        return itemRepository.findByNombre(nombre);
    }

    public List<Item> obtenerPorItemLevelMin(Integer itemLevel) {
        return itemRepository.findByItemLevelMin(itemLevel);
    }

    // === GESTIÓN DE CLASES PERMITIDAS (EMBEBIDAS) ===

    @Transactional
    public Item agregarClasePermitida(String idItem, String clase) {
        Item item = itemRepository.findById(idItem)
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));

        if (item.getClasesPermitidas() == null) {
            item.setClasesPermitidas(new ArrayList<>());
        }

        if (!item.getClasesPermitidas().contains(clase)) {
            item.getClasesPermitidas().add(clase);
            itemRepository.save(item);
        }
        return item;
    }

    @Transactional
    public Item eliminarClasePermitida(String idItem, String clase) {
        Item item = itemRepository.findById(idItem)
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));

        if (item.getClasesPermitidas() != null) {
            item.getClasesPermitidas().remove(clase);
            itemRepository.save(item);
        }
        return item;
    }

    public List<String> obtenerClasesPermitidas(String idItem) {
        Item item = itemRepository.findById(idItem)
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));
        return item.getClasesPermitidas() != null ? item.getClasesPermitidas() : new ArrayList<>();
    }

    public boolean esClasePermitida(String idItem, String clase) {
        Item item = itemRepository.findById(idItem).orElse(null);
        return item != null && item.getClasesPermitidas() != null && item.getClasesPermitidas().contains(clase);
    }
}