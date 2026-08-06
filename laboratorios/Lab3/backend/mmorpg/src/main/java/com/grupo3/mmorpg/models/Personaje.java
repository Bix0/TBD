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

@Document(collection = "personajes")
@Data
@NoArgsConstructor
@AllArgsConstructor
// =====================================================================
// REQUISITO: Índice compuesto para filtrar rápido por Clan + Clase + Rol
// =====================================================================
@CompoundIndexes({
    @CompoundIndex(
        name = "clan_clase_rol_idx",
        def = "{'clanId': 1, 'clase': 1, 'rolClan': 1}"
    ),
})
public class Personaje {

    @Id
    private String idPersonaje;

    private String jugadorId;

    private String clanId;

    // =====================================================================
    // REQUISITO: Índice Único sobre el nombre del personaje
    // =====================================================================
    @Indexed(unique = true)
    private String nombre;

    private String clase;
    private Integer nivel;
    private String faccion;
    private Integer itemLevel;
    private Integer puntosMerito = 0;
    private String rolClan;

    // --- MANEJO GEOESPACIAL EN MONGODB (GeoJSON + 2dsphere) ---
    @JsonIgnore
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint ubicacionActual;

    private String regionMapa;

    // Estado de combate: "Activo" o "Caido" (fuera de combate).
    // La regla de Schema Validation de loot exige que el personaje no esté caído
    // al momento de la distribución (se registra en historial_loot.estadoPersonaje).
    private String estado = "Activo";

    @JsonProperty("latitud")
    public Double getLatitud() {
        return ubicacionActual != null ? ubicacionActual.getY() : null;
    }

    @JsonProperty("latitud")
    public void setLatitud(Double latitud) {
        if (latitud != null) {
            double x =
                this.ubicacionActual != null
                    ? this.ubicacionActual.getX()
                    : 0.0;
            this.ubicacionActual = new GeoJsonPoint(x, latitud);
        }
    }

    @JsonProperty("longitud")
    public Double getLongitud() {
        return ubicacionActual != null ? ubicacionActual.getX() : null;
    }

    @JsonProperty("longitud")
    public void setLongitud(Double longitud) {
        if (longitud != null) {
            double y =
                this.ubicacionActual != null
                    ? this.ubicacionActual.getY()
                    : 0.0;
            this.ubicacionActual = new GeoJsonPoint(longitud, y);
        }
    }
}
