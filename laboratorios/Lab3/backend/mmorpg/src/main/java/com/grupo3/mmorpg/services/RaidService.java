package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.HistorialLoot;
import com.grupo3.mmorpg.models.InscripcionRaid;
import com.grupo3.mmorpg.models.Inventario;
import com.grupo3.mmorpg.models.Item;
import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.models.Raid;
import com.grupo3.mmorpg.repositories.HistorialLootRepository;
import com.grupo3.mmorpg.repositories.InscripcionRaidRepository;
import com.grupo3.mmorpg.repositories.InventarioRepository;
import com.grupo3.mmorpg.repositories.ItemRepository;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import com.grupo3.mmorpg.repositories.RaidRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para operaciones relacionadas con Raids en MongoDB.
 * Incluye la logica de negocio equivalente a los procedimientos y triggers
 * del Lab 2 (schema.sql): validacion de item level al inscribirse,
 * registro de inscripciones y distribucion atomica de loot.
 */
@Service
public class RaidService {

    private final RaidRepository raidRepository;
    private final PersonajeRepository personajeRepository;
    private final InscripcionRaidRepository inscripcionRaidRepository;
    private final InventarioRepository inventarioRepository;
    private final ItemRepository itemRepository;
    private final HistorialLootRepository historialLootRepository;

