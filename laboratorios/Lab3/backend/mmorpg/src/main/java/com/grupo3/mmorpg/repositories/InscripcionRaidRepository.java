package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.InscripcionRaid;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de MongoDB para la entidad InscripcionRaid.
 * El indice unico compuesto raid_personaje_unique_idx (definido en la entidad)
 * garantiza que un personaje no pueda inscribirse dos veces a la misma raid.
 */
@Repository
public interface InscripcionRaidRepository extends MongoRepository<InscripcionRaid, String> {

    List<InscripcionRaid> findByRaidId(String raidId);

    Optional<InscripcionRaid> findByRaidIdAndPersonajeId(String raidId, String personajeId);

    boolean existsByRaidIdAndPersonajeId(String raidId, String personajeId);

    long countByRaidId(String raidId);

    void deleteByRaidIdAndPersonajeId(String raidId, String personajeId);
}
