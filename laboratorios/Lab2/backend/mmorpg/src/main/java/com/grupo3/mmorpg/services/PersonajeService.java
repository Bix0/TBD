package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de negocio relacionadas con Personajes
 */
@Service
public class PersonajeService {

    private final PersonajeRepository personajeRepository;

    public PersonajeService(PersonajeRepository personajeRepository) {
        this.personajeRepository = personajeRepository;
    }

    @Transactional
    public Personaje crearPersonaje(Personaje personaje) {
        return personajeRepository.save(personaje);
    }

    public Optional<Personaje> obtenerPersonaje(Long id) {
        return personajeRepository.findById(id);
    }

    public List<Personaje> obtenerTodosLosPersonajes() {
        return personajeRepository.findAll();
    }

    @Transactional
    public Personaje actualizarPersonaje(Personaje personaje) {
        if (!personajeRepository.findById(personaje.getIdPersonaje()).isPresent()) {
            throw new IllegalArgumentException("Personaje no encontrado");
        }
        return personajeRepository.save(personaje);
    }

    @Transactional
    public void eliminarPersonaje(Long id) {
        personajeRepository.deleteById(id);
    }

    // METODOS ESPECIFICOS
    public List<Personaje> obtenerPersonajesPorClan(Long clanId) {
        return personajeRepository.findByClanIdClan(clanId);
    }

    public List<Personaje> obtenerPorClase(String clase) {
        return personajeRepository.findByClase(clase);
    }

    public List<Personaje> obtenerPorRolClan(String rolClan) {
        return personajeRepository.findByRolClan(rolClan);
    }

    public List<Personaje> obtenerPorItemLevelMin(Integer itemLevel) {
        return personajeRepository.findByItemLevelMin(itemLevel);
    }

    @Transactional
    public int actualizarPuntosMerito(Long idPersonaje, Integer cantidad) {
        return personajeRepository.updatePuntosMerito(idPersonaje, cantidad);
    }

    public Optional<Personaje> obtenerPorJugadorId(Long jugadorId) {
        return personajeRepository.findFirstByJugadorIdJugador(jugadorId);
    }

    public List<Personaje> obtenerTodosPorJugadorId(Long jugadorId) {
        return personajeRepository.findByJugadorIdJugador(jugadorId);
    }
}
