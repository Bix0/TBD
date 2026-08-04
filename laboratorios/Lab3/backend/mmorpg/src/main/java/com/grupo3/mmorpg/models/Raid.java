package com.grupo3.mmorpg.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento que representa una Raid (evento de grupo) en MongoDB.
 * Colección: raids
 */
@Document(collection = "raids")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Raid {

    @Id
    private String idRaid; // Cambiado a String para el ObjectId de Mongo

    private String nombre;

    private LocalDateTime fecha;

    private String estado;

    private Integer itemLevelRequerido;

    private Integer cuposTanque;

    private Integer cuposHealer;

    private Integer cuposDps;

    // --- MANEJO GEOESPACIAL EN MONGODB (GeoJSON) ---
    @JsonIgnore
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE) // Crea el índice espacial automáticamente
    private GeoJsonPoint ubicacionBoss;

    // Setters virtuales para recibir latitud/longitud desde el frontend
    @JsonProperty("latitud")
    public void setLatitud(Double latitud) {
        if (latitud != null) {
            // GeoJsonPoint recibe (longitud, latitud) -> (X, Y)
            double longitudActual = (this.ubicacionBoss != null) ? this.ubicacionBoss.getX() : 0.0;
            this.ubicacionBoss = new GeoJsonPoint(longitudActual, latitud);
        }
    }

    @JsonProperty("longitud")
    public void setLongitud(Double longitud) {
        if (longitud != null) {
            // GeoJsonPoint recibe (longitud, latitud) -> (X, Y)
            double latitudActual = (this.ubicacionBoss != null) ? this.ubicacionBoss.getY() : 0.0;
            this.ubicacionBoss = new GeoJsonPoint(longitud, latitudActual);
        }
    }

    // Getters virtuales para devolver latitud/longitud en JSON al frontend
    @JsonProperty("latitud")
    public Double getLatitud() {
        return ubicacionBoss != null ? ubicacionBoss.getY() : null;
    }

    @JsonProperty("longitud")
    public Double getLongitud() {
        return ubicacionBoss != null ? ubicacionBoss.getX() : null;
    }
}