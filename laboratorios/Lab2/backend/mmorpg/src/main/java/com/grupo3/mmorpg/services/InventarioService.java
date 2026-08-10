package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Inventario;
import com.grupo3.mmorpg.repositories.InventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de negocio relacionadas con Inventario
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

    public Optional<Inventario> obtenerInventario(Long id) {
        return inventarioRepository.findById(id);
    }

    public List<Inventario> obtenerTodosLosInventarios() {
        return inventarioRepository.findAll();
    }

    @Transactional
    public Inventario actualizarInventario(Inventario inventario) {
        if (!inventarioRepository.findById(inventario.getIdInventario()).isPresent()) {
            throw new IllegalArgumentException("Registro de inventario no encontrado");
        }
        return inventarioRepository.save(inventario);
    }

    @Transactional
    public void eliminarInventario(Long id) {
        inventarioRepository.deleteById(id);
    }

    // METODOS ESPECIFICOS
    public List<Inventario> obtenerInventarioPorPersonaje(Long personajeId) {
        return inventarioRepository.findByPersonajeIdPersonaje(personajeId);
    }

    public List<Inventario> obtenerItemsEquipados(Long personajeId) {
        return inventarioRepository.findByPersonajeIdPersonajeAndEquipadoTrue(personajeId);
    }

    public Optional<Inventario> obtenerItemEnInventario(Long personajeId, Long itemId) {
        return inventarioRepository.findByPersonajeIdPersonajeAndItemIdItem(personajeId, itemId);
    }

    @Transactional
    public int equiparItem(Long idInventario) {
        return inventarioRepository.equiparItem(idInventario);
    }

    @Transactional
    public int desequiparItem(Long idInventario) {
        return inventarioRepository.desequiparItem(idInventario);
    }

    @Transactional
    public int aumentarCantidadItem(Long idInventario, Integer cantidad) {
        return inventarioRepository.aumentarCantidad(idInventario, cantidad);
    }

    public boolean personajeTieneItem(Long personajeId, Long itemId) {
        return inventarioRepository.tieneItem(personajeId, itemId);
    }

    public int contarItemsEnInventario(Long personajeId) {
        return inventarioRepository.contarItems(personajeId);
    }
}
