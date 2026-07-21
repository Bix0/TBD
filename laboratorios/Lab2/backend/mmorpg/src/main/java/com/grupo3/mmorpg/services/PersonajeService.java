package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Clan;
import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Servicio para operaciones de negocio relacionadas con Personajes
 */
@Service
public class PersonajeService {

    private final PersonajeRepository personajeRepository;
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public PersonajeService(PersonajeRepository personajeRepository) {
        this.personajeRepository = personajeRepository;
    }

    @Transactional
    public Personaje crearPersonaje(Personaje personaje) {
        personaje.setClan(null); // No se une automáticamente a ningún clan
        if (personaje.getUbicacionActual() == null) {
            if ("Horda".equalsIgnoreCase(personaje.getFaccion())) {
                personaje.setUbicacionActual(geometryFactory.createPoint(new Coordinate(850, 850)));
                personaje.setRegionMapa("Base Horda");
            } else {
                personaje.setUbicacionActual(geometryFactory.createPoint(new Coordinate(150, 150)));
                personaje.setRegionMapa("Base Alianza");
            }
        }
        return personajeRepository.save(personaje);
    }

    public Optional<Personaje> obtenerPersonaje(Long id) {
        return personajeRepository.findById(id);
    }

    public List<Personaje> obtenerTodosLosPersonajes() {
        return personajeRepository.findAll();
    }

    @Transactional
    public void unirseAlClan(Long idPersonaje, Clan clan) {
        Optional<Personaje> personajeOpt = personajeRepository.findById(idPersonaje);
        if (personajeOpt.isPresent()) {
            Personaje personaje = personajeOpt.get();
            if (personaje.getFaccion() != null && personaje.getFaccion().equalsIgnoreCase(clan.getFaccion())) {
                personaje.setClan(clan);
                if (clan.getUbicacion() != null) {
                    personaje.setLatitud(clan.getUbicacion().getY());
                    personaje.setLongitud(clan.getUbicacion().getX());
                }
                personajeRepository.save(personaje);
            } else {
                throw new IllegalArgumentException("El personaje y el clan no pertenecen a la misma facción");
            }

        } else {
            throw new IllegalArgumentException("Personaje no encontrado");
        }
    }

    @Transactional
    public void salirDeClan(Long idPersonaje) {
        Optional<Personaje> personajeOpt = personajeRepository.findById(idPersonaje);
        if (personajeOpt.isPresent()) {
            Personaje personaje = personajeOpt.get();
            personaje.setClan(null);
            // Re-spawn en la Base de su Facción al salir del clan
            if ("Horda".equalsIgnoreCase(personaje.getFaccion())) {
                personaje.setUbicacionActual(geometryFactory.createPoint(new Coordinate(850, 850)));
                personaje.setRegionMapa("Base Horda");
            } else {
                personaje.setUbicacionActual(geometryFactory.createPoint(new Coordinate(150, 150)));
                personaje.setRegionMapa("Base Alianza");
            }
            personajeRepository.save(personaje);
        } else {
            throw new IllegalArgumentException("Personaje no encontrado");
        }
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

    public List<Personaje> obtenerHealersDisponibles(Long tankId, double distancia) {
        Optional<Personaje> tankOpt = personajeRepository.findById(tankId);
        if (tankOpt.isEmpty()) {
            throw new IllegalArgumentException("Tanque no encontrado");
        }
        Personaje tank = tankOpt.get();
        if (tank.getLatitud() == null || tank.getLongitud() == null) {
            throw new IllegalArgumentException("El Tanque no tiene ubicación conocida");
        }
        return personajeRepository.findHealersCercanos(tank.getLongitud(), tank.getLatitud(), distancia);
    }
}
