package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Item;
import com.grupo3.mmorpg.models.ItemClasePermitida;
import com.grupo3.mmorpg.services.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operaciones con Items
 * Endpoints: /api/items
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

     //CRUD BASICOS PARA ITEM
    /**
     * Crea un nuevo item
     * POST /api/items
     * @param item Objeto Item con los datos
     * @return ResponseEntity con el item creado
     */
    @PostMapping
    public ResponseEntity<Item> crearItem(@RequestBody Item item) {
        try {
            itemService.crearItem(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(item);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Obtiene todos los items
     * GET /api/items
     * @return Lista de items
     */
    @GetMapping
    public List<Item> obtenerTodosLosItems() {
        return itemService.obtenerTodosLosItems();
    }

    /**
     * Obtiene un item por su ID
     * GET /api/items/{id}
     * @param id ID del item
     * @return ResponseEntity con el item o 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> obtenerItem(@PathVariable Long id) {
        return itemService.obtenerItem(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza un item existente
     * PUT /api/items/{id}
     * @param id ID del item
     * @param item Objeto Item con los datos actualizados
     * @return ResponseEntity con el item actualizado o 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> actualizarItem(@PathVariable Long id, @RequestBody Item item) {
        item.setIdItem(id);
        try {
            itemService.actualizarItem(item);
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un item por su ID
     * DELETE /api/items/{id}
     * @param id ID del item
     * @return ResponseEntity con status 204 o 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long id) {
        try {
            itemService.eliminarItem(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================================================
    // MÉTODOS ESPECÍFICOS PARA ITEM
    // ============================================================================

    /**
     * Busca un item por nombre
     * GET /api/items/nombre/{nombre}
     * @param nombre Nombre del item
     * @return ResponseEntity con el item o 404
     */
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Item> buscarPorNombre(@PathVariable String nombre) {
        return itemService.buscarPorNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene items con item_lvl mínimo
     * GET /api/items/itemlevel/{itemLevel}
     * @param itemLevel Item level mínimo
     * @return Lista de items que cumplen el requisito
     */
    @GetMapping("/itemlevel/{itemLevel}")
    public List<Item> obtenerPorItemLevelMin(@PathVariable Integer itemLevel) {
        return itemService.obtenerPorItemLevelMin(itemLevel);
    }

    // MÉTODOS PARA ITEM_CLASE_PERMITIDA (falta implmentar filtro)
    /**
     * Agrega una clase permitida para un item
     * POST /api/items/{id}/clases-permitidas
     * @param id ID del item
     * @param clase Clase a agregar
     * @return ResponseEntity con status 201 o 404
     */
    @PostMapping("/{id}/clases-permitidas")
    public ResponseEntity<Void> agregarClasePermitida(@PathVariable Long id, @RequestParam String clase) {
        try {
            itemService.agregarClasePermitida(id, clase);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina una clase permitida para un item
     * DELETE /api/items/{id}/clases-permitidas/{clase}
     * @param id ID del item
     * @param clase Clase a eliminar
     * @return ResponseEntity con status 204 o 404
     */
    @DeleteMapping("/{id}/clases-permitidas/{clase}")
    public ResponseEntity<Void> eliminarClasePermitida(@PathVariable Long id, @PathVariable String clase) {
        try {
            itemService.eliminarClasePermitida(id, clase);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene todas las clases permitidas para un item
     * GET /api/items/{id}/clases-permitidas
     * @param id ID del item
     * @return Lista de clases permitidas
     */
    @GetMapping("/{id}/clases-permitidas")
    public List<String> obtenerClasesPermitidas(@PathVariable Long id) {
        return itemService.obtenerClasesPermitidas(id);
    }

    /**
     * Verifica si una clase está permitida para un item
     * GET /api/items/{id}/clases-permitidas/verificar/{clase}
     * @param id ID del item
     * @param clase Clase a verificar
     * @return true si la clase está permitida, false en caso contrario
     */
    @GetMapping("/{id}/clases-permitidas/verificar/{clase}")
    public ResponseEntity<Boolean> esClasePermitida(@PathVariable Long id, @PathVariable String clase) {
        return ResponseEntity.ok(itemService.esClasePermitida(id, clase));
    }

    /**
     * Obtiene todas las clases permitidas para un item (con objeto completo)
     * GET /api/items/{id}/clases-permitidas/completo
     * @param id ID del item
     * @return Lista de objetos ItemClasePermitida
     */
    @GetMapping("/{id}/clases-permitidas/completo")
    public List<ItemClasePermitida> obtenerClasesPermitidasCompleto(@PathVariable Long id) {
        return itemService.obtenerClasesPermitidasCompleto(id);
    }
}
