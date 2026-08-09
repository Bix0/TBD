package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.AuditoriaLiderazgo;
import com.grupo3.mmorpg.models.Clan;
import com.grupo3.mmorpg.models.Jugador;
import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.repositories.AuditoriaLiderazgoRepository;
import com.grupo3.mmorpg.repositories.ClanRepository;
import com.grupo3.mmorpg.repositories.JugadorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para operaciones de negocio relacionadas con Clanes en MongoDB
 */
@Service
public class ClanService {

    private final ClanRepository clanRepository;
    private final AuditoriaLiderazgoRepository auditoriaRepository;
    private final JugadorRepository jugadorRepository;

    @Autowired
    private PersonajeService personajeService;

    public ClanService(
        ClanRepository clanRepository,
        AuditoriaLiderazgoRepository auditoriaRepository,
        JugadorRepository jugadorRepository
    ) {
        this.clanRepository = clanRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.jugadorRepository = jugadorRepository;
    }

    @Transactional
    public Optional<Clan> unirseAlClan(String idClan, String idPersonaje) {
        Clan thisClan = clanRepository.findById(idClan)
                .orElseThrow(() -> new IllegalArgumentException("Clan no encontrado"));
        personajeService.unirseAlClan(idPersonaje, thisClan);
        return Optional.of(thisClan);
    }

    @Transactional
    public Optional<Clan> salirDeClan(String idClan, String idPersonaje) {
        Clan thisClan = clanRepository.findById(idClan)
                .orElseThrow(() -> new IllegalArgumentException("Clan no encontrado"));
        personajeService.salirDeClan(idPersonaje);
        return Optional.of(thisClan);
    }

    @Transactional
    public Clan crearClan(Clan clan) {
        if (clanRepository.existsByNombre(clan.getNombre())) {
            throw new IllegalArgumentException("El nombre del clan ya existe");
        }
        return clanRepository.save(clan);
    }

    public Optional<Clan> obtenerClan(String id) {
        return clanRepository.findById(id);
    }

    public List<Clan> obtenerTodosLosClanes() {
        return clanRepository.findAll();
    }

    @Transactional
    public Clan actualizarClan(Clan clan) {
        if (!clanRepository.existsById(clan.getIdClan())) {
            throw new IllegalArgumentException("Clan no encontrado");
        }
        return clanRepository.save(clan);
    }

    @Transactional
    public int cambiarLider(String idClan, String nuevoLider) {
        Clan clan = clanRepository.findById(idClan)
                .orElseThrow(() -> new IllegalArgumentException("Clan no encontrado"));
        String antiguoLider = clan.getIdLider();
        clan.setIdLider(nuevoLider);
        clanRepository.save(clan);

        // Trigger 2 (Lab1) + Auditoría Territorial (Lab2): registrar el cambio de liderazgo
        // (de quién a quién, cuándo y en qué coordenadas = "Sede de Poder" del clan)
        AuditoriaLiderazgo auditoria = new AuditoriaLiderazgo();
        auditoria.setClanId(idClan);
        auditoria.setAntiguoLiderId(antiguoLider);
        auditoria.setNuevoLiderId(nuevoLider);
        auditoria.setFechaCambio(LocalDateTime.now());
        auditoria.setUbicacionSuceso(clan.getUbicacion());
        auditoriaRepository.save(auditoria);
        return 1;
    }

    @Transactional
    public void eliminarClan(String id) {
        clanRepository.deleteById(id);
    }

    public Optional<Clan> buscarPorNombre(String nombre) {
        return clanRepository.findByNombre(nombre);
    }

    public boolean existeNombreClan(String nombre) {
        return clanRepository.existsByNombre(nombre);
    }

    public Optional<String> obtenerLiderId(String idClan) {
        return clanRepository.findIdLiderByIdClan(idClan);
    }

    public List<Object[]> obtenerAuditoriaLiderazgo() {
        List<AuditoriaLiderazgo> registros = auditoriaRepository.findAllByOrderByFechaCambioDesc();
        if (registros.isEmpty()) {
            return List.of();
        }

        // Resolver nombres para el formato que espera el frontend:
        // Object[]{ id, nombreClan, antiguoLider, nuevoLider, fecha, lat, lon }
        // El idLider del seed puede ser id de personaje o de jugador: resolvemos ambos.
        Map<String, String> nombresClanes = clanRepository.findAll().stream()
                .collect(Collectors.toMap(Clan::getIdClan, Clan::getNombre));
        Map<String, String> nombresPersonajes = personajeService.obtenerTodosLosPersonajes().stream()
                .collect(Collectors.toMap(Personaje::getIdPersonaje, Personaje::getNombre));
        Map<String, String> nombresJugadores = jugadorRepository.findAll().stream()
                .collect(Collectors.toMap(Jugador::getIdJugador, Jugador::getUsername));

        java.util.function.Function<String, String> resolverNombre = id ->
                nombresPersonajes.getOrDefault(id, nombresJugadores.getOrDefault(id, id));

        return registros.stream().map(a -> {
            double lat = a.getUbicacionSuceso() != null ? a.getUbicacionSuceso().getY() : 0.0;
            double lon = a.getUbicacionSuceso() != null ? a.getUbicacionSuceso().getX() : 0.0;
            return new Object[]{
                a.getIdAuditoria(),
                nombresClanes.getOrDefault(a.getClanId(), a.getClanId()),
                resolverNombre.apply(a.getAntiguoLiderId()),
                resolverNombre.apply(a.getNuevoLiderId()),
                a.getFechaCambio(),
                lat,
                lon
            };
        }).collect(Collectors.toList());
    }

    /**
     * Mapa de calor (Lab2): devuelve los clanes con ubicación y el DKP total
     * de cada uno (suma de puntosMerito de sus personajes), ordenado de mayor a menor.
     */
    public List<Map<String, Object>> obtenerMapaCalorConDkp() {
        List<Clan> clanes = clanRepository.obtenerMapaCalorClanes();

        Map<String, Integer> dkpPorClan = personajeService.obtenerTodosLosPersonajes().stream()
                .filter(p -> p.getClanId() != null)
                .collect(Collectors.groupingBy(Personaje::getClanId,
                        Collectors.summingInt(p -> p.getPuntosMerito() != null ? p.getPuntosMerito() : 0)));

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Clan clan : clanes) {
            Map<String, Object> map = new HashMap<>();
            map.put("idClan", clan.getIdClan());
            map.put("nombre", clan.getNombre());
            map.put("latitud", clan.getLatitud());
            map.put("longitud", clan.getLongitud());
            map.put("dkpTotal", dkpPorClan.getOrDefault(clan.getIdClan(), 0));
            resultado.add(map);
        }
        resultado.sort(Comparator.comparingInt(
                (Map<String, Object> m) -> (Integer) m.get("dkpTotal")).reversed());
        return resultado;
    }
}