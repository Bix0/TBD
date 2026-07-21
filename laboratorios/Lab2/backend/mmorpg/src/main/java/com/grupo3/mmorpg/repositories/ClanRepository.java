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

    // Auditoría de liderazgo: join nativo para obtener nombres + coordenadas
    @Query(value = """
        SELECT a.id_auditoria, c.nombre AS clan,
               COALESCE(p1.nombre, 'Nadie') AS antiguo,
               p2.nombre AS nuevo, a.fecha_cambio,
               ST_Y(a.ubicacion_suceso::geometry) as lat,
               ST_X(a.ubicacion_suceso::geometry) as lon
        FROM auditoria_liderazgo a
        JOIN clan c ON a.id_clan = c.id_clan
        LEFT JOIN personaje p1 ON a.id_antiguo_lider = p1.id_personaje
        JOIN personaje p2 ON a.id_nuevo_lider = p2.id_personaje
        ORDER BY a.fecha_cambio DESC
        """, nativeQuery = true)
    List<Object[]> obtenerAuditoriaLiderazgo();

    // --- NUEVA LÓGICA GEOESPACIAL (LAB 2) ---
    @Query(value = "SELECT * FROM Clan c WHERE c.ubicacion IS NOT NULL AND ST_DWithin(c.ubicacion, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :distancia) AND (:faccion IS NULL OR LOWER(c.faccion) = LOWER(:faccion))", nativeQuery = true)
    List<Clan> findClanesCercanos(@Param("lon") double lon, @Param("lat") double lat, @Param("distancia") double distancia, @Param("faccion") String faccion);

    // --- MAPA DE CALOR (LAB 2) ---
    // Extrae las coordenadas separadas (lat, lon) y el dkp total desde la vista materializada
    @Query(value = "SELECT id_clan, nombre, ST_Y(ubicacion::geometry) as lat, ST_X(ubicacion::geometry) as lon, dkp_total_clan FROM mv_calor_clanes WHERE ubicacion IS NOT NULL", nativeQuery = true)
    List<Object[]> obtenerMapaCalorClanes();
}
