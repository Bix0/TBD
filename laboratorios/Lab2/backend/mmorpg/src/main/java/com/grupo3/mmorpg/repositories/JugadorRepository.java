package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Jugador
 */
@Repository
public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    Optional<Jugador> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query(value = """
        SELECT h.fecha, p.nombre AS personaje, i.nombre AS item,
               r.nombre AS raid, h.estado_loot
        FROM historial_loot h
        JOIN personaje p ON h.id_personaje = p.id_personaje
        JOIN item i ON h.id_item = i.id_item
        JOIN raid r ON h.id_raid = r.id_raid
        WHERE p.id_jugador = :idJugador
        ORDER BY h.fecha DESC
        """, nativeQuery = true)
    List<Object[]> obtenerHistorialBotinJugador(@Param("idJugador") Long idJugador);
}
