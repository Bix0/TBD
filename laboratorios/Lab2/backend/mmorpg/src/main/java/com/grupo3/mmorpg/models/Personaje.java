package com.grupo3.mmorpg.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

// IMPORT VITAL PARA POSTGIS
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

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
@JsonIgnoreProperties({"ubicacionActual"})
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
    @JsonIgnore
    @Column(name = "ubicacion_actual", columnDefinition = "geometry(Point, 4326)")
    private Point ubicacionActual;

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @JsonProperty("latitud")
    public Double getLatitud() {
        return ubicacionActual != null ? ubicacionActual.getY() : null;
    }

    @JsonProperty("latitud")
    public void setLatitud(Double y) {
        if (y != null) {
            double x = (this.ubicacionActual != null) ? this.ubicacionActual.getX() : 0;
            this.ubicacionActual = geometryFactory.createPoint(new Coordinate(x, y));
        }
    }

    @JsonProperty("longitud")
    public Double getLongitud() {
        return ubicacionActual != null ? ubicacionActual.getX() : null;
    }

    @JsonProperty("longitud")
    public void setLongitud(Double x) {
        if (x != null) {
            double y = (this.ubicacionActual != null) ? this.ubicacionActual.getY() : 0;
            this.ubicacionActual = geometryFactory.createPoint(new Coordinate(x, y));
        }
    }

    @Column(name = "region_mapa")
    private String regionMapa;
}