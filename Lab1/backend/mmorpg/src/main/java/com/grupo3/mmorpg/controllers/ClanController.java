package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Clan;
import com.grupo3.mmorpg.services.ClanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operaciones con Clanes
 * Endpoints: /api/clanes
 */
@RestController
@RequestMapping("/api/clanes")
public class ClanController {
    
    private final ClanService clanService;
    
    public ClanController(ClanService clanService) {
        this.clanService = clanService;
    }
    
    // ============================================================================
    // CRUD BÁSICOS
    // ============================================================================
    
    /**
     * Crea un nuevo clan
     * POST /api/clanes
     * @param clan Objeto Clan con los datos
     * @return ResponseEntity con el clan creado
     */
    @PostMapping
    public ResponseEntity<Clan> crearClan(@RequestBody Clan clan) {
        try {
            clanService.crearClan(clan);
            return ResponseEntity.status(HttpStatus.CREATED).body(clan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Obtiene todos los clanes
     * GET /api/clanes
     * @return Lista de clanes
     */
    @GetMapping
    public List<Clan> obtenerTodosLosClanes() {
        return clanService.obtenerTodosLosClanes();
    }
    
    /**
     * Obtiene un clan por su ID
     * GET /api/clanes/{id}
     * @param id ID del clan
     * @return ResponseEntity con el clan o 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Clan> obtenerClan(@PathVariable Long id) {
        return clanService.obtenerClan(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Actualiza un clan existente
     * PUT /api/clanes/{id}
     * @param id ID del clan
     * @param clan Objeto Clan con los datos actualizados
     * @return ResponseEntity con el clan actualizado o 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<Clan> actualizarClan(@PathVariable Long id, @RequestBody Clan clan) {
        clan.setId_clan(id);
        try {
            clanService.actualizarClan(clan);
            return ResponseEntity.ok(clan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Cambia el líder de un clan
     * PUT /api/clanes/{id}/lider
     * @param id ID del clan
     * @param nuevoLider ID del nuevo líder
     * @return ResponseEntity con status 200 o 404
     * Este endpoint activa el trigger trg_auditar_lider
     */
    @PutMapping("/{id}/lider")
    public ResponseEntity<Void> cambiarLider(@PathVariable Long id, @RequestParam Long nuevoLider) {
        try {
            clanService.cambiarLider(id, nuevoLider);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Elimina un clan por su ID
     * DELETE /api/clanes/{id}
     * @param id ID del clan
     * @return ResponseEntity con status 204 o 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarClan(@PathVariable Long id) {
        try {
            clanService.eliminarClan(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // ============================================================================
    // MÉTODOS ESPECÍFICOS
    // ============================================================================
    
    /**
     * Busca un clan por nombre
     * GET /api/clanes/nombre/{nombre}
     * @param nombre Nombre del clan
     * @return ResponseEntity con el clan o 404
     */
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Clan> buscarPorNombre(@PathVariable String nombre) {
        return clanService.buscarPorNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Verifica si un nombre de clan ya existe
     * GET /api/clanes/exists/{nombre}
     * @param nombre Nombre del clan
     * @return true si existe, false en caso contrario
     */
    @GetMapping("/exists/{nombre}")
    public ResponseEntity<Boolean> existeNombreClan(@PathVariable String nombre) {
        return ResponseEntity.ok(clanService.existeNombreClan(nombre));
    }
    
    /**
     * Obtiene el ID del líder de un clan
     * GET /api/clanes/{id}/lider-id
     * @param id ID del clan
     * @return ResponseEntity con el ID del líder o 404
     */
    @GetMapping("/{id}/lider-id")
    public ResponseEntity<Long> obtenerLiderId(@PathVariable Long id) {
        return clanService.obtenerLiderId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Une a un personaje al clan
     * POST /api/clanes/{id}/miembros?idPersonaje={idPersonaje}
     */
    @PostMapping("/{id}/miembros")
    public ResponseEntity<String> unirseAClan(@PathVariable Long id, @RequestParam Long idPersonaje) {
        String resultado = clanService.unirseAClan(id, idPersonaje);
        if (resultado.contains("ya pertenece") || resultado.contains("no existe")) {
            return ResponseEntity.badRequest().body(resultado);
        }
        return ResponseEntity.ok(resultado);
    }

    /**
     * Expulsa o permite que un miembro salga del clan
     * DELETE /api/clanes/{id}/miembros/{idPersonaje}
     */
    @DeleteMapping("/{id}/miembros/{idPersonaje}")
    public ResponseEntity<String> abandonarClan(@PathVariable Long id, @PathVariable Long idPersonaje) {
        String resultado = clanService.abandonarClan(id, idPersonaje);
        if (resultado.startsWith("Error")) {
            return ResponseEntity.badRequest().body(resultado);
        }
        return ResponseEntity.ok(resultado);
    }
}