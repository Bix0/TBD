package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Inventario;
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
    
    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    //CRUD BASICOS
    /**
     * Crea un nuevo registro de inventario
     * POST /api/personajes/{id}/inventario
     * @param id ID del personaje
     * @param inventario Objeto Inventario con los datos
     * @return ResponseEntity con el registro creado
     */
    @PostMapping
    public ResponseEntity<Inventario> crearInventario(@PathVariable Long id, @RequestBody Inventario inventario) {
        inventario.setId_personaje(id);
        try {
            inventarioService.crearInventario(inventario);
            return ResponseEntity.status(HttpStatus.CREATED).body(inventario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Obtiene todos los items del inventario de un personaje
     * GET /api/personajes/{id}/inventario
     * @param id ID del personaje
     * @return Lista de items del inventario
     */
    @GetMapping
    public List<Inventario> obtenerInventarioPorPersonaje(@PathVariable Long id) {
        return inventarioService.obtenerInventarioPorPersonaje(id);
    }
    
    /**
     * Obtiene un registro de inventario por su ID
     * GET /api/personajes/{id}/inventario/{inventarioId}
     * @param id ID del personaje
     * @param inventarioId ID del registro
     * @return ResponseEntity con el registro o 404
     */
    @GetMapping("/{inventarioId}")
    public ResponseEntity<Inventario> obtenerInventario(@PathVariable Long id, @PathVariable Long inventarioId) {
        return inventarioService.obtenerInventario(inventarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Actualiza un registro de inventario existente
     * PUT /api/personajes/{id}/inventario/{inventarioId}
     * @param id ID del personaje
     * @param inventarioId ID del registro
     * @param inventario Objeto Inventario con los datos actualizados
     * @return ResponseEntity con el registro actualizado o 404
     */
    @PutMapping("/{inventarioId}")
    public ResponseEntity<Inventario> actualizarInventario(@PathVariable Long id, @PathVariable Long inventarioId, @RequestBody Inventario inventario) {
        inventario.setId_inventario(inventarioId);
        inventario.setId_personaje(id);
        try {
            inventarioService.actualizarInventario(inventario);
            return ResponseEntity.ok(inventario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Elimina un registro de inventario por su ID
     * DELETE /api/personajes/{id}/inventario/{inventarioId}
     * @param id ID del personaje
     * @param inventarioId ID del registro
     * @return ResponseEntity con status 204 o 404
     */
    @DeleteMapping("/{inventarioId}")
    public ResponseEntity<Void> eliminarInventario(@PathVariable Long id, @PathVariable Long inventarioId) {
        try {
            inventarioService.eliminarInventario(inventarioId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    //METODOS ESPECIFICOS
    /**
     * Obtiene los items equipados de un personaje
     * GET /api/personajes/{id}/inventario/equipados
     * @param id ID del personaje
     * @return Lista de items equipados
     */
    @GetMapping("/equipados")
    public List<Inventario> obtenerItemsEquipados(@PathVariable Long id) {
        return inventarioService.obtenerItemsEquipados(id);
    }
    
    /**
     * Obtiene un item específico en el inventario de un personaje
     * GET /api/personajes/{id}/inventario/item/{itemId}
     * @param id ID del personaje
     * @param itemId ID del item
     * @return ResponseEntity con el registro o 404
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<Inventario> obtenerItemEnInventario(@PathVariable Long id, @PathVariable Long itemId) {
        return inventarioService.obtenerItemEnInventario(id, itemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Equipa un item en el inventario
     * PUT /api/personajes/{id}/inventario/{inventarioId}/equipar
     * @param id ID del personaje
     * @param inventarioId ID del registro de inventario
     * @return ResponseEntity con status 200 o 404
     */
    @PutMapping("/{inventarioId}/equipar")
    public ResponseEntity<Void> equiparItem(@PathVariable Long id, @PathVariable Long inventarioId) {
        try {
            inventarioService.equiparItem(inventarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Desequipa un item en el inventario
     * PUT /api/personajes/{id}/inventario/{inventarioId}/desequipar
     * @param id ID del personaje
     * @param inventarioId ID del registro de inventario
     * @return ResponseEntity con status 200 o 404
     */
    @PutMapping("/{inventarioId}/desequipar")
    public ResponseEntity<Void> desequiparItem(@PathVariable Long id, @PathVariable Long inventarioId) {
        try {
            inventarioService.desequiparItem(inventarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Aumenta la cantidad de un item en el inventario
     * PUT /api/personajes/{id}/inventario/{inventarioId}/cantidad
     * @param id ID del personaje
     * @param inventarioId ID del registro de inventario
     * @param cantidad Cantidad a agregar
     * @return ResponseEntity con status 200 o 404
     */
    @PutMapping("/{inventarioId}/cantidad")
    public ResponseEntity<Void> aumentarCantidadItem(@PathVariable Long id, @PathVariable Long inventarioId, @RequestParam Integer cantidad) {
        try {
            inventarioService.aumentarCantidadItem(inventarioId, cantidad);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Verifica si un personaje tiene un item en su inventario
     * GET /api/personajes/{id}/inventario/verificar/{itemId}
     * @param id ID del personaje
     * @param itemId ID del item
     * @return true si el personaje tiene el item, false en caso contrario
     */
    @GetMapping("/verificar/{itemId}")
    public ResponseEntity<Boolean> personajeTieneItem(@PathVariable Long id, @PathVariable Long itemId) {
        return ResponseEntity.ok(inventarioService.personajeTieneItem(id, itemId));
    }
    
    /**
     * Cuenta el total de items en el inventario de un personaje
     * GET /api/personajes/{id}/inventario/cantidad
     * @param id ID del personaje
     * @return Número total de items
     */
    @GetMapping("/cantidad")
    public ResponseEntity<Integer> contarItemsEnInventario(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.contarItemsEnInventario(id));
    }
}