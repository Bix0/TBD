package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Clan;
import com.grupo3.mmorpg.services.ClanService;
import com.grupo3.mmorpg.repositories.ClanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operaciones con Clanes en MongoDB
 * Endpoints: /api/clanes
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/clanes")
public class ClanController {

    private final ClanService clanService;

    @Autowired
    private ClanRepository clanRepository;

    public ClanController(ClanService clanService) {
        this.clanService = clanService;
    }

    @PostMapping
    public ResponseEntity<Clan> crearClan(@RequestBody Clan clan) {
        try {
            clanService.crearClan(clan);
            return ResponseEntity.status(HttpStatus.CREATED).body(clan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public List<Clan> obtenerTodosLosClanes() {
        return clanService.obtenerTodosLosClanes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Clan> obtenerClan(@PathVariable String id) {
        return clanService.obtenerClan(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Clan> actualizarClan(@PathVariable String id, @RequestBody Clan clan) {
        clan.setIdClan(id);
        try {
            clanService.actualizarClan(clan);
            return ResponseEntity.ok(clan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/lider")
    public ResponseEntity<Void> cambiarLider(@PathVariable String id, @RequestParam String nuevoLider) {
        try {
            clanService.cambiarLider(id, nuevoLider);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarClan(@PathVariable String id) {
        try {
            clanService.eliminarClan(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Clan> buscarPorNombre(@PathVariable String nombre) {
        return clanService.buscarPorNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/exists/{nombre}")
    public ResponseEntity<Boolean> existeNombreClan(@PathVariable String nombre) {
        return ResponseEntity.ok(clanService.existeNombreClan(nombre));
    }

    @PostMapping("/unirse/{idClan}")
    public ResponseEntity<Void> unirseAlClan(@PathVariable String idClan, @RequestBody String personajeId) {
        try {
            clanService.unirseAlClan(idClan, personajeId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/salir/{idClan}")
    public ResponseEntity<Void> salirDeClan(@PathVariable String idClan, @RequestBody String personajeId) {
        try {
            clanService.salirDeClan(idClan, personajeId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/lider-id")
    public ResponseEntity<String> obtenerLiderId(@PathVariable String id) {
        return clanService.obtenerLiderId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/auditoria")
    public ResponseEntity<List<Object[]>> obtenerAuditoriaLiderazgo() {
        return ResponseEntity.ok(clanService.obtenerAuditoriaLiderazgo());
    }

    @GetMapping("/cercanos-gps")
    public ResponseEntity<List<Clan>> getClanesCercanosGPS(
            @RequestParam double lon,
            @RequestParam double lat,
            @RequestParam(defaultValue = "300") double distancia) {

        List<Clan> clanes = clanRepository.findClanesCercanosCustom(lon, lat, distancia);
        return ResponseEntity.ok(clanes);
    }

    @GetMapping("/cercanos")
    public ResponseEntity<List<Clan>> getClanesCercanos(
            @RequestParam double lon,
            @RequestParam double lat,
            @RequestParam(defaultValue = "5000") double distancia,
            @RequestParam(required = false) String faccion) {

        List<Clan> clanesCercanos = clanRepository.findClanesCercanos(lon, lat, distancia, faccion);
        if (clanesCercanos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clanesCercanos);
    }

    @GetMapping("/mapa-calor")
    public ResponseEntity<List<Clan>> getMapaCalor() {
        return ResponseEntity.ok(clanRepository.obtenerMapaCalorClanes());
    }
}