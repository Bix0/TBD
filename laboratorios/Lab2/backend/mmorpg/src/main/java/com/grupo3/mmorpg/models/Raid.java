package com.grupo3.mmorpg.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// IMPORT VITAL PARA POSTGIS
import org.locationtech.jts.geom.Point;

/**
 * Entidad que representa una Raid (evento de grupo)
 * Mapea a la tabla: Raid
 *
 * Nota: En fases posteriores se agregará la columna espacial
 * ubicacionBoss (Point) para el punto de muerte del jefe.
 */
@Entity
@Table(name = "Raid")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Raid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRaid;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private Integer itemLevelRequerido;

    @Column(nullable = false)
    private Integer cuposTanque;

    @Column(nullable = false)
    private Integer cuposHealer;

    @Column(nullable = false)
    private Integer cuposDps;

    // --- NUEVO REQUERIMIENTO POSTGIS (LAB 2) ---
    @Column(name = "ubicacion_boss", columnDefinition = "geometry(Point, 4326)")
    private Point ubicacionBoss;
}