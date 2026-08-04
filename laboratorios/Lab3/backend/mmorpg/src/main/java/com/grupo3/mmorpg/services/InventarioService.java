package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Inventario;
import com.grupo3.mmorpg.repositories.InventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de negocio relacionadas con Inventario en MongoDB
 */
@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;

    public InventarioService(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    @Transactional
    public Inventario crearInventario(Inventario inventario) {
        return inventarioRepository.save(inventario);
    }

    public Optional<Inventario> obtenerInventario(String id) {
        return inventarioRepository.findById(id);
    }

    public List<Inventario> obtenerTodosLosInventarios() {
        return inventarioRepository.findAll();
    }

    @Transactional
    public Inventario actualizarInventario(Inventario inventario) {
        if (!inventarioRepository.existsById(inventario.getIdInventario())) {
            throw new IllegalArgumentException("Registro de inventario no encontrado");
        }
        return inventarioRepository.save(inventario);
    }

    @Transactional
    public void eliminarInventario(String id) {
        inventarioRepository.deleteById(id);
    }

    // MÉTODOS ESPECÍFICOS
    public List<Inventario> obtenerInventarioPorPersonaje(String personajeId) {
        return inventarioRepository.findByPersonajeId(personajeId);
    }

    public List<Inventario> obtenerItemsEquipados(String personajeId) {
        return inventarioRepository.findByPersonajeIdAndEquipadoTrue(personajeId);
    }

    public Optional<Inventario> obtenerItemEnInventario(String personajeId, String itemId) {
        return inventarioRepository.findByPersonajeIdAndItemId(personajeId, itemId);
    }

    @Transactional
    public Inventario equiparItem(String idInventario) {
        Inventario inventario = inventarioRepository.findById(idInventario)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado"));
        inventario.setEquipado(true);
        return inventarioRepository.save(inventario);
    }

    @Transactional
    public Inventario desequiparItem(String idInventario) {
        Inventario inventario = inventarioRepository.findById(idInventario)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado"));
        inventario.setEquipado(false);
        return inventarioRepository.save(inventario);
    }

    @Transactional
    public Inventario aumentarCantidadItem(String idInventario, Integer cantidad) {
        Inventario inventario = inventarioRepository.findById(idInventario)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado"));
        inventario.setCantidad(inventario.getCantidad() + (cantidad != null ? cantidad : 1));
        return inventarioRepository.save(inventario);
    }

    public boolean personajeTieneItem(String personajeId, String itemId) {
        return inventarioRepository.existsByPersonajeIdAndItemId(personajeId, itemId);
    }

    public int contarItemsEnInventario(String personajeId) {
        return (int) inventarioRepository.countByPersonajeId(personajeId);
    }
}