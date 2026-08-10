package com.grupo3.mmorpg.services;

import com.grupo3.mmorpg.models.*;
import com.grupo3.mmorpg.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final JugadorRepository jugadorRepository;
    private final ClanRepository clanRepository;
    private final ItemRepository itemRepository;
    private final ItemClasePermitidaRepository itemClasePermitidaRepository;
    private final PersonajeRepository personajeRepository;
    private final RaidRepository raidRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(JugadorRepository jugadorRepository,
                      ClanRepository clanRepository,
                      ItemRepository itemRepository,
                      ItemClasePermitidaRepository itemClasePermitidaRepository,
                      PersonajeRepository personajeRepository,
                      RaidRepository raidRepository,
                      PasswordEncoder passwordEncoder) {
        this.jugadorRepository = jugadorRepository;
        this.clanRepository = clanRepository;
        this.itemRepository = itemRepository;
        this.itemClasePermitidaRepository = itemClasePermitidaRepository;
        this.personajeRepository = personajeRepository;
        this.raidRepository = raidRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (jugadorRepository.count() > 0) return;

        System.out.println("=== Inyectando Universo MMORPG ===");

        // --- INYECCIÓN DE COORDENADAS ESPACIALES (MAPA 2D: 0 a 1000) ---
        // Mantenemos SRID 4326 por si la base de datos lo requiere, pero usamos coordenadas cartesianas
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        // Creamos puntos estratégicos en el mapa (X, Y)
        Point baseAlianza = geometryFactory.createPoint(new Coordinate(150, 150)); // Esquina Inferior Izquierda
        Point baseHorda = geometryFactory.createPoint(new Coordinate(850, 850));   // Esquina Superior Derecha
        Point bossCastillo = geometryFactory.createPoint(new Coordinate(800, 200)); // Zona Inferior Derecha (Bot)
        Point bossDragon = geometryFactory.createPoint(new Coordinate(200, 800));   // Zona Superior Izquierda (Top)
        Point medioMapa = geometryFactory.createPoint(new Coordinate(500, 500));    // Centro (Mid/Río)

        // 1. Crear Jugadores
        String pw = passwordEncoder.encode("123456");
        Jugador admin = jugadorRepository.save(new Jugador(null, "admin", pw, "Admin"));
        Jugador jugador2 = jugadorRepository.save(new Jugador(null, "jugador2", pw, "Usuario"));
        Jugador jugador3 = jugadorRepository.save(new Jugador(null, "jugador3", pw, "Usuario"));

        // 2. Crear Clanes
        Clan alianza = clanRepository.save(new Clan(null, "Guardia de la Alianza", 1L, "Alianza", baseAlianza));
        Clan horda = clanRepository.save(new Clan(null, "Señores de la Horda", 2L, "Horda", baseHorda));

        // 3. Crear Items Base
        Item pocion = itemRepository.save(new Item(null, "Poción de Bienvenida", 5, 0));
        Item espada = itemRepository.save(new Item(null, "Espada de Fuego", 150, 50));
        Item baculo = itemRepository.save(new Item(null, "Báculo Sagrado", 140, 45));

        // 4. Clases Permitidas
        itemClasePermitidaRepository.save(new ItemClasePermitida(
                new ItemClasePermitida.ItemClasePermitidaId(pocion.getIdItem(), "Guerrero"), pocion));
        itemClasePermitidaRepository.save(new ItemClasePermitida(
                new ItemClasePermitida.ItemClasePermitidaId(pocion.getIdItem(), "Mago"), pocion));
        itemClasePermitidaRepository.save(new ItemClasePermitida(
                new ItemClasePermitida.ItemClasePermitidaId(pocion.getIdItem(), "Ranger"), pocion));
        itemClasePermitidaRepository.save(new ItemClasePermitida(
                new ItemClasePermitida.ItemClasePermitidaId(espada.getIdItem(), "Guerrero"), espada));
        itemClasePermitidaRepository.save(new ItemClasePermitida(
                new ItemClasePermitida.ItemClasePermitidaId(baculo.getIdItem(), "Mago"), baculo));

        // 5. Crear Personajes (Sin clan inicial, spawneando en las bases principales de su facción)
        personajeRepository.save(new Personaje(null, admin, null,
                "Tato_Rey", "Guerrero", 60, "Alianza", 200, 1000, "Tanque", baseAlianza, "Base Alianza"));
        personajeRepository.save(new Personaje(null, jugador2, null,
                "Thrall", "Guerrero", 60, "Horda", 190, 800, "DPS", baseHorda, "Base Horda"));

        // 6. Crear Raids (Asignamos coordenadas separadas)
        raidRepository.save(new Raid(null, "Asalto al Castillo",
                LocalDateTime.of(2026, 6, 10, 20, 0), "Programada", 150, 2, 2, 6, bossCastillo));
        raidRepository.save(new Raid(null, "Cueva del Dragón",
                LocalDateTime.of(2026, 6, 12, 19, 30), "Programada", 100, 1, 1, 3, bossDragon));

        System.out.println("=== Datos inyectados con éxito ===");
    }
}