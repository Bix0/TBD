package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Jugador;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de MongoDB para la colección Jugadores
 */
@Repository
public interface JugadorRepository extends MongoRepository<Jugador, String> {

    // Spring Data MongoDB es lo suficientemente inteligente para entender
    // estas consultas derivadas por nombre igual que JPA.
    Optional<Jugador> findByUsername(String username);

    boolean existsByUsername(String username);

    /*
      NOTA: El método obtenerHistorialBotinJugador() con @Query(nativeQuery = true)
      fue removido temporalmente. MongoDB no usa SQL ni JOINs relacionales directos.
      Más adelante reconstruiremos esta lógica usando Aggregation Pipelines ($lookup).
    */
}