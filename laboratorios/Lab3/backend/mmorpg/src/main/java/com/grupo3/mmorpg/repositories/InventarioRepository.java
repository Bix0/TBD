package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Inventario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de MongoDB para la entidad Inventario
 */
@Repository
public interface InventarioRepository extends MongoRepository<Inventario, String> {

    List<Inventario> findByPersonajeId(String personajeId);

    List<Inventario> findByPersonajeIdAndEquipadoTrue(String personajeId);

    Optional<Inventario> findByPersonajeIdAndItemId(String personajeId, String itemId);

    // Métodos para verificar existencia y conteo
    boolean existsByPersonajeIdAndItemId(String personajeId, String itemId);

    long countByPersonajeId(String personajeId);
}