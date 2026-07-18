package com.grupo3.mmorpg.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

// IMPORT VITAL PARA POSTGIS
import org.locationtech.jts.geom.Point;

/**
 * Entidad que representa un Personaje (avatar) del jugador
 * Mapea a la tabla: Personaje
 *
 * Nota: En fases posteriores se agregará la columna espacial
 * ubicacion (Point) y el campo regionMapa (String).
 */
@Entity
@Table(name = "Personaje")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Personaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPersonaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jugador", nullable = false)
    @ToString.Exclude
    private Jugador jugador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_clan")
    @ToString.Exclude
    private Clan clan;

    @Column(unique = true, nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String clase;

    @Column(nullable = false)
    private Integer nivel;

    @Column(nullable = false)
    private String faccion;

    @Column(nullable = false)
    private Integer itemLevel;

    @Column(nullable = false)
    private Integer puntosMerito = 0;

    private String rolClan;

    // --- NUEVO REQUERIMIENTO POSTGIS (LAB 2) ---
    @Column(name = "ubicacion_actual", columnDefinition = "geometry(Point, 4326)")
    private Point ubicacionActual;

    @Column(name = "region_mapa")
    private String regionMapa;
}