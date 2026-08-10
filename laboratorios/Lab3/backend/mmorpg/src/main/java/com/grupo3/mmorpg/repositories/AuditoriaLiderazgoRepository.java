package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.AuditoriaLiderazgo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de MongoDB para la auditoría de cambios de liderazgo (Trigger 2 / Lab1).
 * Colección: auditoria_liderazgo
 */
@Repository
public interface AuditoriaLiderazgoRepository
    extends MongoRepository<AuditoriaLiderazgo, String> {

    List<AuditoriaLiderazgo> findAllByOrderByFechaCambioDesc();
}
