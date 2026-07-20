package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Personaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Personaje
 */
@Repository
public interface PersonajeRepository extends JpaRepository<Personaje, Long> {

    List<Personaje> findByClanIdClan(Long clanId);

    List<Personaje> findByClase(String clase);

    List<Personaje> findByRolClan(String rolClan);

    @Query("SELECT p FROM Personaje p WHERE p.itemLevel >= :minLevel ORDER BY p.itemLevel DESC")
    List<Personaje> findByItemLevelMin(@Param("minLevel") Integer minLevel);

    @Modifying
    @Query("UPDATE Personaje p SET p.puntosMerito = p.puntosMerito - :cantidad WHERE p.idPersonaje = :idPersonaje")
    int updatePuntosMerito(@Param("idPersonaje") Long idPersonaje, @Param("cantidad") Integer cantidad);

    Optional<Personaje> findFirstByJugadorIdJugador(Long jugadorId);

    List<Personaje> findByJugadorIdJugador(Long jugadorId);

    // --- LAB 2: FILTRAR HEALERS CERCANOS AL TANK ---
    @Query(value = "SELECT * FROM Personaje p WHERE (p.rol_clan ILIKE '%healer%' OR p.rol_clan ILIKE '%sanador%' OR p.clase ILIKE '%sacerdote%' OR p.clase ILIKE '%druida%' OR p.clase ILIKE '%chaman%' OR p.clase ILIKE '%paladin%') AND p.ubicacion_actual IS NOT NULL AND ST_DWithin(p.ubicacion_actual, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :distancia)", nativeQuery = true)
    List<Personaje> findHealersCercanos(@Param("lon") double lon, @Param("lat") double lat, @Param("distancia") double distancia);
}
