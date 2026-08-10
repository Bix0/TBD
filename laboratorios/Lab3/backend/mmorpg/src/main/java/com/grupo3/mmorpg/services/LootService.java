package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.InscripcionRaid;
import com.grupo3.mmorpg.models.Item;
import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.models.Raid;
import com.grupo3.mmorpg.repositories.InscripcionRaidRepository;
import com.grupo3.mmorpg.repositories.ItemRepository;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import com.grupo3.mmorpg.repositories.RaidRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LootService {

    // Distancia máxima al jefe para recibir botín (Requisito de Proximidad del Lab2: 50 uds
    // en el mundo 0-1000 = 5% del mapa). Al escalar a 0-90 en MongoDB, el equivalente es
    // 50 * 90/1000 = 4.5 uds (redondeado a 5). Se convierte a metros para $near/$maxDistance.
    private static final double DISTANCIA_PROXIMIDAD_JEFE_UNIDADES = 5.0;
    private static final double DISTANCIA_PROXIMIDAD_JEFE =
        DISTANCIA_PROXIMIDAD_JEFE_UNIDADES * 111_320.0;

    private final RaidRepository raidRepository;
    private final PersonajeRepository personajeRepository;
    private final InscripcionRaidRepository inscripcionRaidRepository;
    private final ItemRepository itemRepository;
    private final RaidService raidService;
    private final MongoTemplate mongoTemplate;
    private final Random random = new Random();

    public LootService(
        RaidRepository raidRepository,
        PersonajeRepository personajeRepository,
        InscripcionRaidRepository inscripcionRaidRepository,
        ItemRepository itemRepository,
        RaidService raidService,
        MongoTemplate mongoTemplate
    ) {
        this.raidRepository = raidRepository;
        this.personajeRepository = personajeRepository;
        this.inscripcionRaidRepository = inscripcionRaidRepository;
        this.itemRepository = itemRepository;
        this.raidService = raidService;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Distribuye un botín específico asignado desde el Frontend o Panel Admin.
     * Reutiliza la consulta de proximidad de PersonajeRepository (findPersonajesCercanos / ST_DWithin de Mongo).
     */
    @Transactional
    public void distribuirBotin(
        String idPersonaje,
        String idItem,
        String idRaid,
        Integer costoDkp
    ) {
        // Validar cercanía al Jefe de la Raid usando la consulta de repositorio $near (equivalente a ST_DWithin)
        Raid r = raidRepository.findById(idRaid).orElse(null);
        if (r != null && r.getUbicacionBoss() != null) {
            double lonBoss = r.getUbicacionBoss().getX();
            double latBoss = r.getUbicacionBoss().getY();

            List<Personaje> personajesCercanos =
                personajeRepository.findPersonajesCercanos(
                    lonBoss,
                    latBoss,
                    DISTANCIA_PROXIMIDAD_JEFE
                );
            Set<String> idsCercanos = personajesCercanos
                .stream()
                .map(Personaje::getIdPersonaje)
                .collect(Collectors.toSet());

            if (!idsCercanos.contains(idPersonaje)) {
                throw new IllegalArgumentException(
                    "El personaje seleccionado está demasiado lejos del jefe de la Raid (máximo " +
                        (int) DISTANCIA_PROXIMIDAD_JEFE_UNIDADES +
                        " unidades del mapa)."
                );
            }
        }

        // 1. Ejecutar transacción de distribución de botín (actualizar personaje, inventario e historial)
        raidService.distribuirBotin(idPersonaje, idItem, idRaid, costoDkp);

        // 2. Registrar automáticamente el evento BOSS_DEATH en la colección raid_events de MongoDB
        try {
            Document eventDoc = new Document();
            eventDoc.put("raidId", idRaid);
            eventDoc.put("eventType", "BOSS_DEATH");
            eventDoc.put("idItem", idItem);
            eventDoc.put("idPersonaje", idPersonaje);
            eventDoc.put("isAlreadyDistributed", true);
            eventDoc.put("timestamp", new Date());

            mongoTemplate.insert(eventDoc, "raid_events");
        } catch (Exception e) {
            System.err.println(
                "Aviso al insertar evento BOSS_DEATH en raid_events: " +
                    e.getMessage()
            );
        }
    }

    /**
     * Procesa la distribución automática de Loot tras la muerte del Boss en una Raid (MongoDB).
     * Reutiliza la consulta geoespacial $near de PersonajeRepository para obtener sólo personajes dentro de 50m.
     */
    @Transactional
    public void distributeBossLoot(
        String raidId,
        String clanId,
        String idItemExplicit,
        String idPersonajeExplicit
    ) {
        if (raidId == null) return;

        Optional<Raid> raidOpt = raidRepository.findById(raidId);
        if (raidOpt.isEmpty()) {
            System.err.println("Aviso: Raid no encontrada para ID: " + raidId);
            return;
        }
        Raid raid = raidOpt.get();

        // Evitar WriteConflict: Si la raid ya fue completada (por la petición síncrona previa), omitir re-procesar
        if ("Completada".equalsIgnoreCase(raid.getEstado())) {
            System.out.println(
                "ℹ️ Raid " +
                    raidId +
                    " ya fue procesada y se encuentra 'Completada'. Se omite redistribución."
            );
            return;
        }

        // Generar desempeño aleatorio (daño por inscrito + tiempo de finalización):
        // alimenta el ranking por clan (Aggregation Pipeline de desempeño).
        raidService.simularDesempenoRaid(raidId);

        // 1. Determinar el ítem a entregar
        Item itemAEntregar = null;
        if (idItemExplicit != null && !idItemExplicit.isEmpty()) {
            itemAEntregar = itemRepository
                .findById(idItemExplicit)
                .orElse(null);
        }
        if (itemAEntregar == null) {
            List<Item> itemsDisponibles = itemRepository.findAll();
            if (!itemsDisponibles.isEmpty()) {
                itemAEntregar = itemsDisponibles.get(
                    random.nextInt(itemsDisponibles.size())
                );
            }
        }

        if (itemAEntregar == null) {
            System.err.println(
                "Aviso: No hay ítems en la colección 'items' para asignar a la Raid " +
                    raidId
            );
            raid.setEstado("Completada");
            raidRepository.save(raid);
            return;
        }

        Integer costoDkp =
            itemAEntregar.getGananciaDkp() != null
                ? itemAEntregar.getGananciaDkp()
                : 0;
        String idItemFinal = itemAEntregar.getIdItem();

        // Obtener la lista de personajes cercanos usando la consulta geoespacial del repositorio
        Set<String> idsPersonajesCercanos = Set.of();
        if (raid.getUbicacionBoss() != null) {
            List<Personaje> cercanos =
                personajeRepository.findPersonajesCercanos(
                    raid.getUbicacionBoss().getX(),
                    raid.getUbicacionBoss().getY(),
                    DISTANCIA_PROXIMIDAD_JEFE
                );
            idsPersonajesCercanos = cercanos
                .stream()
                .map(Personaje::getIdPersonaje)
                .collect(Collectors.toSet());
        }

        // 2. Si se especificó un personaje explícito, verificar cercanía antes de asignar
        if (idPersonajeExplicit != null && !idPersonajeExplicit.isEmpty()) {
            if (
                !idsPersonajesCercanos.isEmpty() &&
                !idsPersonajesCercanos.contains(idPersonajeExplicit)
            ) {
                System.err.println(
                    "Aviso: El personaje explícito no está cerca del jefe. Se omitirá para buscar candidatos cercanos."
                );
                idPersonajeExplicit = null;
            }
        }

        if (idPersonajeExplicit != null && !idPersonajeExplicit.isEmpty()) {
            try {
                raidService.distribuirBotin(
                    idPersonajeExplicit,
                    idItemFinal,
                    raidId,
                    costoDkp
                );
                System.out.println(
                    "✅ Botín asignado al personaje explícito ID: " +
                        idPersonajeExplicit +
                        " (Ítem: '" +
                        itemAEntregar.getNombre() +
                        "', Costo DKP: " +
                        costoDkp +
                        ")"
                );
            } catch (Exception e) {
                System.err.println(
                    "Error distribuyendo botín explícito: " + e.getMessage()
                );
            }
            raid.setEstado("Completada");
            raidRepository.save(raid);
            return;
        }

        // 3. Candidatos: inscritos en la raid y cerca del jefe, ordenados por daño
        // (los que más daño hicieron reciben loot primero).
        List<InscripcionRaid> inscripciones =
            inscripcionRaidRepository.findByRaidId(raidId);
        Map<String, Integer> danoPorPersonaje = new HashMap<>();
        List<Personaje> candidatos = new ArrayList<>();
        if (inscripciones != null) {
            for (InscripcionRaid ins : inscripciones) {
                if (idsPersonajesCercanos.contains(ins.getPersonajeId())) {
                    Optional<Personaje> pOpt = personajeRepository.findById(
                        ins.getPersonajeId()
                    );
                    if (pOpt.isPresent()) {
                        candidatos.add(pOpt.get());
                        danoPorPersonaje.put(
                            ins.getPersonajeId(),
                            ins.getDanoTotal() != null ? ins.getDanoTotal() : 0
                        );
                    }
                }
            }
        }
        candidatos.sort((a, b) ->
            Integer.compare(
                danoPorPersonaje.getOrDefault(b.getIdPersonaje(), 0),
                danoPorPersonaje.getOrDefault(a.getIdPersonaje(), 0)
            )
        );

        // 4. Pool de ítems: el explícito primero (si viene), luego el resto por nivel desc.
        List<Item> itemsDisponibles = new ArrayList<>();
        if (itemAEntregar != null) {
            itemsDisponibles.add(itemAEntregar);
        }
        itemRepository
            .findAll()
            .stream()
            .filter(i -> !itemsDisponibles.contains(i))
            .sorted(Comparator.comparingInt(
                (Item i) -> i.getItemLvl() != null ? i.getItemLvl() : 0
            ).reversed())
            .forEach(itemsDisponibles::add);

        // 5. Repartir 1 ítem DISTINTO por candidato (el mejor que pueda pagar con su DKP).
        // El índice único {raidId, itemId} garantiza que un ítem nunca se asigne dos veces.
        int asignados = 0;
        for (Personaje ganador : candidatos) {
            Item itemAsignado = null;
            for (Item item : itemsDisponibles) {
                int costoItem =
                    item.getGananciaDkp() != null ? item.getGananciaDkp() : 0;
                int dkp =
                    ganador.getPuntosMerito() != null
                        ? ganador.getPuntosMerito()
                        : 0;
                if (dkp >= costoItem) {
                    itemAsignado = item;
                    break;
                }
            }
            if (itemAsignado == null) {
                continue;
            }
            try {
                raidService.distribuirBotin(
                    ganador.getIdPersonaje(),
                    itemAsignado.getIdItem(),
                    raidId,
                    itemAsignado.getGananciaDkp() != null
                        ? itemAsignado.getGananciaDkp()
                        : 0
                );
                itemsDisponibles.remove(itemAsignado);
                asignados++;
                System.out.println(
                    "🎁 ChangeStream: " +
                    ganador.getNombre() +
                    " recibió '" +
                    itemAsignado.getNombre() +
                    "'"
                );
            } catch (Exception e) {
                System.err.println(
                    "Error distribuyendo botín automático a " +
                    ganador.getNombre() +
                    ": " +
                    e.getMessage()
                );
            }
        }
        if (asignados == 0) {
            System.out.println(
                "Aviso: Ningún personaje inscrito en la raid " +
                    raidId +
                    " está cerca del jefe con ítems disponibles."
            );
        }

        // 6. Actualizar el estado de la raid a Completada
        raid.setEstado("Completada");
        raidRepository.save(raid);
    }

    public void distributeBossLoot(String raidId, String clanId) {
        distributeBossLoot(raidId, clanId, null, null);
    }
}
