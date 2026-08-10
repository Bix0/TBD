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
 * Documento que representa la auditoría de cambios de liderazgo en clanes en MongoDB.
 * Colección: auditoria_liderazgo
 */
@Document(collection = "auditoria_liderazgo")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Estrategia de Índices (Laboratorio 3): Índice compuesto para consultar el historial de cambios ordenado por clan y fecha
@CompoundIndexes({
        @CompoundIndex(name = "clan_fecha_idx", def = "{'clanId': 1, 'fechaCambio': -1}")
})
public class AuditoriaLiderazgo {

    @Id
    private String idAuditoria; // ObjectId de MongoDB mapeado como String

    // En MongoDB guardamos las referencias como IDs en lugar de objetos completos
    @Indexed // Índice simple para acelerar las búsquedas de auditoría asociadas a un Clan específico
    private String clanId;

    private String antiguoLiderId;

    private String nuevoLiderId;

    @Indexed // Índice simple para filtrar o ordenar los eventos de auditoría cronológicamente
    private LocalDateTime fechaCambio = LocalDateTime.now();

    // --- MANEJO GEOESPACIAL EN MONGODB (GeoJSON) ---
    @JsonIgnore
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE) // Soporte espacial obligatorio para consultas geográficas del suceso
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