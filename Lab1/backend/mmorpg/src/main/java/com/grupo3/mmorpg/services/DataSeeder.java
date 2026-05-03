package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.repositories.JugadorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Puebla la base de datos con datos de prueba al iniciar la aplicación.
 * Las contraseñas se hashean con BCrypt antes de insertarse.
 */
@Component
public class DataSeeder implements CommandLineRunner {

        private final JugadorRepository jugadorRepository;
        private final JdbcTemplate jdbcTemplate;
        private final PasswordEncoder passwordEncoder;

        public DataSeeder(JugadorRepository jugadorRepository, JdbcTemplate jdbcTemplate,
                        PasswordEncoder passwordEncoder) {
                this.jugadorRepository = jugadorRepository;
                this.jdbcTemplate = jdbcTemplate;
                this.passwordEncoder = passwordEncoder;
        }

        /**
         * Convierte un String "yyyy-MM-dd HH:mm:ss" a java.sql.Timestamp
         * para que PostgreSQL lo acepte en columnas TIMESTAMP.
         */
        private Timestamp parseTimestamp(String dateStr) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime ldt = LocalDateTime.parse(dateStr, formatter);
                return Timestamp.valueOf(ldt);
        }

        @Override
        public void run(String... args) {
                // Solo insertar datos si la tabla Jugador está vacía
                Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Jugador", Long.class);
                if (count != null && count > 0) {
                        return; // Ya hay datos, no duplicar
                }

                System.out.println("=== Insertando datos de prueba con BCrypt ===");

                // 1. Crear jugadores con contraseñas hasheadas
                String adminPw = passwordEncoder.encode("123456");
                String userPw = passwordEncoder.encode("123456");

                jdbcTemplate.update("INSERT INTO Jugador (username, password, rol) VALUES (?, ?, ?)",
                                "bruno", adminPw, "Admin");
                jdbcTemplate.update("INSERT INTO Jugador (username, password, rol) VALUES (?, ?, ?)",
                                "xhuala", userPw, "Usuario");

                // 2. Crear clan (id_lider = 1 que es bruno)
                jdbcTemplate.update("INSERT INTO Clan (nombre, id_lider) VALUES (?, ?)",
                                "Los Cruzados", 1);

                // 3. Crear personajes
                jdbcTemplate.update(
                                "INSERT INTO Personaje (id_jugador, id_clan, nombre, clase, nivel, faccion, item_level, puntos_merito, rol_clan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                1, 1, "Thorin", "Guerrero", 80, "Alianza", 210, 500, "Raider");
                jdbcTemplate.update(
                                "INSERT INTO Personaje (id_jugador, id_clan, nombre, clase, nivel, faccion, item_level, puntos_merito, rol_clan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                2, 1, "Elara", "Mago", 80, "Alianza", 190, 300, "Raider");

                // 4. Crear Raids (usando Timestamp en vez de String para la fecha)
                jdbcTemplate.update(
                                "INSERT INTO Raid (nombre, fecha, estado, item_level_requerido, cupos_tanque, cupos_healer, cupos_dps) VALUES (?, ?, ?, ?, ?, ?, ?)",
                                "La Caída del Rey Exánime", parseTimestamp("2026-05-10 20:00:00"), "Programada", 200, 2,
                                4, 14);
                jdbcTemplate.update(
                                "INSERT INTO Raid (nombre, fecha, estado, item_level_requerido, cupos_tanque, cupos_healer, cupos_dps) VALUES (?, ?, ?, ?, ?, ?, ?)",
                                "Guarida de Onyxia", parseTimestamp("2026-05-12 19:30:00"), "Programada", 150, 1, 2, 7);
                jdbcTemplate.update(
                                "INSERT INTO Raid (nombre, fecha, estado, item_level_requerido, cupos_tanque, cupos_healer, cupos_dps) VALUES (?, ?, ?, ?, ?, ?, ?)",
                                "Núcleo de Magma", parseTimestamp("2026-05-15 21:00:00"), "En Curso", 180, 0, 5, 13);
                jdbcTemplate.update(
                                "INSERT INTO Raid (nombre, fecha, estado, item_level_requerido, cupos_tanque, cupos_healer, cupos_dps) VALUES (?, ?, ?, ?, ?, ?, ?)",
                                "Templo Oscuro", parseTimestamp("2026-05-20 18:00:00"), "Programada", 210, 2, 0, 10);
                jdbcTemplate.update(
                                "INSERT INTO Raid (nombre, fecha, estado, item_level_requerido, cupos_tanque, cupos_healer, cupos_dps) VALUES (?, ?, ?, ?, ?, ?, ?)",
                                "Ulduar", parseTimestamp("2026-05-25 21:30:00"), "Completada", 220, 2, 5, 18);

                System.out.println("=== Datos de prueba insertados correctamente ===");
        }
}
