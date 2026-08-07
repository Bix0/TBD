package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.*;
import com.grupo3.mmorpg.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final JugadorRepository jugadorRepository;
    private final ClanRepository clanRepository;
    private final ItemRepository itemRepository;
    private final PersonajeRepository personajeRepository;
    private final RaidRepository raidRepository;
    private final InscripcionRaidRepository inscripcionRaidRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(JugadorRepository jugadorRepository,
                      ClanRepository clanRepository,
                      ItemRepository itemRepository,
                      PersonajeRepository personajeRepository,
                      RaidRepository raidRepository,
                      InscripcionRaidRepository inscripcionRaidRepository,
                      PasswordEncoder passwordEncoder) {
        this.jugadorRepository = jugadorRepository;
        this.clanRepository = clanRepository;
        this.itemRepository = itemRepository;
        this.personajeRepository = personajeRepository;
        this.raidRepository = raidRepository;
        this.inscripcionRaidRepository = inscripcionRaidRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (jugadorRepository.count() > 0) return;

        System.out.println("=== Inyectando Universo MMORPG (MongoDB) ===");

        // --- PUNTOS ESPACIALES EN MONGODB (GeoJSON: Longitud, Latitud) ---
        // ¡OJO! Las coordenadas se han escalado (/10) para no superar los límites geográficos (Lat: 90, Lng: 180)
        GeoJsonPoint baseAlianza = new GeoJsonPoint(15.0, 15.0); // Esquina Inferior Izquierda
        GeoJsonPoint baseHorda = new GeoJsonPoint(85.0, 85.0);   // Esquina Superior Derecha
        GeoJsonPoint bossCastillo = new GeoJsonPoint(80.0, 20.0); // Zona Inferior Derecha (Bot)
        GeoJsonPoint bossDragon = new GeoJsonPoint(20.0, 80.0);   // Zona Superior Izquierda (Top)

        // 1. Crear Jugadores
        String pw = passwordEncoder.encode("123456");
        Jugador admin = jugadorRepository.save(new Jugador(null, "admin", pw, "Admin"));
        Jugador jugador2 = jugadorRepository.save(new Jugador(null, "jugador2", pw, "Usuario"));
        Jugador jugador3 = jugadorRepository.save(new Jugador(null, "jugador3", pw, "Usuario"));

        // 2. Crear Clanes (Nota: idLider se manejará como String/ObjectId)
        Clan alianza = clanRepository.save(new Clan(null, "Guardia de la Alianza", admin.getIdJugador(), "Alianza", baseAlianza));
        Clan horda = clanRepository.save(new Clan(null, "Señores de la Horda", jugador2.getIdJugador(), "Horda", baseHorda));

        // 3. Crear Items Base (Con clases permitidas embebidas directamente)
        Item pocion = itemRepository.save(new Item(null, "Poción de Bienvenida", 5, 0, List.of("Guerrero", "Mago", "Ranger")));
        Item espada = itemRepository.save(new Item(null, "Espada de Fuego", 150, 50, List.of("Guerrero")));
        Item baculo = itemRepository.save(new Item(null, "Báculo Sagrado", 140, 45, List.of("Mago")));

        // 4. Crear Personajes
        personajeRepository.save(new Personaje(null, admin.getIdJugador(), alianza.getIdClan(),
                "Tato_Rey", "Guerrero", 60, "Alianza", 200, 1000, "Tanque", baseAlianza, "Base Alianza"));
        personajeRepository.save(new Personaje(null, jugador2.getIdJugador(), horda.getIdClan(),
                "Thrall", "Guerrero", 60, "Horda", 190, 800, "DPS", baseHorda, "Base Horda"));

        // 5. Crear Raids
        Raid raidCastillo = raidRepository.save(new Raid(null, "Asalto al Castillo",
                LocalDateTime.of(2026, 6, 10, 20, 0), "Finalizada", 150, 2, 2, 6, 45, bossCastillo)); // 45 minutos
        Raid raidDragon = raidRepository.save(new Raid(null, "Cueva del Dragón",
                LocalDateTime.of(2026, 6, 12, 19, 30), "Finalizada", 100, 1, 1, 3, 30, bossDragon)); // 30 minutos

        // 6. Crear Inscripciones para simular el desempeño en las Raids
        Personaje p1 = personajeRepository.findByNombre("Tato_Rey").orElse(null);
        Personaje p2 = personajeRepository.findByNombre("Thrall").orElse(null);

        if (p1 != null && p2 != null) {
            inscripcionRaidRepository.save(new InscripcionRaid(null, raidCastillo.getIdRaid(), p1.getIdPersonaje(), "Aprobada", true, 50000));
            inscripcionRaidRepository.save(new InscripcionRaid(null, raidCastillo.getIdRaid(), p2.getIdPersonaje(), "Aprobada", true, 45000));
            
            inscripcionRaidRepository.save(new InscripcionRaid(null, raidDragon.getIdRaid(), p1.getIdPersonaje(), "Aprobada", true, 60000));
            // p2 faltó a la segunda raid
            inscripcionRaidRepository.save(new InscripcionRaid(null, raidDragon.getIdRaid(), p2.getIdPersonaje(), "Aprobada", false, 0));
        }

        System.out.println("=== Datos inyectados con éxito en MongoDB ===");
    }
}