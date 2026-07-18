package com.grupo3.mmorpg.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// IMPORT VITAL PARA POSTGIS
import org.locationtech.jts.geom.Point;

/**
 * Entidad que representa un Clan
 * Mapea a la tabla: Clan
 *
 * Nota: idLider referencia a Personaje.id_personaje (relación many-to-one
 * definida desde Personaje). En fases posteriores se agregará la columna
 * espacial ubicacion (Point).
 */
@Entity
@Table(name = "Clan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClan;

    @Column(unique = true, nullable = false)
    private String nombre;

    @Column(name = "id_lider")
    private Long idLider;

    // --- NUEVO REQUERIMIENTO POSTGIS (LAB 2) ---
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point ubicacion;
}