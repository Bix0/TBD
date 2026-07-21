package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.services.PersonajeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operaciones con Personajes
 * Endpoints: /api/personajes
 */
@RestController
@RequestMapping("/api/personajes")
public class PersonajeController {

    private final PersonajeService personajeService;

    public PersonajeController(PersonajeService personajeService) {
        this.personajeService = personajeService;
    }


    // CRUD BASICOS
    /**
     * Crea un nuevo personaje
     * POST /api/personajes
     * @param personaje Objeto Personaje con los datos
     * @return ResponseEntity con el personaje creado
     */
    @PostMapping
    public ResponseEntity<Personaje> crearPersonaje(@RequestBody Personaje personaje) {
        try {
            personajeService.crearPersonaje(personaje);
            return ResponseEntity.status(HttpStatus.CREATED).body(personaje);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Obtiene todos los personajes
     * GET /api/personajes
     * @return Lista de personajes
     */
    @GetMapping
    public List<Personaje> obtenerTodosLosPersonajes() {
        return personajeService.obtenerTodosLosPersonajes();
    }

    /**
     * Obtiene un personaje por su ID
     * GET /api/personajes/{id}
     * @param id ID del personaje
     * @return ResponseEntity con el personaje o 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Personaje> obtenerPersonaje(@PathVariable Long id) {
        return personajeService.obtenerPersonaje(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza un personaje existente
     * PUT /api/personajes/{id}
     * @param id ID del personaje
     * @param personaje Objeto Personaje con los datos actualizados
     * @return ResponseEntity con el personaje actualizado o 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<Personaje> actualizarPersonaje(@PathVariable Long id, @RequestBody Personaje personaje) {
        personaje.setIdPersonaje(id);
        try {
            personajeService.actualizarPersonaje(personaje);
            return ResponseEntity.ok(personaje);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un personaje por su ID
     * DELETE /api/personajes/{id}
     * @param id ID del personaje
     * @return ResponseEntity con status 204 o 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPersonaje(@PathVariable Long id) {
        try {
            personajeService.eliminarPersonaje(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // METODOS ESPECIFICOS
    /**
     * Obtiene personajes por clan
     * GET /api/personajes/clan/{clanId}
     * @param clanId ID del clan
     * @return Lista de personajes del clan
     */
    @GetMapping("/clan/{clanId}")
    public List<Personaje> obtenerPorClan(@PathVariable Long clanId) {
        return personajeService.obtenerPersonajesPorClan(clanId);
    }

    /**
     * Obtiene personajes por clase
     * GET /api/personajes/clase/{clase}
     * @param clase Clase del personaje
     * @return Lista de personajes de esa clase
     */
    @GetMapping("/clase/{clase}")
    public List<Personaje> obtenerPorClase(@PathVariable String clase) {
        return personajeService.obtenerPorClase(clase);
    }

    /**
     * Obtiene personajes por rol de clan
     * GET /api/personajes/rol/{rolClan}
     * @param rolClan Rol del personaje
     * @return Lista de personajes con ese rol
     */
    @GetMapping("/rol/{rolClan}")
    public List<Personaje> obtenerPorRolClan(@PathVariable String rolClan) {
        return personajeService.obtenerPorRolClan(rolClan);
    }

    /**
     * Obtiene personajes con item_level mínimo
     * GET /api/personajes/itemlevel/{itemLevel}
     * @param itemLevel Item level mínimo
     * @return Lista de personajes que cumplen el requisito
     */
    @GetMapping("/itemlevel/{itemLevel}")
    public List<Personaje> obtenerPorItemLevelMin(@PathVariable Integer itemLevel) {
        return personajeService.obtenerPorItemLevelMin(itemLevel);
    }

    /**
     * Actualiza los puntos de mérito (DKP) de un personaje
     * PUT /api/personajes/{id}/merito
     * @param id ID del personaje
     * @param cantidad Cantidad a restar (positivo) o sumar (negativo)
     * @return ResponseEntity con status 200 o 404
     */
    @PutMapping("/{id}/merito")
    public ResponseEntity<Void> actualizarPuntosMerito(@PathVariable Long id, @RequestParam Integer cantidad) {
        try {
            personajeService.actualizarPuntosMerito(id, cantidad);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene el personaje principal de un jugador
     * GET /api/personajes/jugador/{jugadorId}
     * @param jugadorId ID del jugador
     * @return ResponseEntity con el personaje o 404
     */
    @GetMapping("/jugador/{jugadorId}")
    public ResponseEntity<Personaje> obtenerPorJugadorId(@PathVariable Long jugadorId) {
        return personajeService.obtenerPorJugadorId(jugadorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/jugador/{jugadorId}/todos")
    public List<Personaje> obtenerTodosPorJugadorId(@PathVariable Long jugadorId) {
        return personajeService.obtenerTodosPorJugadorId(jugadorId);
    }

    /**
     * Obtiene healers disponibles cerca de un Tanque
     * GET /api/personajes/healers-disponibles?tankId=X&distancia=Y
     */
    @GetMapping("/healers-disponibles")
    public ResponseEntity<List<Personaje>> obtenerHealersDisponibles(
            @RequestParam Long tankId, 
            @RequestParam(defaultValue = "500.0") double distancia) {
        try {
            List<Personaje> healers = personajeService.obtenerHealersDisponibles(tankId, distancia);
            return ResponseEntity.ok(healers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mueve espacialmente a un personaje (Actualiza Geometría)
     * PATCH /api/personajes/{id}/mover?latitud=Y&longitud=X
     */
    @PatchMapping("/{id}/mover")
    public ResponseEntity<Void> moverPersonaje(
            @PathVariable Long id,
            @RequestParam Double latitud,
            @RequestParam Double longitud) {
        try {
            personajeService.moverPersonaje(id, latitud, longitud);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
