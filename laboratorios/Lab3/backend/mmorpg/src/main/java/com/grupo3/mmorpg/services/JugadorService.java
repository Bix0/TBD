package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.HistorialLoot;
import com.grupo3.mmorpg.models.Item;
import com.grupo3.mmorpg.models.Jugador;
import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.models.Raid;
import com.grupo3.mmorpg.repositories.HistorialLootRepository;
import com.grupo3.mmorpg.repositories.ItemRepository;
import com.grupo3.mmorpg.repositories.JugadorRepository;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import com.grupo3.mmorpg.repositories.RaidRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para operaciones relacionadas con Jugadores en MongoDB.
 */
@Service
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersonajeRepository personajeRepository;
    private final HistorialLootRepository historialLootRepository;
    private final ItemRepository itemRepository;
    private final RaidRepository raidRepository;

    public JugadorService(
        JugadorRepository jugadorRepository,
        PasswordEncoder passwordEncoder,
        PersonajeRepository personajeRepository,
        HistorialLootRepository historialLootRepository,
        ItemRepository itemRepository,
        RaidRepository raidRepository
    ) {
        this.jugadorRepository = jugadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.personajeRepository = personajeRepository;
        this.historialLootRepository = historialLootRepository;
        this.itemRepository = itemRepository;
        this.raidRepository = raidRepository;
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

    /**
     * Historial de botin de todos los personajes de un jugador, con el formato
     * que espera el frontend: Object[]{ fecha, nombrePersonaje, nombreItem, nombreRaid }
     */
    public List<Object[]> obtenerHistorialBotinJugador(String idJugador) {
        List<Personaje> personajes = personajeRepository.findByJugadorId(
            idJugador
        );
        if (personajes.isEmpty()) {
            return List.of();
        }

        List<String> idsPersonajes = personajes
            .stream()
            .map(Personaje::getIdPersonaje)
            .toList();
        List<HistorialLoot> historial =
            historialLootRepository.findByPersonajeIdIn(idsPersonajes);
        if (historial.isEmpty()) {
            return List.of();
        }

        Map<String, String> nombresPersonajes = personajes
            .stream()
            .collect(
                Collectors.toMap(
                    Personaje::getIdPersonaje,
                    Personaje::getNombre
                )
            );
        Map<String, String> nombresItems = itemRepository
            .findAll()
            .stream()
            .collect(Collectors.toMap(Item::getIdItem, Item::getNombre));
        Map<String, String> nombresRaids = raidRepository
            .findAll()
            .stream()
            .collect(Collectors.toMap(Raid::getIdRaid, Raid::getNombre));

        return historial
            .stream()
            .sorted((a, b) -> b.getFecha().compareTo(a.getFecha()))
            .map(h -> new Object[] {
                h.getFecha(),
                nombresPersonajes.getOrDefault(h.getPersonajeId(), "?"),
                nombresItems.getOrDefault(h.getItemId(), "?"),
                nombresRaids.getOrDefault(h.getRaidId(), "?"),
            })
            .toList();
    }
}