    public RaidService(
        RaidRepository raidRepository,
        PersonajeRepository personajeRepository,
        InscripcionRaidRepository inscripcionRaidRepository,
        InventarioRepository inventarioRepository,
        ItemRepository itemRepository,
        HistorialLootRepository historialLootRepository
    ) {
        this.raidRepository = raidRepository;
        this.personajeRepository = personajeRepository;
        this.inscripcionRaidRepository = inscripcionRaidRepository;
        this.inventarioRepository = inventarioRepository;
        this.itemRepository = itemRepository;
        this.historialLootRepository = historialLootRepository;
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
    public void crearRaidConInscripcionMasiva(
        String nombre,
        LocalDateTime fecha,
        Integer itemLevel,
        Integer tanques,
        Integer healers,
        Integer dps
    ) {
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
        Raid raid = raidRepository
            .findById(idRaid)
            .orElseThrow(() -> new RuntimeException("La Raid no existe"));

        if (!raid.getEstado().equalsIgnoreCase("Programada")) {
            throw new RuntimeException(
                "No puedes inscribirte a una raid cerrada."
            );
        }

        Personaje personaje = personajeRepository
            .findById(idPersonaje)
            .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        // Equivalente al trigger fn_validar_item_level de schema.sql (T1)
        if (personaje.getItemLevel() < raid.getItemLevelRequerido()) {
            throw new RuntimeException("Nivel de objeto insuficiente.");
        }

        // El indice unico raid_personaje_unique_idx evita la doble inscripcion
        if (
            inscripcionRaidRepository.existsByRaidIdAndPersonajeId(
                idRaid,
                idPersonaje
            )
        ) {
            throw new RuntimeException("Ya estás inscrito en esta raid.");
        }

        String rol =
            personaje.getRolClan() != null
                ? personaje.getRolClan().toUpperCase()
                : "DPS";
        if (rol.equals("TANQUE")) {
            if (raid.getCuposTanque() > 0) raid.setCuposTanque(
                raid.getCuposTanque() - 1
            );
            else throw new RuntimeException("No quedan cupos para Tanques.");
        } else if (rol.equals("HEALER")) {
            if (raid.getCuposHealer() > 0) raid.setCuposHealer(
                raid.getCuposHealer() - 1
            );
            else throw new RuntimeException("No quedan cupos para Healers.");
        } else if (rol.equals("DPS")) {
            if (raid.getCuposDps() > 0) raid.setCuposDps(
                raid.getCuposDps() - 1
            );
            else throw new RuntimeException("No quedan cupos para DPS.");
        } else {
            throw new RuntimeException("Rol no reconocido: " + rol);
        }

        raidRepository.save(raid);

        // Registrar la inscripcion en su coleccion dedicada
        inscripcionRaidRepository.save(
            new InscripcionRaid(null, idRaid, idPersonaje, "Inscrito", false, 0)
        );
        return "Inscripción exitosa.";
    }

    @Transactional
    public String desinscribirPersonaje(String idRaid, String idPersonaje) {
        Raid raid = raidRepository
            .findById(idRaid)
            .orElseThrow(() -> new RuntimeException("La Raid no existe"));

        if (!raid.getEstado().equalsIgnoreCase("Programada")) {
            throw new RuntimeException(
                "No puedes salirte de una raid en curso."
            );
        }

        Personaje personaje = personajeRepository
            .findById(idPersonaje)
            .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        String rol =
            personaje.getRolClan() != null
                ? personaje.getRolClan().toUpperCase()
                : "DPS";
        if (rol.equals("TANQUE")) raid.setCuposTanque(
            raid.getCuposTanque() + 1
        );
        else if (rol.equals("HEALER")) raid.setCuposHealer(
            raid.getCuposHealer() + 1
        );
        else if (rol.equals("DPS")) raid.setCuposDps(raid.getCuposDps() + 1);

        raidRepository.save(raid);

        // Liberar el cupo en la coleccion de inscripciones
        inscripcionRaidRepository.deleteByRaidIdAndPersonajeId(
            idRaid,
            idPersonaje
        );
        return "Desinscripción exitosa.";
    }

    /**
     * Devuelve las inscripciones de una raid con el formato que espera el frontend:
     * Object[]{ idInscripcion, idPersonaje, nombrePersonaje, clasePersonaje }
     */
    public List<Object[]> obtenerInscripcionesRaid(String idRaid) {
        List<InscripcionRaid> inscripciones =
            inscripcionRaidRepository.findByRaidId(idRaid);
        if (inscripciones.isEmpty()) {
            return List.of();
        }

        List<String> idsPersonajes = inscripciones
            .stream()
            .map(InscripcionRaid::getPersonajeId)
            .distinct()
            .toList();
        Map<String, Personaje> personajes = personajeRepository
            .findAllById(idsPersonajes)
            .stream()
            .collect(Collectors.toMap(Personaje::getIdPersonaje, p -> p));

        return inscripciones
            .stream()
            .map(ins -> {
                Personaje p = personajes.get(ins.getPersonajeId());
                return new Object[] {
                    ins.getIdInscripcion(),
                    ins.getPersonajeId(),
                    p != null ? p.getNombre() : "?",
                    p != null ? p.getClase() : "?",
                };
            })
            .toList();
    }

    public List<Object[]> contarInscripcionesPorEstado(String idRaid) {
        return List.of(); // Estructura adaptada para control documental
    }

    /**
     * Distribucion de botin, equivalente a sp_distribuir_botin de schema.sql.
     * Todo ocurre dentro de una transaccion multi-documento (replica set):
     * 1) descuenta DKP al personaje
     * 2) agrega el item a su inventario (o suma cantidad si ya lo posee)
     * 3) registra el historial de loot
     * 4) marca la asistencia del personaje en la raid
     */
    @Transactional
    public void distribuirBotin(
        String idPersonaje,
        String idItem,
        String idRaid,
        Integer costoDkp
    ) {
        // Validacion de existencia: personaje, item y raid deben existir
        Personaje personaje = personajeRepository
            .findById(idPersonaje)
            .orElseThrow(() ->
                new IllegalArgumentException("Personaje no encontrado")
            );
        itemRepository
            .findById(idItem)
            .orElseThrow(() ->
                new IllegalArgumentException("Item no encontrado")
            );
        raidRepository
            .findById(idRaid)
            .orElseThrow(() ->
                new IllegalArgumentException("Raid no encontrada")
            );

        // 1) Descontar DKP (UPDATE Personaje SET puntos_merito = puntos_merito - costo)
        int costo = costoDkp != null ? costoDkp : 0;
        personaje.setPuntosMerito(personaje.getPuntosMerito() - costo);
        personajeRepository.save(personaje);

        // 2) Entregar el item (INSERT INTO Inventario / UPDATE cantidad si ya lo tiene)
        Optional<Inventario> inventarioOpt =
            inventarioRepository.findByPersonajeIdAndItemId(
                idPersonaje,
                idItem
            );
        if (inventarioOpt.isPresent()) {
            Inventario inv = inventarioOpt.get();
            inv.setCantidad(inv.getCantidad() + 1);
            inventarioRepository.save(inv);
        } else {
            inventarioRepository.save(
                new Inventario(null, idItem, idPersonaje, 1, false)
            );
        }

        // 3) Registrar historial de loot (INSERT INTO Historial_Loot)
        historialLootRepository.save(
            new HistorialLoot(
                null,
                idRaid,
                idPersonaje,
                idItem,
                LocalDateTime.now(),
                "Botín Ganado"
            )
        );

        // 4) Marcar asistencia del personaje en la raid (UPDATE Inscripcion_Raid SET asistio = TRUE)
        inscripcionRaidRepository
            .findByRaidIdAndPersonajeId(idRaid, idPersonaje)
            .ifPresent(ins -> {
                ins.setAsistio(true);
                ins.setEstado("Completada");
                inscripcionRaidRepository.save(ins);
            });
    }
}
