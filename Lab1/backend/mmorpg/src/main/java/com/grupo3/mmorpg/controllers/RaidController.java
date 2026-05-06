package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Raid;
import com.grupo3.mmorpg.services.RaidService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/raids")
public class RaidController {

    private final RaidService raidService;

    public RaidController(RaidService raidService) {
        this.raidService = raidService;
    }

    @PostMapping
    public ResponseEntity<Raid> crearRaid(@RequestBody Raid raid) {
        try {
            raidService.crearRaid(raid);
            return ResponseEntity.status(HttpStatus.CREATED).body(raid);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/con-inscripcion-masiva")
    public ResponseEntity<Long> crearRaidConInscripcionMasiva(@RequestParam String nombre, @RequestParam LocalDateTime fecha, @RequestParam Integer itemLevel, @RequestParam Integer tanques, @RequestParam Integer heals, @RequestParam Integer dps) {
        Long idRaid = raidService.crearRaidConInscripcionMasiva(nombre, fecha, itemLevel, tanques, heals, dps);
        return ResponseEntity.status(HttpStatus.CREATED).body(idRaid);
    }

    @GetMapping
    public List<Raid> obtenerTodasLasRaids() {
        return raidService.obtenerTodasLasRaids();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Raid> obtenerRaid(@PathVariable Long id) {
        return raidService.obtenerRaid(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

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

    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstadoRaid(@PathVariable Long id, @RequestParam String estado) {
        try {
            raidService.cambiarEstadoRaid(id, estado);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRaid(@PathVariable Long id) {
        try {
            raidService.eliminarRaid(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/estado/{estado}")
    public List<Raid> obtenerPorEstado(@PathVariable String estado) {
        return raidService.obtenerPorEstado(estado);
    }

    @GetMapping("/programadas")
    public List<Raid> obtenerRaidsProgramadas() {
        return raidService.obtenerRaidsProgramadas();
    }

    @PostMapping("/{id}/inscribir")
    public ResponseEntity<String> inscribirPersonaje(@PathVariable Long id, @RequestParam Long idPersonaje) {
        try {
            String mensaje = raidService.inscribirPersonaje(id, idPersonaje);
            return ResponseEntity.ok(mensaje);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/desinscribir")
    public ResponseEntity<Void> desinscribirPersonaje(@PathVariable Long id, @RequestParam Long idPersonaje) {
        try {
            raidService.desinscribirPersonaje(id, idPersonaje);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/inscripciones")
    public List<Object[]> obtenerInscripcionesRaid(@PathVariable Long id) {
        return raidService.obtenerInscripcionesRaid(id);
    }

    @GetMapping("/{id}/inscripciones-conteo")
    public List<Object[]> contarInscripcionesPorEstado(@PathVariable Long id) {
        return raidService.contarInscripcionesPorEstado(id);
    }

    // EL NUEVO ENDPOINT PARA ENTREGAR LOOT Y COBRAR DKP
    @PostMapping("/distribuir-loot")
    public ResponseEntity<String> distribuirLoot(@RequestParam Long idPersonaje, @RequestParam Long idItem, @RequestParam Long idRaid, @RequestParam Integer costoDkp) {
        try {
            raidService.distribuirBotin(idPersonaje, idItem, idRaid, costoDkp);
            return ResponseEntity.ok("Botín distribuido correctamente. DKP descontado.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al distribuir botín: " + e.getMessage());
        }
    }
}
