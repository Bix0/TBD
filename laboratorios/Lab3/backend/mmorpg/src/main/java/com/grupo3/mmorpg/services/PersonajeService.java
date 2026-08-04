package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Clan;
import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para operaciones de negocio relacionadas con Personajes (MongoDB)
 */
@Service
public class PersonajeService {

    private final PersonajeRepository personajeRepository;

    public PersonajeService(PersonajeRepository personajeRepository) {
        this.personajeRepository = personajeRepository;
    }

    @Transactional
    public Personaje crearPersonaje(Personaje personaje) {
        personaje.setClanId(null); // No se une automáticamente a ningún clan
        if (personaje.getUbicacionActual() == null) {
            // Nota: GeoJsonPoint recibe (Longitud, Latitud) en ese orden (X, Y).
            // Las coordenadas deben estar dentro de los rangos GeoJSON del indice 2dsphere
            // (latitud [-90, 90], longitud [-180, 180]), igual que las del DataSeeder.
            if ("Horda".equalsIgnoreCase(personaje.getFaccion())) {
                personaje.setUbicacionActual(new GeoJsonPoint(85.0, 85.0));
                personaje.setRegionMapa("Base Horda");
            } else {
                personaje.setUbicacionActual(new GeoJsonPoint(15.0, 15.0));
                personaje.setRegionMapa("Base Alianza");
            }
        }
        return personajeRepository.save(personaje);
    }

    public Optional<Personaje> obtenerPersonaje(String id) {
        return personajeRepository.findById(id);
    }

    public List<Personaje> obtenerTodosLosPersonajes() {
        return personajeRepository.findAll();
    }

    @Transactional
    public void unirseAlClan(String idPersonaje, Clan clan) {
        Optional<Personaje> personajeOpt = personajeRepository.findById(
            idPersonaje
        );
        if (personajeOpt.isPresent()) {
            Personaje personaje = personajeOpt.get();
            if (
                personaje.getFaccion() != null &&
                personaje.getFaccion().equalsIgnoreCase(clan.getFaccion())
            ) {
                personaje.setClanId(clan.getIdClan()); // Guardamos la referencia en vez del objeto completo

                // Asumiremos que el Clan también será migrado a Mongo y usará GeoJsonPoint
                // Si el clan tiene ubicación, teletransportamos al jugador a la sede del clan
                if (clan.getUbicacion() != null) {
                    personaje.setLatitud(clan.getUbicacion().getY());
                    personaje.setLongitud(clan.getUbicacion().getX());
                }
                personajeRepository.save(personaje);
            } else {
                throw new IllegalArgumentException(
                    "El personaje y el clan no pertenecen a la misma facción"
                );
            }
        } else {
            throw new IllegalArgumentException("Personaje no encontrado");
        }
    }

    @Transactional
    public void salirDeClan(String idPersonaje) {
        Optional<Personaje> personajeOpt = personajeRepository.findById(
            idPersonaje
        );
        if (personajeOpt.isPresent()) {
            Personaje personaje = personajeOpt.get();
            personaje.setClanId(null);
            // Re-spawn en la Base de su Facción al salir del clan
            if ("Horda".equalsIgnoreCase(personaje.getFaccion())) {
                personaje.setUbicacionActual(new GeoJsonPoint(85.0, 85.0));
                personaje.setRegionMapa("Base Horda");
            } else {
                personaje.setUbicacionActual(new GeoJsonPoint(15.0, 15.0));
                personaje.setRegionMapa("Base Alianza");
            }
            personajeRepository.save(personaje);
        } else {
            throw new IllegalArgumentException("Personaje no encontrado");
        }
    }

    @Transactional
    public Personaje actualizarPersonaje(Personaje personaje) {
        if (!personajeRepository.existsById(personaje.getIdPersonaje())) {
            throw new IllegalArgumentException("Personaje no encontrado");
        }
        return personajeRepository.save(personaje);
    }

    @Transactional
    public void eliminarPersonaje(String id) {
        personajeRepository.deleteById(id);
    }

    // METODOS ESPECIFICOS
    public List<Personaje> obtenerPersonajesPorClan(String clanId) {
        return personajeRepository.findByClanId(clanId);
    }

    public List<Personaje> obtenerPorClase(String clase) {
        return personajeRepository.findByClase(clase);
    }

    public List<Personaje> obtenerPorRolClan(String rolClan) {
        return personajeRepository.findByRolClan(rolClan);
    }

    public List<Personaje> obtenerPorItemLevelMin(Integer itemLevel) {
        return personajeRepository.findByItemLevelGreaterThanEqualOrderByItemLevelDesc(
            itemLevel
        );
    }

    @Transactional
    public int actualizarPuntosMerito(String idPersonaje, Integer cantidad) {
        Personaje p = personajeRepository
            .findById(idPersonaje)
            .orElseThrow(() ->
                new IllegalArgumentException("Personaje no encontrado")
            );
        // Simulamos la operación UPDATE p SET p.puntosMerito = p.puntosMerito - cantidad
        p.setPuntosMerito(p.getPuntosMerito() - cantidad);
        personajeRepository.save(p);
        return 1; // 1 fila afectada
    }

    public Optional<Personaje> obtenerPorJugadorId(String jugadorId) {
        return personajeRepository.findFirstByJugadorId(jugadorId);
    }

    public List<Personaje> obtenerTodosPorJugadorId(String jugadorId) {
        return personajeRepository.findByJugadorId(jugadorId);
    }

    public List<Personaje> obtenerHealersDisponibles(
        String tankId,
        double distancia
    ) {
        Optional<Personaje> tankOpt = personajeRepository.findById(tankId);
        if (tankOpt.isEmpty()) {
            throw new IllegalArgumentException("Tanque no encontrado");
        }
        Personaje tank = tankOpt.get();
        if (tank.getLatitud() == null || tank.getLongitud() == null) {
            throw new IllegalArgumentException(
                "El Tanque no tiene ubicación conocida"
            );
        }
        return personajeRepository.findHealersCercanos(
            tank.getLongitud(),
            tank.getLatitud(),
            distancia
        );
    }

    @Transactional
    public void moverPersonaje(
        String idPersonaje,
        Double latitud,
        Double longitud
    ) {
        Personaje personaje = personajeRepository
            .findById(idPersonaje)
            .orElseThrow(() ->
                new IllegalArgumentException("Personaje no encontrado")
            );
        personaje.setUbicacionActual(new GeoJsonPoint(longitud, latitud));
        personajeRepository.save(personaje);
    }

    public List<Personaje> obtenerPersonajesConUbicacion() {
        return personajeRepository.findAllConUbicacion();
    }

    public List<Personaje> obtenerPersonajesPorRolEnMapa(String rol) {
        return personajeRepository.findByRolClanIgnoreCaseAndUbicacionNotNull(
            rol
        );
    }
}
