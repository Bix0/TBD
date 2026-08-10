package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.Personaje;
import com.grupo3.mmorpg.repositories.PersonajeRepository;
import com.grupo3.mmorpg.repositories.RaidRepository;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MergeOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para operaciones relacionadas con el Ranking en MongoDB
 */
@Service
public class RankingService {

    private final PersonajeRepository personajeRepository;
    private final RaidRepository raidRepository;
    private final MongoTemplate mongoTemplate;

    public RankingService(PersonajeRepository personajeRepository, RaidRepository raidRepository, MongoTemplate mongoTemplate) {
        this.personajeRepository = personajeRepository;
        this.raidRepository = raidRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Obtiene el ranking actual de personajes usando ordenamiento directo
     */
    public List<Map<String, Object>> obtenerRanking() {
        List<Personaje> personajes = personajeRepository.findAll(Sort.by(Sort.Direction.DESC, "puntosMerito"));
        return personajes.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id_personaje", p.getIdPersonaje());
            map.put("nombre", p.getNombre());
            map.put("clase", p.getClase());
            map.put("rol_clan", p.getRolClan());
            map.put("dkp_actual", p.getPuntosMerito());
            map.put("total_raids_asistidas", 0);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * Refresca la colección materializada 'clanes_top_ranking' en MongoDB
     */
    public boolean actualizarRanking() {
        actualizarRankingMaterializadoClanes();
        return true;
    }

    /**
     * Actualiza la colección materializada de clanes top en MongoDB mediante $merge pipeline
     */
    public void actualizarRankingMaterializadoClanes() {
        try {
            GroupOperation groupByClan = Aggregation.group("clanId")
                    .sum("puntosMerito").as("puntosTotales")
                    .count().as("totalMiembros")
                    .avg("puntosMerito").as("dkpPromedio");

            SortOperation sortByPuntos = Aggregation.sort(Sort.by(Sort.Direction.DESC, "puntosTotales"));

            MergeOperation mergeToRanking = Aggregation.merge()
                    .intoCollection("clanes_top_ranking")
                    .build();

            Aggregation pipeline = Aggregation.newAggregation(
                    // Excluir personajes sin clan: un clanId nulo rompería el $merge (error 51132)
                    Aggregation.match(Criteria.where("clanId").ne(null)),
                    groupByClan,
                    sortByPuntos,
                    mergeToRanking
            );

            mongoTemplate.aggregate(pipeline, "personajes", Document.class);
        } catch (Exception e) {
            System.err.println("Aviso: No se pudo actualizar la colección materializada MongoDB 'clanes_top_ranking': " + e.getMessage());
        }
    }

    /**
     * Obtiene el ranking con un límite de resultados
     */
    public List<Map<String, Object>> obtenerRankingConLimite(Integer limite) {
        List<Personaje> personajes = personajeRepository.findAll(Sort.by(Sort.Direction.DESC, "puntosMerito"));
        return personajes.stream().limit(limite).map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id_personaje", p.getIdPersonaje());
            map.put("nombre", p.getNombre());
            map.put("clase", p.getClase());
            map.put("dkp_actual", p.getPuntosMerito());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene el top N personajes por puntos de mérito (DKP)
     */
    public List<Map<String, Object>> obtenerTopPersonajes(Integer top) {
        return obtenerRankingConLimite(top);
    }

    /**
     * Obtiene el ranking filtrado por clase
     */
    public List<Map<String, Object>> obtenerRankingPorClase(String clase) {
        List<Personaje> personajes = personajeRepository.findByClase(clase);
        return personajes.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id_personaje", p.getIdPersonaje());
            map.put("nombre", p.getNombre());
            map.put("clase", p.getClase());
            map.put("dkp_actual", p.getPuntosMerito());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene el ranking filtrado por DKP mínimo usando expresiones de Mongo
     */
    public List<Map<String, Object>> obtenerRankingPorDkpMin(Integer dkpMinimo) {
        List<Personaje> personajes = personajeRepository.findAll();
        return personajes.stream()
                .filter(p -> p.getPuntosMerito() != null && p.getPuntosMerito() >= dkpMinimo)
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id_personaje", p.getIdPersonaje());
                    map.put("nombre", p.getNombre());
                    map.put("dkp_actual", p.getPuntosMerito());
                    return map;
                }).collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas del ranking usando Aggregation Pipeline de MongoDB
     */
    public Map<String, Object> obtenerEstadisticasRanking() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group()
                        .count().as("total")
                        .sum("puntosMerito").as("raids_totales")
                        .avg("puntosMerito").as("dkp_promedio")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "personajes", Map.class);
        if (!results.getMappedResults().isEmpty()) {
            return results.getMappedResults().get(0);
        }

        Map<String, Object> emptyStats = new HashMap<>();
        emptyStats.put("total", 0);
        emptyStats.put("raids_totales", 0);
        emptyStats.put("dkp_promedio", 0.0);
        return emptyStats;
    }

    /**
     * Requerimiento 4: Pipeline de agregación para calcular el Ranking de Clanes
     * basado en el desempeño en raids (tiempo de finalización, asistencia, y daño total).
     */
    public List<Map> obtenerRankingClanesPorDesempeno() {
        // Etapa 1: Proyección para convertir IDs de String a ObjectId para los lookups
        // y mapear boolean 'asistio' a número 1 o 0 para poder sumarlo
        ProjectionOperation convertIdsAndProject = Aggregation.project("danoTotal")
                // OJO: ConvertOperators no agrega el prefijo '$'; hay que pasarlo explícito
                // para que $toObjectId use la referencia al campo y no el literal.
                .and(ConvertOperators.ToObjectId.toObjectId("$personajeId")).as("personajeIdObj")
                .and(ConvertOperators.ToObjectId.toObjectId("$raidId")).as("raidIdObj")
                .and(ConditionalOperators.when(Criteria.where("asistio").is(true)).then(1).otherwise(0)).as("asistenciaNum");

        // Etapa 2: Lookup con personajes para obtener de qué clan es el jugador
        LookupOperation lookupPersonaje = Aggregation.lookup("personajes", "personajeIdObj", "_id", "personaje");
        UnwindOperation unwindPersonaje = Aggregation.unwind("personaje", true);

        // Etapa 3: Convertir clanId de personaje a ObjectId
        ProjectionOperation projectClanId = Aggregation.project("danoTotal", "raidIdObj", "asistenciaNum", "personaje")
                .and(ConvertOperators.ToObjectId.toObjectId("$personaje.clanId")).as("clanIdObj");

        // Etapa 4: Lookup con clanes para obtener el nombre del clan
        LookupOperation lookupClan = Aggregation.lookup("clanes", "clanIdObj", "_id", "clan");
        UnwindOperation unwindClan = Aggregation.unwind("clan", true);

        // Etapa 5: Lookup con raids para obtener el tiempo de finalización
        LookupOperation lookupRaid = Aggregation.lookup("raids", "raidIdObj", "_id", "raid");
        UnwindOperation unwindRaid = Aggregation.unwind("raid", true);

        // Etapa 6: Agrupar por el ID del Clan
        GroupOperation groupOperation = Aggregation.group("clan._id")
                .first("clan.nombre").as("nombreClan")
                .sum("danoTotal").as("danoTotal")
                .sum("asistenciaNum").as("asistenciaTotal")
                .avg("raid.tiempoFinalizacionMinutos").as("tiempoPromedioRaid");

        // Etapa 7: Ordenar por Daño Total (Descendente) y luego por Tiempo (Ascendente)
        SortOperation sortOperation = Aggregation.sort(
                Sort.by(Sort.Direction.DESC, "danoTotal")
                    .and(Sort.by(Sort.Direction.ASC, "tiempoPromedioRaid")));

        Aggregation pipeline = Aggregation.newAggregation(
                convertIdsAndProject,
                lookupPersonaje, unwindPersonaje,
                projectClanId,
                lookupClan, unwindClan,
                lookupRaid, unwindRaid,
                groupOperation,
                sortOperation
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(pipeline, "inscripciones_raid", Map.class);
        return results.getMappedResults();
    }
}