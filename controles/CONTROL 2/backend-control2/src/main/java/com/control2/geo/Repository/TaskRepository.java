package com.control2.geo.Repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.control2.geo.Entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

        // Consulta con QUERY lo cual hace que primero, busca por estado y luego busca
        // en Titulo y Descripcion si contiene la palabra buscada
        @Query("SELECT t FROM Task t WHERE " +
                        "t.status = :status AND " +
                        "(:keyword IS NULL OR LOWER(t.title) LIKE :keyword OR LOWER(t.description) LIKE :keyword)")
        List<Task> searchTasks(@Param("status") String status, @Param("keyword") String keyword);

        @Query(value = "SELECT t.* " +
                        "FROM task t " +
                        "JOIN geo_point gp_task ON t.id_geo_point = gp_task.id_geo_point " +
                        "CROSS JOIN users u " +
                        "JOIN geo_point gp_user ON u.id_geo_point = gp_user.id_geo_point " +
                        "WHERE u.id_user = :idUser " +
                        "AND t.status = 'Pendiente' " +
                        "ORDER BY ST_Distance(gp_task.point::geography, gp_user.point::geography) ASC " +
                        "LIMIT 1", nativeQuery = true)
        Task findClosestPendingTask(@Param("idUser") Long idUser);

        @Query(value = "SELECT gp_task.sector AS sector, COUNT(t.id_task) AS total_tareas " +
                        "FROM task t " +
                        "JOIN geo_point gp_task ON t.id_geo_point = gp_task.id_geo_point " +
                        "CROSS JOIN users u " +
                        "JOIN geo_point gp_user ON u.id_geo_point = gp_user.id_geo_point " +
                        "WHERE u.id_user = :idUser " +
                        "AND t.status = 'Completada' " +
                        "AND ST_DWithin(gp_task.point::geography, gp_user.point::geography, :radiusInMeters) " +
                        "GROUP BY gp_task.sector " +
                        "ORDER BY total_tareas DESC " +
                        "LIMIT 1", nativeQuery = true)
        Map<String, Object> findSectorWithMostCompletedTasksWithinRadius(
                        @Param("idUser") Long idUser,
                        @Param("radiusInMeters") double radiusInMeters);

        @Query(value = "SELECT COALESCE(AVG(ST_Distance(gp_task.point::geography, gp_user.point::geography)), 0) " +
                        "FROM user_tasks ut " +
                        "JOIN task t ON ut.id_task = t.id_task " +
                        "JOIN geo_point gp_task ON t.id_geo_point = gp_task.id_geo_point " +
                        "CROSS JOIN users u " +
                        "JOIN geo_point gp_user ON u.id_geo_point = gp_user.id_geo_point " +
                        "WHERE u.id_user = :idUser", nativeQuery = true)
        Double getAverageDistanceOfCompletedTasks(@Param("idUser") Long idUser);

        @Query(value = "SELECT gp.sector AS sector, COUNT(t.id_task) AS total_pendientes " +
                        "FROM task t " +
                        "JOIN geo_point gp ON t.id_geo_point = gp.id_geo_point " +
                        "WHERE t.status = 'Pendiente' " +
                        "GROUP BY gp.sector " +
                        "ORDER BY total_pendientes DESC", nativeQuery = true)
        List<Map<String, Object>> getPendingTasksConcentrationPerSector();

        @Query(value = "SELECT gp.sector AS sector, COUNT(ut.id_task) AS total_tareas " +
                        "FROM user_tasks ut " +
                        "JOIN task t ON ut.id_task = t.id_task " +
                        "JOIN geo_point gp ON t.id_geo_point = gp.id_geo_point " +
                        "WHERE ut.id_user = :idUser " +
                        "GROUP BY gp.sector " +
                        "ORDER BY total_tareas DESC", nativeQuery = true)
        List<Map<String, Object>> countTasksByUserAndSector(@Param("idUser") Long idUser);
}
