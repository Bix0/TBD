package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Jugador;
import com.grupo3.mmorpg.repositories.JugadorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    public Optional<Jugador> obtenerJugador(Long id) {
        return jugadorRepository.findById(id);
    }

    public List<Jugador> obtenerTodosLosJugadores() {
        return jugadorRepository.findAll();
    }

    @Transactional
    public Jugador actualizarJugador(Jugador jugador) {
        if (!jugadorRepository.findById(jugador.getIdJugador()).isPresent()) {
            throw new IllegalArgumentException("Jugador no encontrado");
        }
        return jugadorRepository.save(jugador);
    }

    @Transactional
    public void eliminarJugador(Long id) {
        jugadorRepository.deleteById(id);
    }

    public Optional<Jugador> buscarPorUsername(String username) {
        return jugadorRepository.findByUsername(username);
    }

    public boolean existeUsername(String username) {
        return jugadorRepository.existsByUsername(username);
    }

    public List<Object[]> obtenerHistorialBotinJugador(Long idJugador) {
        return jugadorRepository.obtenerHistorialBotinJugador(idJugador);
    }
}
