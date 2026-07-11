package com.grupo3.mmorpg.repositories;

import com.grupo3.mmorpg.models.InscripcionRaid;
import com.grupo3.mmorpg.models.Raid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Raid
 */
@Repository
public interface RaidRepository extends JpaRepository<Raid, Long> {

    List<Raid> findByEstadoOrderByFechaDesc(String estado);

    @Query("SELECT r FROM Raid r WHERE r.itemLevelRequerido >= :itemLevel ORDER BY r.itemLevelRequerido DESC")
    List<Raid> findByItemLevelMin(@Param("itemLevel") Integer itemLevel);

    @Query("SELECT r FROM Raid r WHERE r.estado = 'Programada' ORDER BY r.fecha ASC")
    List<Raid> findProgramadas();

    // === PROCEDIMIENTOS ALMACENADOS ===

    @Modifying
    @Query(value = "CALL sp_crear_raid_e_invitar(:nombre, :fecha, :itemLevel, :tanques, :healers, :dps)", nativeQuery = true)
    void crearRaidConInscripcionMasiva(@Param("nombre") String nombre,
                                       @Param("fecha") LocalDateTime fecha,
                                       @Param("itemLevel") Integer itemLevel,
                                       @Param("tanques") Integer tanques,
                                       @Param("healers") Integer healers,
                                       @Param("dps") Integer dps);

    @Modifying
    @Query(value = "CALL sp_distribuir_botin(:idPersonaje, :idItem, :idRaid, :costoDkp)", nativeQuery = true)
    void distribuirBotin(@Param("idPersonaje") Long idPersonaje,
                         @Param("idItem") Long idItem,
                         @Param("idRaid") Long idRaid,
                         @Param("costoDkp") Integer costoDkp);

    // === INSCRIPCIONES ===

    @Modifying
    @Query(value = "INSERT INTO Inscripcion_Raid (id_raid, id_personaje, estado, asistio) VALUES (:idRaid, :idPersonaje, 'Pendiente', FALSE)", nativeQuery = true)
    int inscribirPersonaje(@Param("idRaid") Long idRaid, @Param("idPersonaje") Long idPersonaje);

    @Modifying
    @Query(value = "DELETE FROM Inscripcion_Raid WHERE id_raid = :idRaid AND id_personaje = :idPersonaje", nativeQuery = true)
    int desinscribirPersonaje(@Param("idRaid") Long idRaid, @Param("idPersonaje") Long idPersonaje);

    @Query(value = "SELECT ir.id_inscripcion, ir.id_personaje, p.nombre, p.clase, ir.estado, ir.asistio " +
                   "FROM Inscripcion_Raid ir JOIN Personaje p ON ir.id_personaje = p.id_personaje " +
                   "WHERE ir.id_raid = :idRaid ORDER BY ir.id_inscripcion", nativeQuery = true)
    List<Object[]> getInscripcionesRaid(@Param("idRaid") Long idRaid);

    @Query(value = "SELECT COUNT(*) FROM Inscripcion_Raid WHERE id_raid = :idRaid AND id_personaje = :idPersonaje", nativeQuery = true)
    long estaPersonajeInscrito(@Param("idRaid") Long idRaid, @Param("idPersonaje") Long idPersonaje);

    @Query(value = "SELECT estado, COUNT(*) FROM Inscripcion_Raid WHERE id_raid = :idRaid GROUP BY estado", nativeQuery = true)
    List<Object[]> contarInscripcionesPorEstado(@Param("idRaid") Long idRaid);

    @Modifying
    @Query("UPDATE Raid r SET r.estado = :estado WHERE r.idRaid = :idRaid")
    int updateEstado(@Param("idRaid") Long idRaid, @Param("estado") String estado);
}
