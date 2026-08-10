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

/**
 * Documento que representa un Clan en MongoDB
 * Colección: clanes
 */
@Document(collection = "clanes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clan {

    @Id
    private String idClan; // Cambiado a String para el ObjectId de MongoDB

    private String nombre;

    private String idLider; // Referencia al ID del personaje líder (String)

    private String faccion;

    // --- MANEJO GEOESPACIAL EN MONGODB (GeoJSON) ---
    @JsonIgnore
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE) // Obligatorio para consultas espaciales en clanes
    private GeoJsonPoint ubicacion;

    @JsonProperty("latitud")
    public void setLatitud(Double latitud) {
        if (latitud != null) {
            double longitudActual = (this.ubicacion != null) ? this.ubicacion.getX() : 0.0;
            this.ubicacion = new GeoJsonPoint(longitudActual, latitud);
        }
    }

    @JsonProperty("longitud")
    public void setLongitud(Double longitud) {
        if (longitud != null) {
            double latitudActual = (this.ubicacion != null) ? this.ubicacion.getY() : 0.0;
            this.ubicacion = new GeoJsonPoint(longitud, latitudActual);
        }
    }

    @JsonProperty("latitud")
    public Double getLatitud() {
        return ubicacion != null ? ubicacion.getY() : null;
    }

    @JsonProperty("longitud")
    public Double getLongitud() {
        return ubicacion != null ? ubicacion.getX() : null;
    }
}