package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.services.ClanService;
import com.grupo3.mmorpg.services.PersonajeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operaciones con Personajes (MongoDB)
 * Endpoints: /api/personajes
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/personajes")
public class PersonajeController {

    private final PersonajeService personajeService;
    private final ClanService clanService;

    public PersonajeController(
        PersonajeService personajeService,
        ClanService clanService
    ) {
        this.personajeService = personajeService;
        this.clanService = clanService;
    }

    // ==========================================
    // CRUD BÁSICOS
    // ==========================================

    @PostMapping
    public ResponseEntity<Personaje> crearPersonaje(@RequestBody Personaje personaje) {
        try {
            personajeService.crearPersonaje(personaje);
            return ResponseEntity.status(HttpStatus.CREATED).body(personaje);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public List<Personaje> obtenerTodosLosPersonajes() {
        return personajeService.obtenerTodosLosPersonajes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Personaje> obtenerPersonaje(@PathVariable String id) {
        return personajeService.obtenerPersonaje(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Personaje> actualizarPersonaje(@PathVariable String id, @RequestBody Personaje personaje) {
        personaje.setIdPersonaje(id);
        try {
            personajeService.actualizarPersonaje(personaje);
            return ResponseEntity.ok(personaje);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPersonaje(@PathVariable String id) {
        try {
            personajeService.eliminarPersonaje(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==========================================
    // MÉTODOS ESPECÍFICOS DE BÚSQUEDA
    // ==========================================

    @GetMapping("/clan/{clanId}")
    public List<Personaje> obtenerPorClan(@PathVariable String clanId) {
        return personajeService.obtenerPersonajesPorClan(clanId);
    }

    @GetMapping("/clase/{clase}")
    public List<Personaje> obtenerPorClase(@PathVariable String clase) {
        return personajeService.obtenerPorClase(clase);
    }

    @GetMapping("/rol/{rolClan}")
    public List<Personaje> obtenerPorRolClan(@PathVariable String rolClan) {
        return personajeService.obtenerPorRolClan(rolClan);
    }

    @GetMapping("/itemlevel/{itemLevel}")
    public List<Personaje> obtenerPorItemLevelMin(@PathVariable Integer itemLevel) {
        return personajeService.obtenerPorItemLevelMin(itemLevel);
    }

    @PutMapping("/{id}/merito")
    public ResponseEntity<Void> actualizarPuntosMerito(
        @PathVariable String id,
        @RequestParam Integer cantidad
    ) {
        try {
            personajeService.actualizarPuntosMerito(id, cantidad);
            // Ascenso automático de líder por DKP (equivalente al trigger T4 del Lab1)
            clanService.verificarAscensoLider(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/jugador/{jugadorId}")
    public ResponseEntity<Personaje> obtenerPorJugadorId(@PathVariable String jugadorId) {
        return personajeService.obtenerPorJugadorId(jugadorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/jugador/{jugadorId}/todos")
    public List<Personaje> obtenerTodosPorJugadorId(@PathVariable String jugadorId) {
        return personajeService.obtenerTodosPorJugadorId(jugadorId);
    }

    // ==========================================
    // MÉTODOS GEOESPACIALES Y DE MAPA
    // ==========================================

    @GetMapping("/healers-disponibles")
    public ResponseEntity<List<Personaje>> obtenerHealersDisponibles(
            @RequestParam String tankId,
            @RequestParam(defaultValue = "500.0") double distancia) {
        try {
            List<Personaje> healers = personajeService.obtenerHealersDisponibles(tankId, distancia);
            return ResponseEntity.ok(healers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/mover")
    public ResponseEntity<Void> moverPersonaje(
            @PathVariable String id,
            @RequestParam Double latitud,
            @RequestParam Double longitud) {
        try {
            personajeService.moverPersonaje(id, latitud, longitud);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/mapa")
    public List<Personaje> obtenerPersonajesConUbicacion() {
        return personajeService.obtenerPersonajesConUbicacion();
    }

    @GetMapping("/mapa/rol/{rol}")
    public List<Personaje> obtenerPersonajesPorRolEnMapa(@PathVariable String rol) {
        return personajeService.obtenerPersonajesPorRolEnMapa(rol);
    }
}