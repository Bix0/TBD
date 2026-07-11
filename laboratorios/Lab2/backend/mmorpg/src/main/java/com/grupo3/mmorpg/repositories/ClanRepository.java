package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.Clan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Clan
 */
@Repository
public interface ClanRepository extends JpaRepository<Clan, Long> {

    Optional<Clan> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    @Modifying
    @Query("UPDATE Clan c SET c.idLider = :nuevoLider WHERE c.idClan = :idClan")
    int updateLider(@Param("idClan") Long idClan, @Param("nuevoLider") Long nuevoLider);

    @Query("SELECT c.idLider FROM Clan c WHERE c.idClan = :idClan")
    Optional<Long> findIdLiderByIdClan(@Param("idClan") Long idClan);

    // Auditoría de liderazgo: join nativo para obtener nombres
    @Query(value = """
        SELECT a.id_auditoria, c.nombre AS clan,
               COALESCE(p1.nombre, 'Nadie') AS antiguo,
               p2.nombre AS nuevo, a.fecha_cambio
        FROM auditoria_liderazgo a
        JOIN clan c ON a.id_clan = c.id_clan
        LEFT JOIN personaje p1 ON a.id_antiguo_lider = p1.id_personaje
        JOIN personaje p2 ON a.id_nuevo_lider = p2.id_personaje
        ORDER BY a.fecha_cambio DESC
        """, nativeQuery = true)
    List<Object[]> obtenerAuditoriaLiderazgo();
}
