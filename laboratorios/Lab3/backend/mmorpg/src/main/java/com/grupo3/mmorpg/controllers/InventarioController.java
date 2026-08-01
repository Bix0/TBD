package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Inventario;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import com.grupo3.mmorpg.services.InventarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operaciones con Inventario
 * Endpoints: /api/personajes/{id}/inventario
 */
@RestController
@RequestMapping("/api/personajes/{id}/inventario")
public class InventarioController {

    private final InventarioService inventarioService;
    private final PersonajeRepository personajeRepository;

    public InventarioController(InventarioService inventarioService,
                                PersonajeRepository personajeRepository) {
        this.inventarioService = inventarioService;
        this.personajeRepository = personajeRepository;
    }

    @PostMapping
    public ResponseEntity<Inventario> crearInventario(@PathVariable Long id, @RequestBody Inventario inventario) {
        inventario.setPersonaje(personajeRepository.getReferenceById(id));
        try {
            inventarioService.crearInventario(inventario);
            return ResponseEntity.status(HttpStatus.CREATED).body(inventario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public List<Inventario> obtenerInventarioPorPersonaje(@PathVariable Long id) {
        return inventarioService.obtenerInventarioPorPersonaje(id);
    }

    @GetMapping("/{inventarioId}")
    public ResponseEntity<Inventario> obtenerInventario(@PathVariable Long id, @PathVariable Long inventarioId) {
        return inventarioService.obtenerInventario(inventarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{inventarioId}")
    public ResponseEntity<Inventario> actualizarInventario(@PathVariable Long id, @PathVariable Long inventarioId, @RequestBody Inventario inventario) {
        inventario.setIdInventario(inventarioId);
        inventario.setPersonaje(personajeRepository.getReferenceById(id));
        try {
            inventarioService.actualizarInventario(inventario);
            return ResponseEntity.ok(inventario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{inventarioId}")
    public ResponseEntity<Void> eliminarInventario(@PathVariable Long id, @PathVariable Long inventarioId) {
        try {
            inventarioService.eliminarInventario(inventarioId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/equipados")
    public List<Inventario> obtenerItemsEquipados(@PathVariable Long id) {
        return inventarioService.obtenerItemsEquipados(id);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<Inventario> obtenerItemEnInventario(@PathVariable Long id, @PathVariable Long itemId) {
        return inventarioService.obtenerItemEnInventario(id, itemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{inventarioId}/equipar")
    public ResponseEntity<Void> equiparItem(@PathVariable Long id, @PathVariable Long inventarioId) {
        try {
            inventarioService.equiparItem(inventarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{inventarioId}/desequipar")
    public ResponseEntity<Void> desequiparItem(@PathVariable Long id, @PathVariable Long inventarioId) {
        try {
            inventarioService.desequiparItem(inventarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{inventarioId}/cantidad")
    public ResponseEntity<Void> aumentarCantidadItem(@PathVariable Long id, @PathVariable Long inventarioId, @RequestParam Integer cantidad) {
        try {
            inventarioService.aumentarCantidadItem(inventarioId, cantidad);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/verificar/{itemId}")
    public ResponseEntity<Boolean> personajeTieneItem(@PathVariable Long id, @PathVariable Long itemId) {
        return ResponseEntity.ok(inventarioService.personajeTieneItem(id, itemId));
    }

    @GetMapping("/cantidad")
    public ResponseEntity<Integer> contarItemsEnInventario(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.contarItemsEnInventario(id));
    }
}
