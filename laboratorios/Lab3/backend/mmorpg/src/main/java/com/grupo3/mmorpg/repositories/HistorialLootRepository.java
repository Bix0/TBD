package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.HistorialLoot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de MongoDB para la entidad HistorialLoot.
 * El indice compuesto raid_personaje_loot_idx (definido en la entidad) acelera
 * las consultas de botin por raid y por personaje.
 */
@Repository
public interface HistorialLootRepository extends MongoRepository<HistorialLoot, String> {

    List<HistorialLoot> findByRaidId(String raidId);

    List<HistorialLoot> findByPersonajeId(String personajeId);

    List<HistorialLoot> findByPersonajeIdIn(List<String> personajeIds);
}
