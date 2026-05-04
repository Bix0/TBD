package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.models.Raid;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import com.grupo3.mmorpg.repositories.RaidRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de negocio relacionadas con Raids
 * Utiliza RaidRepository para acceder a los datos
 * Incluye integración con procedimientos almacenados
 */
@Service
public class RaidService {
    
    private final RaidRepository raidRepository;
    private final PersonajeRepository personajeRepository;

    public RaidService(RaidRepository raidRepository, PersonajeRepository personajeRepository) {
        this.raidRepository = raidRepository;
        this.personajeRepository = personajeRepository;
    }

    
    // ============================================================================
    // CRUD BÁSICOS
    // ============================================================================
    
    /**
     * Crea una nueva raid
     * @param raid Objeto Raid con los datos a guardar
     * @return Número de filas afectadas (1 si se creó correctamente)
     */
    public int crearRaid(Raid raid) {
        return raidRepository.create(raid);
    }
    
    /**
     * Obtiene una raid por su ID
     * @param id ID de la raid
     * @return Optional con la raid si existe
     */
    public Optional<Raid> obtenerRaid(Long id) {
        return raidRepository.findById(id);
    }
    
    /**
     * Obtiene todas las raids
     * @return Lista de todas las raids
     */
    public List<Raid> obtenerTodasLasRaids() {
        return raidRepository.findAll();
    }
    
    /**
     * Actualiza una raid existente
     * @param raid Objeto Raid con los datos actualizados
     * @return Número de filas afectadas
     */
    public int actualizarRaid(Raid raid) {
        if (!raidRepository.findById(raid.getId_raid()).isPresent()) {
            throw new IllegalArgumentException("Raid no encontrada");
        }
        return raidRepository.update(raid);
    }
    
    /**
     * Cambia el estado de una raid
     * @param idRaid ID de la raid
     * @param estado Nuevo estado (Programada, En curso, Finalizada)
     * @return Número de filas afectadas
     */
    public int cambiarEstadoRaid(Long idRaid, String estado) {
        return raidRepository.updateEstado(idRaid, estado);
    }
    
    /**
     * Elimina una raid por su ID
     * @param id ID de la raid a eliminar
     * @return Número de filas afectadas
     */
    public int eliminarRaid(Long id) {
        return raidRepository.deleteById(id);
    }
    
    // ============================================================================
    // MÉTODOS ESPECÍFICOS
    // ============================================================================
    
    /**
     * Obtiene raids por estado
     * @param estado Estado de la raid
     * @return Lista de raids con ese estado
     */
    public List<Raid> obtenerPorEstado(String estado) {
        return raidRepository.findByEstado(estado);
    }
    
    /**
     * Obtiene raids programadas
     * @return Lista de raids con estado 'Programada'
     */
    public List<Raid> obtenerRaidsProgramadas() {
        return raidRepository.findProgramadas();
    }
    
    /**
     * Crea una raid con inscripción masiva automática
     * Usa el procedimiento almacenado sp_crear_raid_e_invitar
     * @param nombre Nombre de la raid
     * @param fecha Fecha de la raid
     * @param itemLevel Item level requerido
     * @param tanques Cantidad de cupos para tanques
     * @param heals Cantidad de cupos para heals
     * @param dps Cantidad de cupos para DPS
     * @return ID de la raid creada
     */
    public Long crearRaidConInscripcionMasiva(String nombre, LocalDateTime fecha,
                                               Integer itemLevel, Integer tanques,
                                               Integer heals, Integer dps) {
        return raidRepository.crearRaidConInscripcionMasiva(nombre, fecha, itemLevel, tanques, heals, dps);
    }
    
