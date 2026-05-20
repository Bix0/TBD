package com.control2.geo.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.control2.geo.Entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);

    @Query(value = "SELECT u.user_name AS usuario, gp.sector AS sector, COUNT(ut.id_task) AS total_tareas " +
            "FROM users u " +
            "LEFT JOIN user_tasks ut ON u.id_user = ut.id_user " +
            "LEFT JOIN task t ON ut.id_task = t.id_task " +
            "LEFT JOIN geo_point gp ON t.id_geo_point = gp.id_geo_point " +
            "GROUP BY u.id_user, u.user_name, gp.sector " +
            "ORDER BY u.user_name ASC, total_tareas DESC", nativeQuery = true)
    List<Map<String, Object>> countTasksPerUserAndSector();
}
