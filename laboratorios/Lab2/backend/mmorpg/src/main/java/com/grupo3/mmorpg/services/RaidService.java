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

    public Optional<Raid> obtenerRaid(Long id) {
        return raidRepository.findById(id);
    }

    public List<Raid> obtenerTodasLasRaids() {
        return raidRepository.findAll();
    }

    @Transactional
    public Raid actualizarRaid(Raid raid) {
        if (!raidRepository.findById(raid.getIdRaid()).isPresent()) {
            throw new IllegalArgumentException("Raid no encontrada");
        }
        return raidRepository.save(raid);
    }

    @Transactional
    public int cambiarEstadoRaid(Long idRaid, String estado) {
        return raidRepository.updateEstado(idRaid, estado);
    }

    @Transactional
    public void eliminarRaid(Long id) {
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
        raidRepository.crearRaidConInscripcionMasiva(nombre, fecha, itemLevel, tanques, healers, dps);
    }

    @Transactional
    public String inscribirPersonaje(Long idRaid, Long idPersonaje) {
        Raid raid = raidRepository.findById(idRaid)
                .orElseThrow(() -> new RuntimeException("La Raid no existe"));

        if (raidRepository.estaPersonajeInscrito(idRaid, idPersonaje) > 0) {
            throw new RuntimeException("Ya estás inscrito en esta Raid.");
        }
        if (!raid.getEstado().equalsIgnoreCase("Programada")) {
            throw new RuntimeException("No puedes inscribirte a una raid cerrada.");
        }

        Personaje personaje = personajeRepository.findById(idPersonaje)
                .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        if (personaje.getItemLevel() < raid.getItemLevelRequerido()) {
            throw new RuntimeException("Nivel de objeto insuficiente.");
        }

        String rol = personaje.getRolClan().toUpperCase();
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
        raidRepository.inscribirPersonaje(idRaid, idPersonaje);
        return "Inscripción exitosa.";
    }

    @Transactional
    public String desinscribirPersonaje(Long idRaid, Long idPersonaje) {
        Raid raid = raidRepository.findById(idRaid)
                .orElseThrow(() -> new RuntimeException("La Raid no existe"));

        if (raidRepository.estaPersonajeInscrito(idRaid, idPersonaje) == 0) {
            throw new RuntimeException("No estás inscrito en esta raid.");
        }
        if (!raid.getEstado().equalsIgnoreCase("Programada")) {
            throw new RuntimeException("No puedes salirte de una raid en curso.");
        }

        Personaje personaje = personajeRepository.findById(idPersonaje)
                .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        String rol = personaje.getRolClan().toUpperCase();
        if (rol.equals("TANQUE")) raid.setCuposTanque(raid.getCuposTanque() + 1);
        else if (rol.equals("HEALER")) raid.setCuposHealer(raid.getCuposHealer() + 1);
        else if (rol.equals("DPS")) raid.setCuposDps(raid.getCuposDps() + 1);

        raidRepository.save(raid);
        raidRepository.desinscribirPersonaje(idRaid, idPersonaje);
        return "Desincripción exitosa.";
    }

    public List<Object[]> obtenerInscripcionesRaid(Long idRaid) {
        return raidRepository.getInscripcionesRaid(idRaid);
    }

    public List<Object[]> contarInscripcionesPorEstado(Long idRaid) {
        return raidRepository.contarInscripcionesPorEstado(idRaid);
    }

    @Transactional
    public void distribuirBotin(Long idPersonaje, Long idItem, Long idRaid, Integer costoDkp) {
        raidRepository.distribuirBotin(idPersonaje, idItem, idRaid, costoDkp);
    }
}
