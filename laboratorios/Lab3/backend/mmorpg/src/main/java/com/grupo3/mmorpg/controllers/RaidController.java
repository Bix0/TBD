package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.models.Raid;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import com.grupo3.mmorpg.repositories.RaidRepository;
import com.grupo3.mmorpg.services.RaidService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/raids")
public class RaidController {

    private final RaidService raidService;
    private final RaidRepository raidRepository;
    private final PersonajeRepository personajeRepository;

    public RaidController(
        RaidService raidService,
        RaidRepository raidRepository,
        PersonajeRepository personajeRepository
    ) {
        this.raidService = raidService;
        this.raidRepository = raidRepository;
        this.personajeRepository = personajeRepository;
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
    public ResponseEntity<Void> crearRaidConInscripcionMasiva(
        @RequestParam String nombre,
        @RequestParam LocalDateTime fecha,
        @RequestParam Integer itemLevel,
        @RequestParam Integer tanques,
        @RequestParam Integer heals,
        @RequestParam Integer dps
    ) {
        raidService.crearRaidConInscripcionMasiva(
            nombre,
            fecha,
            itemLevel,
            tanques,
            heals,
            dps
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public List<Raid> obtenerTodasLasRaids() {
        return raidService.obtenerTodasLasRaids();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Raid> obtenerRaid(@PathVariable String id) {
        return raidService
            .obtenerRaid(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Raid> actualizarRaid(
        @PathVariable String id,
        @RequestBody Raid raid
    ) {
        raid.setIdRaid(id);
        try {
            raidService.actualizarRaid(raid);
            return ResponseEntity.ok(raid);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstadoRaid(
        @PathVariable String id,
        @RequestParam String estado
    ) {
        try {
            raidService.cambiarEstadoRaid(id, estado);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRaid(@PathVariable String id) {
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
    public ResponseEntity<String> inscribirPersonaje(
        @PathVariable String id,
        @RequestParam String idPersonaje
    ) {
        try {
            String mensaje = raidService.inscribirPersonaje(id, idPersonaje);
            return ResponseEntity.ok(mensaje);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/desinscribir")
    public ResponseEntity<Void> desinscribirPersonaje(
        @PathVariable String id,
        @RequestParam String idPersonaje
    ) {
        try {
            raidService.desinscribirPersonaje(id, idPersonaje);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/inscripciones")
    public List<Object[]> obtenerInscripcionesRaid(@PathVariable String id) {
        return raidService.obtenerInscripcionesRaid(id);
    }

    @GetMapping("/{id}/inscripciones-conteo")
    public List<Object[]> contarInscripcionesPorEstado(
        @PathVariable String id
    ) {
        return raidService.contarInscripcionesPorEstado(id);
    }

    @PostMapping("/distribuir-loot")
    public ResponseEntity<String> distribuirLoot(
        @RequestParam String idPersonaje,
        @RequestParam String idItem,
        @RequestParam String idRaid,
        @RequestParam Integer costoDkp
    ) {
        try {
            raidService.distribuirBotin(idPersonaje, idItem, idRaid, costoDkp);
            return ResponseEntity.ok(
                "Botín distribuido correctamente. DKP descontado."
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                "Error al distribuir botín: " + e.getMessage()
            );
        }
    }

    @GetMapping("/cercanas")
    public ResponseEntity<List<Raid>> getRaidsCercanas(
        @RequestParam(required = false) String idPersonaje,
        @RequestParam(defaultValue = "0") double lon,
        @RequestParam(defaultValue = "0") double lat,
        @RequestParam(defaultValue = "5000") double distancia
    ) {
        double lonConsulta = lon;
        double latConsulta = lat;

        if (idPersonaje != null) {
            Optional<Personaje> personajeOpt = personajeRepository.findById(
                idPersonaje
            );
            if (
                personajeOpt.isPresent() &&
                personajeOpt.get().getUbicacionActual() != null
            ) {
                lonConsulta = personajeOpt.get().getUbicacionActual().getX();
                latConsulta = personajeOpt.get().getUbicacionActual().getY();
            }
        }

        List<Raid> raidsCercanas = raidRepository.findRaidsCercanas(
            lonConsulta,
            latConsulta,
            distancia
        );
        if (raidsCercanas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(raidsCercanas);
    }
}
