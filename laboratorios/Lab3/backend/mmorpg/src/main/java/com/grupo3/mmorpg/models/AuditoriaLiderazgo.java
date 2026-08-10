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
 * Documento que representa la auditoría de cambios de liderazgo en clanes en MongoDB.
 * Colección: auditoria_liderazgo
 */
@Document(collection = "auditoria_liderazgo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaLiderazgo {

    @Id
    private String idAuditoria; // Cambiado a String para el ObjectId de MongoDB

    // En MongoDB guardamos las referencias como IDs en lugar de objetos completos
    private String clanId;

    private String antiguoLiderId;

    private String nuevoLiderId;

    private LocalDateTime fechaCambio = LocalDateTime.now();

    // --- MANEJO GEOESPACIAL EN MONGODB (GeoJSON) ---
    @JsonIgnore
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE) // Soporte espacial para el suceso
    private GeoJsonPoint ubicacionSuceso;

    @JsonProperty("latitud")
    public void setLatitud(Double latitud) {
        if (latitud != null) {
            double longitudActual = (this.ubicacionSuceso != null) ? this.ubicacionSuceso.getX() : 0.0;
            this.ubicacionSuceso = new GeoJsonPoint(longitudActual, latitud);
        }
    }

    @JsonProperty("longitud")
    public void setLongitud(Double longitud) {
        if (longitud != null) {
            double latitudActual = (this.ubicacionSuceso != null) ? this.ubicacionSuceso.getY() : 0.0;
            this.ubicacionSuceso = new GeoJsonPoint(longitud, latitudActual);
        }
    }

    @JsonProperty("latitud")
    public Double getLatitud() {
        return ubicacionSuceso != null ? ubicacionSuceso.getY() : null;
    }

    @JsonProperty("longitud")
    public Double getLongitud() {
        return ubicacionSuceso != null ? ubicacionSuceso.getX() : null;
    }
}