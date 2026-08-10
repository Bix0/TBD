package com.grupo3.mmorpg.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// IMPORT VITAL PARA POSTGIS
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Entidad que representa una Raid (evento de grupo)
 * Mapea a la tabla: Raid
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
    @JsonIgnore
    @Column(name = "ubicacion_boss", columnDefinition = "geometry(Point, 4326)")
    private Point ubicacionBoss;

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    // Setters virtuales para recibir latitud/longitud desde el frontend
    @JsonProperty("latitud")
    public void setLatitud(Double y) {
        if (y != null) {
            double x = (this.ubicacionBoss != null) ? this.ubicacionBoss.getX() : 0;
            this.ubicacionBoss = geometryFactory.createPoint(new Coordinate(x, y));
        }
    }

    @JsonProperty("longitud")
    public void setLongitud(Double x) {
        if (x != null) {
            double y = (this.ubicacionBoss != null) ? this.ubicacionBoss.getY() : 0;
            this.ubicacionBoss = geometryFactory.createPoint(new Coordinate(x, y));
        }
    }

    // Getters virtuales para devolver latitud/longitud en JSON
    @JsonProperty("latitud")
    public Double getLatitud() {
        return ubicacionBoss != null ? ubicacionBoss.getY() : null;
    }

    @JsonProperty("longitud")
    public Double getLongitud() {
        return ubicacionBoss != null ? ubicacionBoss.getX() : null;
    }
}
