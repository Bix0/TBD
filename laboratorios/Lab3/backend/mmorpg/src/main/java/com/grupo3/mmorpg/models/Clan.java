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

/**
 * Documento que representa un Clan en MongoDB
 * Colección: clanes
 */
@Document(collection = "clanes")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Estrategia de Índices (Laboratorio 3): Índice compuesto para optimizar consultas de clanes filtrados por facción
@CompoundIndexes({
        @CompoundIndex(name = "faccion_lider_idx", def = "{'faccion': 1, 'idLider': 1}")
})
public class Clan {

    @Id
    private String idClan; // ObjectId de MongoDB mapeado como String

    @Indexed(unique = true) // Índice único para garantizar que no existan clanes con nombres duplicados
    private String nombre;

    private String idLider; // Referencia al ID del personaje líder (String)

    @Indexed // Índice simple para acelerar filtros rápidos por facción ("Alianza" o "Horda")
    private String faccion;

    // --- MANEJO GEOESPACIAL EN MONGODB (GeoJSON) ---
    @JsonIgnore
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE) // Índice geoespacial obligatorio para consultas de cercanía a la sede del clan
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