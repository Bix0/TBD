package com.grupo3.mmorpg.services;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DataSeeder implements CommandLineRunner {

        private final JdbcTemplate jdbcTemplate;
        private final PasswordEncoder passwordEncoder;

        public DataSeeder(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
                this.jdbcTemplate = jdbcTemplate;
                this.passwordEncoder = passwordEncoder;
        }

        private Timestamp parseTimestamp(String dateStr) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return Timestamp.valueOf(LocalDateTime.parse(dateStr, formatter));
        }

        @Override
        public void run(String... args) {
                Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Jugador", Long.class);
                if (count != null && count > 0) return;

                System.out.println("=== Inyectando Universo MMORPG ===");

                // 1. Crear Jugadores
                String pw = passwordEncoder.encode("123456");
                jdbcTemplate.update("INSERT INTO Jugador (username, password, rol) VALUES (?, ?, ?)", "admin", pw, "Admin");
                jdbcTemplate.update("INSERT INTO Jugador (username, password, rol) VALUES (?, ?, ?)", "jugador2", pw, "Usuario");
                jdbcTemplate.update("INSERT INTO Jugador (username, password, rol) VALUES (?, ?, ?)", "jugador3", pw, "Usuario");

                // 2. Crear Clanes (1 Alianza, 1 Horda)
                jdbcTemplate.update("INSERT INTO Clan (nombre, id_lider) VALUES (?, ?)", "Guardia de la Alianza", 1);
                jdbcTemplate.update("INSERT INTO Clan (nombre, id_lider) VALUES (?, ?)", "Señores de la Horda", 2);

                // 3. Crear Items Base
                jdbcTemplate.update("INSERT INTO Item (id_item, nombre, item_lvl, ganancia_dkp) VALUES (?, ?, ?, ?)", 1, "Poción de Bienvenida", 5, 0);
                jdbcTemplate.update("INSERT INTO Item (id_item, nombre, item_lvl, ganancia_dkp) VALUES (?, ?, ?, ?)", 2, "Espada de Fuego", 150, 50);
                jdbcTemplate.update("INSERT INTO Item (id_item, nombre, item_lvl, ganancia_dkp) VALUES (?, ?, ?, ?)", 3, "Báculo Sagrado", 140, 45);
                jdbcTemplate.execute("SELECT setval('item_id_item_seq', 3)");

                // 4. Clases Permitidas para los Ítems
                jdbcTemplate.update("INSERT INTO Item_Clase_Permitida (id_item, clase_permitida) VALUES (1, 'Guerrero'), (1, 'Mago'), (1, 'Ranger')");
                jdbcTemplate.update("INSERT INTO Item_Clase_Permitida (id_item, clase_permitida) VALUES (2, 'Guerrero')");
                jdbcTemplate.update("INSERT INTO Item_Clase_Permitida (id_item, clase_permitida) VALUES (3, 'Mago')");

                // 5. Crear Personajes (Esto activa el Trigger  les da un objeto de bienvenidda)
                jdbcTemplate.update("INSERT INTO Personaje (id_jugador, id_clan, nombre, clase, nivel, faccion, item_level, puntos_merito, rol_clan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        1, 1, "Tato_Rey", "Guerrero", 60, "Alianza", 200, 1000, "Tanque");
                jdbcTemplate.update("INSERT INTO Personaje (id_jugador, id_clan, nombre, clase, nivel, faccion, item_level, puntos_merito, rol_clan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        2, 2, "Thrall", "Guerrero", 60, "Horda", 190, 800, "DPS");

                // 6. Crear Raids
                jdbcTemplate.update("INSERT INTO Raid (nombre, fecha, estado, item_level_requerido, cupos_tanque, cupos_healer, cupos_dps) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        "Asalto al Castillo", parseTimestamp("2026-06-10 20:00:00"), "Programada", 150, 2, 2, 6);
                jdbcTemplate.update("INSERT INTO Raid (nombre, fecha, estado, item_level_requerido, cupos_tanque, cupos_healer, cupos_dps) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        "Cueva del Dragón", parseTimestamp("2026-06-12 19:30:00"), "Programada", 100, 1, 1, 3);

                System.out.println("=== Datos inyectados con éxito ===");
        }
}