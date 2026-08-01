package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Raid;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de MongoDB para la entidad Raid
 */
@Repository
public interface RaidRepository extends MongoRepository<Raid, String> {

    List<Raid> findByEstadoOrderByFechaDesc(String estado);

    @Query("{ 'itemLevelRequerido': { $gte: ?0 } }")
    List<Raid> findByItemLevelMin(Integer itemLevel);

    @Query("{ 'estado': 'Programada' }")
    List<Raid> findProgramadas();

    // ==========================================
    // CONSULTAS GEOESPACIALES PARA RAIDS (2DSPHERE)
    // ==========================================

    /**
     * Equivalente a la búsqueda espacial de jefes de Raid cercanos en el mapa.
     * Utiliza el operador $near junto con un índice 2dsphere.
     */
    @Query("{ " +
            "  'ubicacionBoss': { " +
            "    $near: { " +
            "      $geometry: { type: 'Point', coordinates: [ ?0, ?1 ] }, " +
            "      $maxDistance: ?2 " +
            "    } " +
            "  } " +
            "}")
    List<Raid> findRaidsCercanas(double longitud, double latitud, double distanciaMetros);
}