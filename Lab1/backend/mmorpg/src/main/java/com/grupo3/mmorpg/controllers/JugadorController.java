package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Jugador;
import com.grupo3.mmorpg.services.JugadorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jugadores")
public class JugadorController {

    private final JugadorService jugadorService;

    public JugadorController(JugadorService jugadorService) {
        this.jugadorService = jugadorService;
    }

    @PostMapping
    public ResponseEntity<Jugador> crearJugador(@RequestBody Jugador jugador) {
        try {
            jugadorService.crearJugador(jugador);
            return ResponseEntity.status(HttpStatus.CREATED).body(jugador);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public List<Jugador> obtenerTodosLosJugadores() {
        return jugadorService.obtenerTodosLosJugadores();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Jugador> obtenerJugador(@PathVariable Long id) {
        return jugadorService.obtenerJugador(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Jugador> actualizarJugador(@PathVariable Long id, @RequestBody Jugador jugador) {
        jugador.setId_jugador(id);
        try {
            jugadorService.actualizarJugador(jugador);
            return ResponseEntity.ok(jugador);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarJugador(@PathVariable Long id) {
        try {
            jugadorService.eliminarJugador(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<Jugador> buscarPorUsername(@PathVariable String username) {
        return jugadorService.buscarPorUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/exists/{username}")
    public ResponseEntity<Boolean> existeUsername(@PathVariable String username) {
        return ResponseEntity.ok(jugadorService.existeUsername(username));
    }

    //Llamada final desde React a través del Servicio
    @GetMapping("/{id}/historial-loot")
    public ResponseEntity<List<Object[]>> obtenerHistorialLoot(@PathVariable Long id) {
        return ResponseEntity.ok(jugadorService.obtenerHistorialBotinJugador(id));
    }
}