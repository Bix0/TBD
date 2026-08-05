package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.InscripcionRaid;
import com.grupo3.mmorpg.models.Item;
import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.models.Raid;
import com.grupo3.mmorpg.repositories.InscripcionRaidRepository;
import com.grupo3.mmorpg.repositories.ItemRepository;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import com.grupo3.mmorpg.repositories.RaidRepository;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LootService {

    // Distancia máxima al jefe para recibir botín (Requisito de Proximidad: 50 unidades, equivalente a ST_DWithin de Lab2)
    private static final double DISTANCIA_PROXIMIDAD_JEFE = 50.0;

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
    public void distribuirBotin(String idPersonaje, String idItem, String idRaid, Integer costoDkp) {
        // Validar cercanía al Jefe de la Raid usando la consulta de repositorio $near (equivalente a ST_DWithin)
        Raid r = raidRepository.findById(idRaid).orElse(null);
        if (r != null && r.getUbicacionBoss() != null) {
            double lonBoss = r.getUbicacionBoss().getX();
            double latBoss = r.getUbicacionBoss().getY();

            List<Personaje> personajesCercanos = personajeRepository.findPersonajesCercanos(lonBoss, latBoss, DISTANCIA_PROXIMIDAD_JEFE);
            Set<String> idsCercanos = personajesCercanos.stream().map(Personaje::getIdPersonaje).collect(Collectors.toSet());

            if (!idsCercanos.contains(idPersonaje)) {
                throw new IllegalArgumentException("El personaje seleccionado está demasiado lejos del jefe de la Raid (máximo " + (int)DISTANCIA_PROXIMIDAD_JEFE + "m).");
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
            System.err.println("Aviso al insertar evento BOSS_DEATH en raid_events: " + e.getMessage());
        }
    }

    /**
     * Procesa la distribución automática de Loot tras la muerte del Boss en una Raid (MongoDB).
     * Reutiliza la consulta geoespacial $near de PersonajeRepository para obtener sólo personajes dentro de 50m.
     */
    @Transactional
    public void distributeBossLoot(String raidId, String clanId, String idItemExplicit, String idPersonajeExplicit) {
        if (raidId == null) return;

        Optional<Raid> raidOpt = raidRepository.findById(raidId);
        if (raidOpt.isEmpty()) {
            System.err.println("Aviso: Raid no encontrada para ID: " + raidId);
            return;
        }
        Raid raid = raidOpt.get();

        // Evitar WriteConflict: Si la raid ya fue completada (por la petición síncrona previa), omitir re-procesar
        if ("Completada".equalsIgnoreCase(raid.getEstado())) {
            System.out.println("ℹ️ Raid " + raidId + " ya fue procesada y se encuentra 'Completada'. Se omite redistribución.");
            return;
        }

        // 1. Determinar el ítem a entregar
        Item itemAEntregar = null;
        if (idItemExplicit != null && !idItemExplicit.isEmpty()) {
            itemAEntregar = itemRepository.findById(idItemExplicit).orElse(null);
        }
        if (itemAEntregar == null) {
            List<Item> itemsDisponibles = itemRepository.findAll();
            if (!itemsDisponibles.isEmpty()) {
                itemAEntregar = itemsDisponibles.get(random.nextInt(itemsDisponibles.size()));
            }
        }

        if (itemAEntregar == null) {
            System.err.println("Aviso: No hay ítems en la colección 'items' para asignar a la Raid " + raidId);
            raid.setEstado("Completada");
            raidRepository.save(raid);
            return;
        }

        Integer costoDkp = itemAEntregar.getGananciaDkp() != null ? itemAEntregar.getGananciaDkp() : 0;
        String idItemFinal = itemAEntregar.getIdItem();

        // Obtener la lista de personajes cercanos usando la consulta geoespacial del repositorio
        Set<String> idsPersonajesCercanos = Set.of();
        if (raid.getUbicacionBoss() != null) {
            List<Personaje> cercanos = personajeRepository.findPersonajesCercanos(
                    raid.getUbicacionBoss().getX(),
                    raid.getUbicacionBoss().getY(),
                    DISTANCIA_PROXIMIDAD_JEFE
            );
            idsPersonajesCercanos = cercanos.stream().map(Personaje::getIdPersonaje).collect(Collectors.toSet());
        }

        // 2. Si se especificó un personaje explícito, verificar cercanía antes de asignar
        if (idPersonajeExplicit != null && !idPersonajeExplicit.isEmpty()) {
            if (!idsPersonajesCercanos.isEmpty() && !idsPersonajesCercanos.contains(idPersonajeExplicit)) {
                System.err.println("Aviso: El personaje explícito no está cerca del jefe. Se omitirá para buscar candidatos cercanos.");
                idPersonajeExplicit = null;
            }
        }

        if (idPersonajeExplicit != null && !idPersonajeExplicit.isEmpty()) {
            try {
                raidService.distribuirBotin(idPersonajeExplicit, idItemFinal, raidId, costoDkp);
                System.out.println("✅ Botín asignado al personaje explícito ID: " + idPersonajeExplicit 
                        + " (Ítem: '" + itemAEntregar.getNombre() + "', Costo DKP: " + costoDkp + ")");
            } catch (Exception e) {
                System.err.println("Error distribuyendo botín explícito: " + e.getMessage());
            }
            raid.setEstado("Completada");
            raidRepository.save(raid);
            return;
        }

        // 3. Buscar candidatos entre los inscritos en la Raid que estén CERCA del jefe y tengan DKP suficiente
        List<InscripcionRaid> inscripciones = inscripcionRaidRepository.findByRaidId(raidId);
        if (inscripciones != null && !inscripciones.isEmpty()) {
            List<Personaje> candidatosElegibles = new ArrayList<>();

            for (InscripcionRaid ins : inscripciones) {
                if (idsPersonajesCercanos.contains(ins.getPersonajeId())) {
                    Optional<Personaje> pOpt = personajeRepository.findById(ins.getPersonajeId());
                    if (pOpt.isPresent()) {
                        Personaje p = pOpt.get();
                        if (p.getPuntosMerito() != null && p.getPuntosMerito() >= costoDkp) {
                            candidatosElegibles.add(p);
                        }
                    }
                }
            }

            if (!candidatosElegibles.isEmpty()) {
                Personaje ganador = candidatosElegibles.get(random.nextInt(candidatosElegibles.size()));
                try {
                    raidService.distribuirBotin(ganador.getIdPersonaje(), idItemFinal, raidId, costoDkp);
                    System.out.println("🎉 ¡Botín distribuido! Ganador cercano: " + ganador.getNombre() 
                            + " (ID: " + ganador.getIdPersonaje() + ") recibió '" + itemAEntregar.getNombre() 
                            + "' (DKP cobrado: " + costoDkp + ")");
                } catch (Exception e) {
                    System.err.println("Error distribuyendo botín automático: " + e.getMessage());
                }
            } else {
                System.out.println("Aviso: Ningún personaje inscrito en la raid " + raidId + " se encuentra cerca del jefe con DKP suficiente.");
            }
        }

        // 4. Actualizar el estado de la raid a Completada
        raid.setEstado("Completada");
        raidRepository.save(raid);
    }

    public void distributeBossLoot(String raidId, String clanId) {
        distributeBossLoot(raidId, clanId, null, null);
    }
}
