package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.models.Raid;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import com.grupo3.mmorpg.repositories.RaidRepository;
import org.springframework.stereotype.Service;

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

    public int crearRaid(Raid raid) { return raidRepository.create(raid); }
    public Optional<Raid> obtenerRaid(Long id) { return raidRepository.findById(id); }
    public List<Raid> obtenerTodasLasRaids() { return raidRepository.findAll(); }
    public int actualizarRaid(Raid raid) {
        if (!raidRepository.findById(raid.getId_raid()).isPresent()) throw new IllegalArgumentException("Raid no encontrada");
        return raidRepository.update(raid);
    }
    public int cambiarEstadoRaid(Long idRaid, String estado) { return raidRepository.updateEstado(idRaid, estado); }
    public int eliminarRaid(Long id) { return raidRepository.deleteById(id); }
    public List<Raid> obtenerPorEstado(String estado) { return raidRepository.findByEstado(estado); }
    public List<Raid> obtenerRaidsProgramadas() { return raidRepository.findProgramadas(); }

    public Long crearRaidConInscripcionMasiva(String nombre, LocalDateTime fecha, Integer itemLevel, Integer tanques, Integer heals, Integer dps) {
        return raidRepository.crearRaidConInscripcionMasiva(nombre, fecha, itemLevel, tanques, heals, dps);
    }

    // EL FIX ESTÁ AQUÍ: Ahora arroja excepciones (throw new)
    public String inscribirPersonaje(Long idRaid, Long idPersonaje) {
        Raid raid = raidRepository.findById(idRaid).orElseThrow(() -> new RuntimeException("La Raid no existe"));
        if (raidRepository.estaPersonajeInscrito(idRaid, idPersonaje)) throw new RuntimeException("Ya estás inscrito en esta Raid.");
        if (!raid.getEstado().equalsIgnoreCase("Programada")) throw new RuntimeException("No puedes inscribirte a una raid cerrada.");

        Personaje personaje = personajeRepository.findById(idPersonaje).orElseThrow(() -> new RuntimeException("Personaje no encontrado"));
        if (personaje.getItem_level() < raid.getItem_level_requerido()) throw new RuntimeException("Nivel de objeto insuficiente.");

        String rol = personaje.getRol_clan().toUpperCase();
        if (rol.equals("TANQUE")) {
            if (raid.getCupos_tanque() > 0) raid.setCupos_tanque(raid.getCupos_tanque() - 1);
            else throw new RuntimeException("No quedan cupos para Tanques.");
        } else if (rol.equals("HEALER")) {
            if (raid.getCupos_healer() > 0) raid.setCupos_healer(raid.getCupos_healer() - 1);
            else throw new RuntimeException("No quedan cupos para Healers.");
        } else if (rol.equals("DPS")) {
            if (raid.getCupos_dps() > 0) raid.setCupos_dps(raid.getCupos_dps() - 1);
            else throw new RuntimeException("No quedan cupos para DPS.");
        } else {
            throw new RuntimeException("Rol no reconocido: " + rol);
        }

        raidRepository.saveCupos(raid);
        raidRepository.inscribirPersonaje(idRaid, idPersonaje);
        return "Inscripción exitosa.";
    }

    public String desinscribirPersonaje(Long idRaid, Long idPersonaje) {
        Raid raid = raidRepository.findById(idRaid).orElseThrow(() -> new RuntimeException("La Raid no existe"));
        if (!raidRepository.estaPersonajeInscrito(idRaid, idPersonaje)) throw new RuntimeException("No estas inscrito en esta raid.");
        if (!raid.getEstado().equalsIgnoreCase("Programada")) throw new RuntimeException("No puedes salirte de una raid en curso.");

        Personaje personaje = personajeRepository.findById(idPersonaje).orElseThrow(() -> new RuntimeException("Personaje no encontrado"));
        String rol = personaje.getRol_clan().toUpperCase();
        if (rol.equals("TANQUE")) raid.setCupos_tanque(raid.getCupos_tanque() + 1);
        else if (rol.equals("HEALER")) raid.setCupos_healer(raid.getCupos_healer() + 1);
        else if (rol.equals("DPS")) raid.setCupos_dps(raid.getCupos_dps() + 1);

        raidRepository.saveCupos(raid);
        raidRepository.desinscribirPersonaje(idRaid, idPersonaje);
        return "Desincripción exitosa.";
    }

    public List<Object[]> obtenerInscripcionesRaid(Long idRaid) { return raidRepository.getInscripcionesRaid(idRaid); }
    public List<Object[]> contarInscripcionesPorEstado(Long idRaid) { return raidRepository.contarInscripcionesPorEstado(idRaid); }

    public void distribuirBotin(Long idPersonaje, Long idItem, Long idRaid, Integer costoDkp) {
        raidRepository.distribuirBotin(idPersonaje, idItem, idRaid, costoDkp);
    }
}