package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.models.Raid;
import com.grupo3.mmorpg.models.RepartoLoot;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import com.grupo3.mmorpg.repositories.RaidRepository;
import com.grupo3.mmorpg.services.LootService;
import com.grupo3.mmorpg.services.RaidService;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
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
    private final LootService lootService;
    private final MongoTemplate mongoTemplate;

    public RaidController(
        RaidService raidService,
        RaidRepository raidRepository,
        PersonajeRepository personajeRepository,
        LootService lootService,
        MongoTemplate mongoTemplate
    ) {
        this.raidService = raidService;
        this.raidRepository = raidRepository;
        this.personajeRepository = personajeRepository;
        this.lootService = lootService;
        this.mongoTemplate = mongoTemplate;
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
            lootService.distribuirBotin(idPersonaje, idItem, idRaid, costoDkp);
            return ResponseEntity.ok(
                "Botín distribuido correctamente. DKP descontado."
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                "Error al distribuir botín: " + e.getMessage()
            );
        }
    }

    /**
     * Endpoint para simular/disparar el evento 'BOSS_DEATH' en MongoDB (raid_events).
     * El listener ChangeStream captura este evento e invoca automáticamente LootService y RankingService.
     */
    @PostMapping("/{id}/evento-muerte-boss")
    public ResponseEntity<String> registrarMuerteBoss(
        @PathVariable String id,
        @RequestParam(required = false) String clanId,
        @RequestParam(required = false) String idItem,
        @RequestParam(required = false) String idPersonaje
    ) {
        Optional<Raid> raidOpt = raidRepository.findById(id);
        if (raidOpt.isPresent() && "Completada".equalsIgnoreCase(raidOpt.get().getEstado())) {
            return ResponseEntity.badRequest().body(
                "❌ La Raid ya se encuentra completada y cerrada."
            );
        }

        Document event = new Document();
        event.put("raidId", id);
        event.put("eventType", "BOSS_DEATH");
        if (clanId != null) event.put("clanId", clanId);
        if (idItem != null) event.put("idItem", idItem);
        if (idPersonaje != null) event.put("idPersonaje", idPersonaje);
        event.put("timestamp", new Date());

        mongoTemplate.insert(event, "raid_events");
        return ResponseEntity.ok(
            "Evento BOSS_DEATH registrado en MongoDB 'raid_events'. ChangeStream procesando..."
        );
    }

    /**
     * Distribución masiva de loot en una sola transacción atómica.
     * Body: [{ "idPersonaje": "...", "idItem": "...", "costoDkp": 50 }, ...]
     */
    @PostMapping("/{id}/distribuir-loot-masivo")
    public ResponseEntity<String> distribuirLootMasivo(
        @PathVariable String id,
        @RequestBody List<RepartoLoot> repartos
    ) {
        try {
            raidService.distribuirBotinMasivo(id, repartos);
            return ResponseEntity.ok(
                "Lote distribuido correctamente. Transacción atómica confirmada."
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                "Error al distribuir lote: " + e.getMessage()
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

        try {
            List<Raid> raidsCercanas = raidRepository.findRaidsCercanas(
                lonConsulta,
                latConsulta,
                distancia
            );
            if (raidsCercanas != null && !raidsCercanas.isEmpty()) {
                return ResponseEntity.ok(raidsCercanas);
            }
        } catch (Exception e) {
            System.err.println(
                "Aviso al consultar raids cercanas: " + e.getMessage()
            );
        }
        return ResponseEntity.ok(raidRepository.findAll());
    }
}
