package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Item;
import com.grupo3.mmorpg.services.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operaciones con Items (MongoDB)
 * Endpoints: /api/items
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // ==========================================
    // CRUD BÁSICOS PARA ITEM
    // ==========================================

    @PostMapping
    public ResponseEntity<Item> crearItem(@RequestBody Item item) {
        try {
            itemService.crearItem(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(item);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public List<Item> obtenerTodosLosItems() {
        return itemService.obtenerTodosLosItems();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> obtenerItem(@PathVariable String id) {
        return itemService.obtenerItem(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> actualizarItem(@PathVariable String id, @RequestBody Item item) {
        item.setIdItem(id);
        try {
            itemService.actualizarItem(item);
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItem(@PathVariable String id) {
        try {
            itemService.eliminarItem(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==========================================
    // MÉTODOS ESPECÍFICOS PARA ITEM
    // ==========================================

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Item> buscarPorNombre(@PathVariable String nombre) {
        return itemService.buscarPorNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/itemlevel/{itemLevel}")
    public List<Item> obtenerPorItemLevelMin(@PathVariable Integer itemLevel) {
        return itemService.obtenerPorItemLevelMin(itemLevel);
    }

    // ==========================================
    // MÉTODOS PARA CLASES PERMITIDAS (EMBEBIDAS)
    // ==========================================

    @PostMapping("/{id}/clases-permitidas")
    public ResponseEntity<Item> agregarClasePermitida(@PathVariable String id, @RequestParam String clase) {
        try {
            Item itemActualizado = itemService.agregarClasePermitida(id, clase);
            return ResponseEntity.status(HttpStatus.CREATED).body(itemActualizado);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/clases-permitidas/{clase}")
    public ResponseEntity<Item> eliminarClasePermitida(@PathVariable String id, @PathVariable String clase) {
        try {
            Item itemActualizado = itemService.eliminarClasePermitida(id, clase);
            return ResponseEntity.ok(itemActualizado);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/clases-permitidas")
    public ResponseEntity<List<String>> obtenerClasesPermitidas(@PathVariable String id) {
        try {
            List<String> clases = itemService.obtenerClasesPermitidas(id);
            return ResponseEntity.ok(clases);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/clases-permitidas/verificar/{clase}")
    public ResponseEntity<Boolean> esClasePermitida(@PathVariable String id, @PathVariable String clase) {
        return ResponseEntity.ok(itemService.esClasePermitida(id, clase));
    }
}