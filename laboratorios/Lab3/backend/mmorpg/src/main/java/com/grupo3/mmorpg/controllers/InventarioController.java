package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Inventario;
import com.grupo3.mmorpg.services.InventarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operaciones con Inventario en MongoDB
 * Endpoints: /api/personajes/{id}/inventario
 */
@RestController
@RequestMapping("/api/personajes/{id}/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @PostMapping
    public ResponseEntity<Inventario> crearInventario(@PathVariable String id, @RequestBody Inventario inventario) {
        try {
            inventario.setPersonajeId(id);
            inventarioService.crearInventario(inventario);
            return ResponseEntity.status(HttpStatus.CREATED).body(inventario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public List<Inventario> obtenerInventarioPorPersonaje(@PathVariable String id) {
        return inventarioService.obtenerInventarioPorPersonaje(id);
    }

    @GetMapping("/{inventarioId}")
    public ResponseEntity<Inventario> obtenerInventario(@PathVariable String id, @PathVariable String inventarioId) {
        return inventarioService.obtenerInventario(inventarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{inventarioId}")
    public ResponseEntity<Inventario> actualizarInventario(@PathVariable String id, @PathVariable String inventarioId, @RequestBody Inventario inventario) {
        inventario.setIdInventario(inventarioId);
        inventario.setPersonajeId(id);
        try {
            inventarioService.actualizarInventario(inventario);
            return ResponseEntity.ok(inventario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{inventarioId}")
    public ResponseEntity<Void> eliminarInventario(@PathVariable String id, @PathVariable String inventarioId) {
        try {
            inventarioService.eliminarInventario(inventarioId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/equipados")
    public List<Inventario> obtenerItemsEquipados(@PathVariable String id) {
        return inventarioService.obtenerItemsEquipados(id);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<Inventario> obtenerItemEnInventario(@PathVariable String id, @PathVariable String itemId) {
        return inventarioService.obtenerItemEnInventario(id, itemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{inventarioId}/equipar")
    public ResponseEntity<Void> equiparItem(@PathVariable String id, @PathVariable String inventarioId) {
        try {
            inventarioService.equiparItem(inventarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{inventarioId}/desequipar")
    public ResponseEntity<Void> desequiparItem(@PathVariable String id, @PathVariable String inventarioId) {
        try {
            inventarioService.desequiparItem(inventarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{inventarioId}/cantidad")
    public ResponseEntity<Void> aumentarCantidadItem(@PathVariable String id, @PathVariable String inventarioId, @RequestParam Integer cantidad) {
        try {
            inventarioService.aumentarCantidadItem(inventarioId, cantidad);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/verificar/{itemId}")
    public ResponseEntity<Boolean> personajeTieneItem(@PathVariable String id, @PathVariable String itemId) {
        return ResponseEntity.ok(inventarioService.personajeTieneItem(id, itemId));
    }

    @GetMapping("/cantidad")
    public ResponseEntity<Integer> contarItemsEnInventario(@PathVariable String id) {
        return ResponseEntity.ok(inventarioService.contarItemsEnInventario(id));
    }
}