package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.HistorialLoot;
import com.grupo3.mmorpg.models.InscripcionRaid;
import com.grupo3.mmorpg.models.Inventario;
import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.models.Raid;
import com.grupo3.mmorpg.models.RepartoLoot;
import com.grupo3.mmorpg.repositories.HistorialLootRepository;
import com.grupo3.mmorpg.repositories.InscripcionRaidRepository;
import com.grupo3.mmorpg.repositories.InventarioRepository;
import com.grupo3.mmorpg.repositories.ItemRepository;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import com.grupo3.mmorpg.repositories.RaidRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
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

            // Randomizador de desempeño: al completarse la raid se generan el daño total
            // por inscrito y el tiempo de finalización (antes eran valores hardcodeados del seeder).
            if ("Completada".equalsIgnoreCase(estado)) {
                simularDesempenoRaid(idRaid);
            }

            raidRepository.save(raid);
            return 1;
        }
        return 0;
    }

    /**
     * Genera desempeño aleatorio para una raid completada: tiempo de finalización
     * (20-80 min) y daño total (10k-100k) para TODOS los inscritos (participaron del kill).
     * Alimenta el ranking por clan (Aggregation Pipeline de desempeño).
     */
    @Transactional
    public void simularDesempenoRaid(String idRaid) {
        Raid raid = raidRepository.findById(idRaid).orElse(null);
        if (raid == null) {
            return;
        }
        Random random = new Random();
        raid.setTiempoFinalizacionMinutos(20 + random.nextInt(61)); // 20-80 min
        for (InscripcionRaid ins : inscripcionRaidRepository.findByRaidId(idRaid)) {
            ins.setAsistio(true);
            ins.setDanoTotal(10_000 + random.nextInt(90_001)); // 10k-100k
            inscripcionRaidRepository.save(ins);
        }
        raidRepository.save(raid);
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
     * Distribucion de botin individual (Panel Admin, modo "dioses").
     * No exige inscripcion previa.
     */
    @Transactional
    public void distribuirBotin(
        String idPersonaje,
        String idItem,
        String idRaid,
        Integer costoDkp
    ) {
        raidRepository
            .findById(idRaid)
            .orElseThrow(() ->
                new IllegalArgumentException("Raid no encontrada")
            );
        entregarItem(
            idRaid,
            idPersonaje,
            idItem,
            costoDkp != null ? costoDkp : 0,
            false
        );
    }

    /**
     * Distribucion MASIVA de loot dentro de una UNICA transaccion multi-documento.
     *
     * Garantias (tarea del Laboratorio 3):
     * 1) Atomicidad: si cualquier reparto del lote falla, TODO se revierte
     *    (rollback de DKP, inventario e historial).
     * 2) Concurrencia: el indice unico {raidId, itemId} de historial_loot impide que
     *    un mismo item quede asignado a mas de un personaje, incluso con peticiones
     *    simultaneas (la segunda transaccion lanza DuplicateKeyException y se aborta).
     * 3) Participacion: cada personaje del lote debe estar inscrito en la raid
     *    (equivale a la regla "no participo -> no loot" de sp_distribuir_botin).
     */
    @Transactional
    public void distribuirBotinMasivo(
        String idRaid,
        List<RepartoLoot> repartos
    ) {
        if (repartos == null || repartos.isEmpty()) {
            throw new IllegalArgumentException(
                "No se recibieron repartos en el lote"
            );
        }
        raidRepository
            .findById(idRaid)
            .orElseThrow(() ->
                new IllegalArgumentException("Raid no encontrada")
            );

        // Validar que cada item aparezca una sola vez dentro del lote
        Set<String> itemsLote = new HashSet<>();
        for (RepartoLoot r : repartos) {
            if (r.idPersonaje() == null || r.idItem() == null) {
                throw new IllegalArgumentException(
                    "Cada reparto debe incluir idPersonaje e idItem"
                );
            }
            if (!itemsLote.add(r.idItem())) {
                throw new IllegalArgumentException(
                    "El item " + r.idItem() + " esta duplicado dentro del lote"
                );
            }
        }

        for (RepartoLoot r : repartos) {
            entregarItem(
                idRaid,
                r.idPersonaje(),
                r.idItem(),
                r.costoDkp() != null ? r.costoDkp() : 0,
                true
            );
        }
    }

    /**
     * Nucleo de la entrega de un item. Equivalente a sp_distribuir_botin:
     * 1) descuenta DKP 2) entrega el item al inventario 3) registra historial
     * 4) marca asistencia. Todo corre dentro de la transaccion del llamante.
     */
    private void entregarItem(
        String idRaid,
        String idPersonaje,
        String idItem,
        int costo,
        boolean validarParticipacion
    ) {
        Personaje personaje = personajeRepository
            .findById(idPersonaje)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Personaje no encontrado: " + idPersonaje
                )
            );
        itemRepository
            .findById(idItem)
            .orElseThrow(() ->
                new IllegalArgumentException("Item no encontrado: " + idItem)
            );

        // Regla de participacion: debe estar inscrito en la raid
        if (
            validarParticipacion &&
            !inscripcionRaidRepository.existsByRaidIdAndPersonajeId(
                idRaid,
                idPersonaje
            )
        ) {
            throw new IllegalArgumentException(
                "El personaje " +
                    personaje.getNombre() +
                    " no participo en esta raid"
            );
        }

        // Regla de Schema Validation (tarea): no se entrega loot a un personaje caído
        if ("Caido".equalsIgnoreCase(personaje.getEstado())) {
            throw new IllegalArgumentException(
                "El personaje " +
                    personaje.getNombre() +
                    " esta caido y no puede recibir loot"
            );
        }

        // 1) Descontar DKP (UPDATE Personaje SET puntos_merito = puntos_merito - costo)
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

        // 3) Registrar historial de loot. El indice unico {raidId, itemId} garantiza
        //    que un mismo item no pueda asignarse dos veces en la misma raid.
        //    Se incluyen los campos que exige el validador $jsonSchema de MongoSchemaConfig
        //    (participoRaid=true y estadoPersonaje="Activo"/"Vivo").
        try {
            historialLootRepository.save(
                new HistorialLoot(
                    null,
                    idRaid,
                    idPersonaje,
                    idItem,
                    LocalDateTime.now(),
                    "Botín Ganado",
                    true,
                    personaje.getEstado() != null
                        ? personaje.getEstado()
                        : "Activo"
                )
            );
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException(
                "El item ya fue asignado en esta raid a otro personaje " +
                    "(indice unico raid+item)"
            );
        }

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
