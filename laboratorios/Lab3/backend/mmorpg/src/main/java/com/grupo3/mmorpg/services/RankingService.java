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
}