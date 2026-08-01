package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Personaje;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de MongoDB para la entidad Personaje con soporte geoespacial (2dsphere)
 */
@Repository
public interface PersonajeRepository extends MongoRepository<Personaje, String> {

    List<Personaje> findByClanId(String clanId);

    List<Personaje> findByClase(String clase);

    List<Personaje> findByRolClan(String rolClan);

    // En Mongo podemos usar la nomenclatura de métodos de Spring o un @Query.
    // Usamos nomenclatura directa para que ordene automáticamente.
    List<Personaje> findByItemLevelGreaterThanEqualOrderByItemLevelDesc(Integer minLevel);

    Optional<Personaje> findFirstByJugadorId(String jugadorId);

    List<Personaje> findByJugadorId(String jugadorId);

    // ==========================================
    // CONSULTAS GEOESPACIALES Y DE MAPA (MONGODB)
    // ==========================================

    /**
     * Equivalente al ST_DWithin de PostGIS.
     * Usa el operador $near y $maxDistance (en metros para índices 2dsphere).
     */
    @Query("{ " +
            "  'ubicacionActual': { " +
            "    $near: { " +
            "      $geometry: { type: 'Point', coordinates: [ ?0, ?1 ] }, " +
            "      $maxDistance: ?2 " +
            "    } " +
            "  }, " +
            "  $or: [ " +
            "    { 'rolClan': { $regex: 'healer', $options: 'i' } }, " +
            "    { 'rolClan': { $regex: 'sanador', $options: 'i' } }, " +
            "    { 'clase': { $regex: 'sacerdote', $options: 'i' } }, " +
            "    { 'clase': { $regex: 'druida', $options: 'i' } }, " +
            "    { 'clase': { $regex: 'chaman', $options: 'i' } }, " +
            "    { 'clase': { $regex: 'paladin', $options: 'i' } } " +
            "  ] " +
            "}")
    List<Personaje> findHealersCercanos(double longitud, double latitud, double distanciaMetros);

    /**
     * Personajes activos en el mapa (tienen coordenadas)
     * Equivalente a "IS NOT NULL" en SQL
     */
    @Query("{ 'ubicacionActual': { $ne: null } }")
    List<Personaje> findAllConUbicacion();

    /**
     * Personajes activos en el mapa filtrados por rol
     */
    @Query("{ 'ubicacionActual': { $ne: null }, 'rolClan': { $regex: ?0, $options: 'i' } }")
    List<Personaje> findByRolClanIgnoreCaseAndUbicacionNotNull(String rol);
}