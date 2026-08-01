package com.grupo3.mmorpg.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento que representa un Personaje (avatar) del jugador en MongoDB.
 * Colección: personajes
 */
@Document(collection = "personajes")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Creamos índices compuestos tal como lo pide el Laboratorio 3
@CompoundIndexes({
        @CompoundIndex(name = "clan_clase_rol_idx", def = "{'clanId': 1, 'clase': 1, 'rolClan': 1}")
})
public class Personaje {

    @Id
    private String idPersonaje;

    // En MongoDB, en lugar de un JOIN pesado de SQL, usamos @DBRef o simplemente guardamos el ID.
    // Como la instrucción pide "Referenciado", guardamos el ID del jugador y el ID del clan.
    private String jugadorId;

    private String clanId;

    // Ya no usamos @Column(unique=true), sino la anotación de indexación de Mongo.
    // (Nota: el índice único real se debe crear en la configuración, pero esto documenta la intención)
    private String nombre;

    private String clase;

    private Integer nivel;

    private String faccion;

    private Integer itemLevel;

    private Integer puntosMerito = 0;

    private String rolClan;

    // --- MANEJO GEOESPACIAL EN MONGODB (GeoJSON) ---
    @JsonIgnore
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE) // Obligatorio para $near
    private GeoJsonPoint ubicacionActual;

    @JsonProperty("latitud")
    public Double getLatitud() {
        return ubicacionActual != null ? ubicacionActual.getY() : null;
    }

    @JsonProperty("latitud")
    public void setLatitud(Double latitud) {
        if (latitud != null) {
            double longitudActual = (this.ubicacionActual != null) ? this.ubicacionActual.getX() : 0.0;
            // OJO: GeoJsonPoint en Spring Data recibe (Longitud, Latitud) = (X, Y)
            this.ubicacionActual = new GeoJsonPoint(longitudActual, latitud);
        }
    }

    @JsonProperty("longitud")
    public Double getLongitud() {
        return ubicacionActual != null ? ubicacionActual.getX() : null;
    }

    @JsonProperty("longitud")
    public void setLongitud(Double longitud) {
        if (longitud != null) {
            double latitudActual = (this.ubicacionActual != null) ? this.ubicacionActual.getY() : 0.0;
            // OJO: GeoJsonPoint en Spring Data recibe (Longitud, Latitud) = (X, Y)
            this.ubicacionActual = new GeoJsonPoint(longitud, latitudActual);
        }
    }

    private String regionMapa;
}