package com.grupo3.mmorpg.controllers;

import com.grupo3.mmorpg.models.Jugador;
import com.grupo3.mmorpg.services.JugadorService;
import com.grupo3.mmorpg.services.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controlador de autenticación.
 * Endpoint público: POST /api/auth/login
 * Recibe username + password, devuelve JWT si son válidos.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JugadorService jugadorService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JugadorService jugadorService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.jugadorService = jugadorService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * POST /api/auth/login
     * Body: { "username": "bruno", "password": "123456" }
     * Response: { "token": "eyJhbGci...", "username": "bruno", "rol": "Admin",
     * "id": 1 }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username y password son requeridos"));
        }

        Optional<Jugador> jugadorOpt = jugadorService.buscarPorUsername(username);

        if (jugadorOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }

        Jugador jugador = jugadorOpt.get();

        // Verificar password con BCrypt
        if (!passwordEncoder.matches(password, jugador.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }

        // Generar token JWT
        String token = jwtUtil.generateToken(jugador.getId_jugador(), jugador.getUsername(), jugador.getRol());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", jugador.getUsername(),
                "rol", jugador.getRol(),
                "id", jugador.getId_jugador()));
    }

    /**
     * POST /api/auth/register
     * Body: { "username": "nuevo", "password": "micontra", "rol": "Usuario" }
     * El rol es opcional, por defecto "Usuario".
     * Crea un jugador con password hasheado con BCrypt.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String rol = body.getOrDefault("rol", "Usuario");

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username y password son requeridos"));
        }

        try {
            Jugador nuevo = new Jugador(null, username, password, rol);
            jugadorService.crearJugador(nuevo);
            return ResponseEntity.ok(Map.of("message", "Usuario registrado exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
