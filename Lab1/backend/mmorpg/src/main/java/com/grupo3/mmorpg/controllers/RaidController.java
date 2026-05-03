package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Raid;
import com.grupo3.mmorpg.services.RaidService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller REST para operaciones con Raids
 * Endpoints: /api/raids
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/raids")
public class RaidController {

    private final RaidService raidService;

    public RaidController(RaidService raidService) {
        this.raidService = raidService;
    }

    // ============================================================================
    // CRUD BÁSICOS
    // ============================================================================

    /**
     * Crea una nueva raid
     * POST /api/raids
     * 
     * @param raid Objeto Raid con los datos
     * @return ResponseEntity con la raid creada
     */
    @PostMapping
    public ResponseEntity<Raid> crearRaid(@RequestBody Raid raid) {
        try {
            raidService.crearRaid(raid);
            return ResponseEntity.status(HttpStatus.CREATED).body(raid);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Crea una raid con inscripción masiva automática (usa SP)
     * POST /api/raids/con-inscripcion-masiva
     * 
     * @param nombre    Nombre de la raid
     * @param fecha     Fecha de la raid
     * @param itemLevel Item level requerido
     * @param tanques   Cantidad de cupos para tanques
     * @param heals     Cantidad de cupos para heals
     * @param dps       Cantidad de cupos para DPS
     * @return ResponseEntity con el ID de la raid creada
     */
    @PostMapping("/con-inscripcion-masiva")
    public ResponseEntity<Long> crearRaidConInscripcionMasiva(
            @RequestParam String nombre,
            @RequestParam LocalDateTime fecha,
            @RequestParam Integer itemLevel,
            @RequestParam Integer tanques,
            @RequestParam Integer heals,
            @RequestParam Integer dps) {
        Long idRaid = raidService.crearRaidConInscripcionMasiva(nombre, fecha, itemLevel, tanques, heals, dps);
        return ResponseEntity.status(HttpStatus.CREATED).body(idRaid);
    }

    /**
     * Obtiene todas las raids
     * GET /api/raids
     * 
     * @return Lista de raids
     */
    @GetMapping
    public List<Raid> obtenerTodasLasRaids() {
        return raidService.obtenerTodasLasRaids();
    }

    /**
     * Obtiene una raid por su ID
     * GET /api/raids/{id}
     * 
     * @param id ID de la raid
     * @return ResponseEntity con la raid o 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Raid> obtenerRaid(@PathVariable Long id) {
        return raidService.obtenerRaid(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza una raid existente
     * PUT /api/raids/{id}
     * 
     * @param id   ID de la raid
     * @param raid Objeto Raid con los datos actualizados
     * @return ResponseEntity con la raid actualizada o 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<Raid> actualizarRaid(@PathVariable Long id, @RequestBody Raid raid) {
        raid.setId_raid(id);
        try {
            raidService.actualizarRaid(raid);
            return ResponseEntity.ok(raid);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cambia el estado de una raid
     * PUT /api/raids/{id}/estado
     * 
     * @param id     ID de la raid
     * @param estado Nuevo estado
     * @return ResponseEntity con status 200 o 404
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstadoRaid(@PathVariable Long id, @RequestParam String estado) {
        try {
            raidService.cambiarEstadoRaid(id, estado);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina una raid por su ID
     * DELETE /api/raids/{id}
     * 
     * @param id ID de la raid
     * @return ResponseEntity con status 204 o 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRaid(@PathVariable Long id) {
        try {
            raidService.eliminarRaid(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================================================
    // MÉTODOS ESPECÍFICOS
    // ============================================================================

    /**
     * Obtiene raids por estado
     * GET /api/raids/estado/{estado}
     * 
     * @param estado Estado de la raid
     * @return Lista de raids con ese estado
     */
    @GetMapping("/estado/{estado}")
    public List<Raid> obtenerPorEstado(@PathVariable String estado) {
        return raidService.obtenerPorEstado(estado);
    }

    /**
     * Obtiene raids programadas
     * GET /api/raids/programadas
     * 
     * @return Lista de raids programadas
     */
    @GetMapping("/programadas")
    public List<Raid> obtenerRaidsProgramadas() {
        return raidService.obtenerRaidsProgramadas();
    }

    /**
     * Inscribe un personaje a una raid
     * POST /api/raids/{id}/inscribir
     * 
     * @param id          ID de la raid
     * @param idPersonaje ID del personaje
     * @return ResponseEntity con status 200 o 404
     *         Este endpoint activa el trigger trg_validar_ilvl
     */
    @PostMapping("/{id}/inscribir")
    public ResponseEntity<String> inscribirPersonaje(@PathVariable Long id, @RequestParam Long idPersonaje) {
        try {
            raidService.inscribirPersonaje(id, idPersonaje);
            return ResponseEntity.ok("Inscripción exitosa");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Desinscribe un personaje de una raid
     * POST /api/raids/{id}/desinscribir
     * 
     * @param id          ID de la raid
     * @param idPersonaje ID del personaje
     * @return ResponseEntity con status 200 o 404
     */
    @PostMapping("/{id}/desinscribir")
    public ResponseEntity<Void> desinscribirPersonaje(@PathVariable Long id, @RequestParam Long idPersonaje) {
        try {
            raidService.desinscribirPersonaje(id, idPersonaje);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene las inscripciones de una raid
     * GET /api/raids/{id}/inscripciones
     * 
     * @param id ID de la raid
     * @return Lista de inscripciones
     */
    @GetMapping("/{id}/inscripciones")
    public List<Object[]> obtenerInscripcionesRaid(@PathVariable Long id) {
        return raidService.obtenerInscripcionesRaid(id);
    }

    /**
     * Cuenta las inscripciones por estado
     * GET /api/raids/{id}/inscripciones-conteo
     * 
     * @param id ID de la raid
     * @return Lista de arrays [estado, count]
     */
    @GetMapping("/{id}/inscripciones-conteo")
    public List<Object[]> contarInscripcionesPorEstado(@PathVariable Long id) {
        return raidService.contarInscripcionesPorEstado(id);
    }
}
