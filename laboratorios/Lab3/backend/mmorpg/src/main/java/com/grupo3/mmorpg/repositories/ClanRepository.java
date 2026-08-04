package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Clan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de MongoDB para la entidad Clan
 */
@Repository
public interface ClanRepository extends MongoRepository<Clan, String> {

    Optional<Clan> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    // Búsqueda del ID del líder de un clan
    @Query(value = "{ '_id': ?0 }", fields = "{ 'idLider': 1 }")
    Optional<String> findIdLiderByIdClan(String idClan);

    // ==========================================
    // CONSULTAS GEOESPACIALES Y DE MAPA (MONGODB)
    // ==========================================

    /**
     * Clanes cercanos aplicando operador $near sobre el campo geoespacial 2dsphere y filtro opcional por facción.
     */
    @Query("{ " +
            "  'ubicacion': { " +
            "    $near: { " +
            "      $geometry: { type: 'Point', coordinates: [ ?0, ?1 ] }, " +
            "      $maxDistance: ?2 " +
            "    } " +
            "  }, " +
            "  $and: [ " +
            "    { $expr: { $or: [ { $eq: [ ?3, null ] }, { $regexMatch: { input: '$faccion', regex: ?3, options: 'i' } } ] } } " +
            "  ] " +
            "}")
    List<Clan> findClanesCercanos(double longitud, double latitud, double distanciaMetros, String faccion);

    /**
     * Búsqueda general de clanes cercanos para proyecciones personalizadas
     */
    @Query("{ " +
            "  'ubicacion': { " +
            "    $near: { " +
            "      $geometry: { type: 'Point', coordinates: [ ?0, ?1 ] }, " +
            "      $maxDistance: ?2 " +
            "    } " +
            "  }, " +
            "  'ubicacion': { $ne: null } " +
            "}")
    List<Clan> findClanesCercanosCustom(double longitud, double latitud, double distanciaMetros);

    /**
     * Mapa de calor de clanes (en MongoDB se puede consultar directo de la colección de clanes o colección calculada)
     */
    @Query(value = "{ 'ubicacion': { $ne: null } }")
    List<Clan> obtenerMapaCalorClanes();
}