    /**
     * Inscribe un personaje a una raid
     * @param idRaid ID de la raid
     * @param idPersonaje ID del personaje
     * @return String de mensaje de inscripcion del personaje
     */
    public String inscribirPersonaje(Long idRaid, Long idPersonaje) {
        Raid raid = raidRepository.findById(idRaid)
                .orElseThrow(() -> new RuntimeException("La Raid no existe"));

        //Verificar si personaje ya está dentro
        if (raidRepository.estaPersonajeInscrito(idRaid, idPersonaje)) {
            return "Ya estás inscrito en esta Raid.";
        }

        //Verifica que ningun personaje se inscriba a la raid salvo que este en el estado "Programada"
        if (!raid.getEstado().equalsIgnoreCase("Programada")) {
            return "No puedes modificar inscripciones en una raid que ya comenzó o terminó.";
        }

        Personaje personaje = personajeRepository
                .findById(idPersonaje)
                .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        if (personaje.getItem_level() < raid.getItem_level_requerido()) {
            return "Nivel de objeto insuficiente.";
        }
            String rol = personaje.getRol_clan().toUpperCase();
            if (rol.equals("TANQUE")) {
                if (raid.getCupos_tanque() > 0) {
                    raid.setCupos_tanque(raid.getCupos_tanque() - 1);
                } else {
                    return "No quedan cupos para Tanques.";
                }
            }
            else if (rol.equals("HEALER")) {
                if (raid.getCupos_healer() > 0) {
                    raid.setCupos_healer(raid.getCupos_healer() - 1);
                } else {
                    return "No quedan cupos para Healers.";
                }
            }
            else if (rol.equals("DPS")) {
                if (raid.getCupos_dps() > 0) {
                    raid.setCupos_dps(raid.getCupos_dps() - 1);
                } else {
                    return "No quedan cupos para DPS.";
                }
            }
            else {
                return "Rol no reconocido.";
            }
        raidRepository.saveCupos(raid); // Actualiza el cupo en la clase determinada
        raidRepository.inscribirPersonaje(idRaid, idPersonaje); //Inscribe el personaje a la raid

        return "Inscripción exitosa.";
    }
    
    /**
     * Desinscribe un personaje de una raid
     * @param idRaid ID de la raid
     * @param idPersonaje ID del personaje
     * @return String de mensaje desinscripcion del personaje
     */
    public String desinscribirPersonaje(Long idRaid, Long idPersonaje) {
        Raid raid = raidRepository.findById(idRaid)
                .orElseThrow(() -> new RuntimeException("La Raid no existe"));

        //Verificar si personaje ya está dentro de la raid
        if (!raidRepository.estaPersonajeInscrito(idRaid, idPersonaje)) {
            return "No estas inscrito en esta raid";
        }

        //Verifica que ningun personaje se desinscriba de la raid salvo que este en el estado "Programada"
        if (!raid.getEstado().equalsIgnoreCase("Programada")) {
            return "No puedes modificar inscripciones en una raid que ya comenzó o terminó.";
        }

        Personaje personaje = personajeRepository
                .findById(idPersonaje)
                .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        String rol = personaje.getRol_clan().toUpperCase();
        if (rol.equals("TANQUE")) {
            raid.setCupos_tanque(raid.getCupos_tanque() + 1);
        }
        else if (rol.equals("HEALER")) {
            raid.setCupos_healer(raid.getCupos_healer() + 1);
        }
        else if (rol.equals("DPS")) {
            raid.setCupos_dps(raid.getCupos_dps() + 1);
        }

        raidRepository.saveCupos(raid); // Actualiza el cupo
        raidRepository.desinscribirPersonaje(idRaid, idPersonaje); //Desinscribe el personaje de la raid

        return "Desincripción exitosa.";
    }
    
    /**
     * Obtiene las inscripciones de una raid
     * @param idRaid ID de la raid
     * @return Lista de inscripciones con detalles del personaje
     */
    public List<Object[]> obtenerInscripcionesRaid(Long idRaid) {
        return raidRepository.getInscripcionesRaid(idRaid);
    }
    
    /**
     * Cuenta las inscripciones por estado para una raid
     * @param idRaid ID de la raid
     * @return Lista de arrays [estado, count]
     */
    public List<Object[]> contarInscripcionesPorEstado(Long idRaid) {
        return raidRepository.contarInscripcionesPorEstado(idRaid);
    }
}