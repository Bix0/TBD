package com.grupo3.mmorpg.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
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
// Estrategia de Índices (Laboratorio 3): Índice compuesto para filtrar raids activas por su nivel de objeto requerido
@CompoundIndexes({
        @CompoundIndex(name = "estado_itemlevel_idx", def = "{'estado': 1, 'itemLevelRequerido': 1}")
})
public class Raid {

    @Id
    private String idRaid; // ObjectId de Mongo mapeado como String

    @Indexed(unique = true) // Índice único para evitar nombres de raids duplicados
    private String nombre;

    @Indexed // Índice simple para ordenar y buscar por fechas de eventos rápidamente
    private LocalDateTime fecha;

    @Indexed // Índice para acelerar los filtros por estado ("Programada", "En curso", "Finalizada")
    private String estado;

    private Integer itemLevelRequerido;

    private Integer cuposTanque;

    private Integer cuposHealer;

    private Integer cuposDps;

    // Nuevo campo para Requerimiento 4: Cálculo de desempeño de raids
    private Integer tiempoFinalizacionMinutos;

    // --- MANEJO GEOESPACIAL EN MONGODB (GeoJSON) ---
    @JsonIgnore
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE) // Índice geoespacial obligatorio para consultas $near o de proximidad al jefe
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