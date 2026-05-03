package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Jugador;
import com.grupo3.mmorpg.repositories.JugadorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de negocio relacionadas con Jugadores
 * Utiliza JugadorRepository para acceder a los datos
 */
@Service
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    private final PasswordEncoder passwordEncoder;

    public JugadorService(JugadorRepository jugadorRepository, PasswordEncoder passwordEncoder) {
        this.jugadorRepository = jugadorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ============================================================================
    // CRUD BÁSICOS
    // ============================================================================

    /**
     * Crea un nuevo jugador
     * 
     * @param jugador Objeto Jugador con los datos a guardar
     * @return Número de filas afectadas (1 si se creó correctamente)
     */
    public int crearJugador(Jugador jugador) {
        // Validación básica: verificar que el username no exista
        if (jugadorRepository.existsByUsername(jugador.getUsername())) {
            throw new IllegalArgumentException("El username ya existe");
        }
        // Hashear la contraseña con BCrypt antes de guardar
        jugador.setPassword(passwordEncoder.encode(jugador.getPassword()));
        return jugadorRepository.create(jugador);
    }

    /**
     * Obtiene un jugador por su ID
     * 
     * @param id ID del jugador
     * @return Optional con el jugador si existe
     */
    public Optional<Jugador> obtenerJugador(Long id) {
        return jugadorRepository.findById(id);
    }

    /**
     * Obtiene todos los jugadores
     * 
     * @return Lista de todos los jugadores
     */
    public List<Jugador> obtenerTodosLosJugadores() {
        return jugadorRepository.findAll();
    }

    /**
     * Actualiza un jugador existente
     * 
     * @param jugador Objeto Jugador con los datos actualizados
     * @return Número de filas afectadas
     */
    public int actualizarJugador(Jugador jugador) {
        // Verificar que el jugador exista
        if (!jugadorRepository.findById(jugador.getId_jugador()).isPresent()) {
            throw new IllegalArgumentException("Jugador no encontrado");
        }
        return jugadorRepository.update(jugador);
    }

    /**
     * Elimina un jugador por su ID
     * 
     * @param id ID del jugador a eliminar
     * @return Número de filas afectadas
     */
    public int eliminarJugador(Long id) {
        return jugadorRepository.deleteById(id);
    }

    // ============================================================================
    // MÉTODOS ESPECÍFICOS
    // ============================================================================

    /**
     * Busca un jugador por username (útil para autenticación)
     * 
     * @param username Nombre de usuario
     * @return Optional con el jugador si existe
     */
    public Optional<Jugador> buscarPorUsername(String username) {
        return jugadorRepository.findByUsername(username);
    }

    /**
     * Verifica si un username ya está registrado
     * 
     * @param username Nombre de usuario a verificar
     * @return true si el username existe, false en caso contrario
     */
    public boolean existeUsername(String username) {
        return jugadorRepository.existsByUsername(username);
    }
}
