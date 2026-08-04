package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Jugador;
import com.grupo3.mmorpg.repositories.JugadorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones relacionadas con Jugadores en MongoDB
 */
@Service
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    private final PasswordEncoder passwordEncoder;

    public JugadorService(JugadorRepository jugadorRepository, PasswordEncoder passwordEncoder) {
        this.jugadorRepository = jugadorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Jugador crearJugador(Jugador jugador) {
        if (jugadorRepository.existsByUsername(jugador.getUsername())) {
            throw new IllegalArgumentException("El username ya existe");
        }
        jugador.setPassword(passwordEncoder.encode(jugador.getPassword()));
        return jugadorRepository.save(jugador);
    }

    public Optional<Jugador> obtenerJugador(String id) {
        return jugadorRepository.findById(id);
    }

    public List<Jugador> obtenerTodosLosJugadores() {
        return jugadorRepository.findAll();
    }

    @Transactional
    public Jugador actualizarJugador(Jugador jugador) {
        if (!jugadorRepository.existsById(jugador.getIdJugador())) {
            throw new IllegalArgumentException("Jugador no encontrado");
        }
        return jugadorRepository.save(jugador);
    }

    @Transactional
    public void eliminarJugador(String id) {
        jugadorRepository.deleteById(id);
    }

    public Optional<Jugador> buscarPorUsername(String username) {
        return jugadorRepository.findByUsername(username);
    }

    public boolean existeUsername(String username) {
        return jugadorRepository.existsByUsername(username);
    }

    public List<Object[]> obtenerHistorialBotinJugador(String idJugador) {
        // Adaptado para colecciones de MongoDB (retorna lista vacía o proyecciones personalizadas)
        return List.of();
    }
}