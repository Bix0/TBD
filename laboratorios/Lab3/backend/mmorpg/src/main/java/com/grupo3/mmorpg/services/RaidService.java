package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.models.Raid;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import com.grupo3.mmorpg.repositories.RaidRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones relacionadas con Raids en MongoDB
 */
@Service
public class RaidService {

    private final RaidRepository raidRepository;
    private final PersonajeRepository personajeRepository;

    public RaidService(RaidRepository raidRepository, PersonajeRepository personajeRepository) {
        this.raidRepository = raidRepository;
        this.personajeRepository = personajeRepository;
    }

    @Transactional
    public Raid crearRaid(Raid raid) {
        return raidRepository.save(raid);
    }

    public Optional<Raid> obtenerRaid(String id) {
        return raidRepository.findById(id);
    }

    public List<Raid> obtenerTodasLasRaids() {
        return raidRepository.findAll();
    }

    @Transactional
    public Raid actualizarRaid(Raid raid) {
        if (!raidRepository.existsById(raid.getIdRaid())) {
            throw new IllegalArgumentException("Raid no encontrada");
        }
        return raidRepository.save(raid);
    }

    @Transactional
    public int cambiarEstadoRaid(String idRaid, String estado) {
        Optional<Raid> raidOpt = raidRepository.findById(idRaid);
        if (raidOpt.isPresent()) {
            Raid raid = raidOpt.get();
            raid.setEstado(estado);
            raidRepository.save(raid);
            return 1;
        }
        return 0;
    }

    @Transactional
    public void eliminarRaid(String id) {
        raidRepository.deleteById(id);
    }

    public List<Raid> obtenerPorEstado(String estado) {
        return raidRepository.findByEstadoOrderByFechaDesc(estado);
    }

    public List<Raid> obtenerRaidsProgramadas() {
        return raidRepository.findProgramadas();
    }

    @Transactional
    public void crearRaidConInscripcionMasiva(String nombre, LocalDateTime fecha,
                                              Integer itemLevel, Integer tanques,
                                              Integer healers, Integer dps) {
        Raid nuevaRaid = new Raid();
        nuevaRaid.setNombre(nombre);
        nuevaRaid.setFecha(fecha != null ? fecha : LocalDateTime.now());
        nuevaRaid.setItemLevelRequerido(itemLevel != null ? itemLevel : 0);
        nuevaRaid.setCuposTanque(tanques != null ? tanques : 2);
        nuevaRaid.setCuposHealer(healers != null ? healers : 4);
        nuevaRaid.setCuposDps(dps != null ? dps : 14);
        nuevaRaid.setEstado("Programada");
        raidRepository.save(nuevaRaid);
    }

    @Transactional
    public String inscribirPersonaje(String idRaid, String idPersonaje) {
        Raid raid = raidRepository.findById(idRaid)
                .orElseThrow(() -> new RuntimeException("La Raid no existe"));

        if (!raid.getEstado().equalsIgnoreCase("Programada")) {
            throw new RuntimeException("No puedes inscribirte a una raid cerrada.");
        }

        Personaje personaje = personajeRepository.findById(idPersonaje)
                .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        if (personaje.getItemLevel() < raid.getItemLevelRequerido()) {
            throw new RuntimeException("Nivel de objeto insuficiente.");
        }

        String rol = personaje.getRolClan() != null ? personaje.getRolClan().toUpperCase() : "DPS";
        if (rol.equals("TANQUE")) {
            if (raid.getCuposTanque() > 0) raid.setCuposTanque(raid.getCuposTanque() - 1);
            else throw new RuntimeException("No quedan cupos para Tanques.");
        } else if (rol.equals("HEALER")) {
            if (raid.getCuposHealer() > 0) raid.setCuposHealer(raid.getCuposHealer() - 1);
            else throw new RuntimeException("No quedan cupos para Healers.");
        } else if (rol.equals("DPS")) {
            if (raid.getCuposDps() > 0) raid.setCuposDps(raid.getCuposDps() - 1);
            else throw new RuntimeException("No quedan cupos para DPS.");
        } else {
            throw new RuntimeException("Rol no reconocido: " + rol);
        }

        raidRepository.save(raid);
        return "Inscripción exitosa.";
    }

    @Transactional
    public String desinscribirPersonaje(String idRaid, String idPersonaje) {
        Raid raid = raidRepository.findById(idRaid)
                .orElseThrow(() -> new RuntimeException("La Raid no existe"));

        if (!raid.getEstado().equalsIgnoreCase("Programada")) {
            throw new RuntimeException("No puedes salirte de una raid en curso.");
        }

        Personaje personaje = personajeRepository.findById(idPersonaje)
                .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        String rol = personaje.getRolClan() != null ? personaje.getRolClan().toUpperCase() : "DPS";
        if (rol.equals("TANQUE")) raid.setCuposTanque(raid.getCuposTanque() + 1);
        else if (rol.equals("HEALER")) raid.setCuposHealer(raid.getCuposHealer() + 1);
        else if (rol.equals("DPS")) raid.setCuposDps(raid.getCuposDps() + 1);

        raidRepository.save(raid);
        return "Desinscripción exitosa.";
    }

    public List<Object[]> obtenerInscripcionesRaid(String idRaid) {
        return List.of(); // Estructura adaptada para control documental
    }

    public List<Object[]> contarInscripcionesPorEstado(String idRaid) {
        return List.of();
    }

    @Transactional
    public void distribuirBotin(String idPersonaje, String idItem, String idRaid, Integer costoDkp) {
        Personaje personaje = personajeRepository.findById(idPersonaje)
                .orElseThrow(() -> new IllegalArgumentException("Personaje no encontrado"));

        // Descontar puntos DKP de forma transaccional
        personaje.setPuntosMerito(personaje.getPuntosMerito() - (costoDkp != null ? costoDkp : 0));
        personajeRepository.save(personaje);
    }
}