package com.grupo3.mmorpg.models;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// IMPORT VITAL PARA POSTGIS
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Entidad que representa un Clan
 * Mapea a la tabla: Clan
 *
 * Nota: idLider referencia a Personaje.id_personaje (relación many-to-one
 * definida desde Personaje). En fases posteriores se agregará la columna
 * espacial ubicacion (Point).
 */
@Entity
@Table(name = "Clan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClan;

    @Column(unique = true, nullable = false)
    private String nombre;

    @Column(name = "id_lider")
    private Long idLider;

    @Column(nullable = false)    
    private String faccion;   


    @JsonIgnore
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point ubicacion;

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @JsonProperty("latitud")
    public void setLatitud(Double y) {
        if (y != null) {
            double x = (this.ubicacion != null) ? this.ubicacion.getX() : 0;
            this.ubicacion = geometryFactory.createPoint(new Coordinate(x, y));
        }
    }

    @JsonProperty("longitud")
    public void setLongitud(Double x) {
        if (x != null) {
            double y = (this.ubicacion != null) ? this.ubicacion.getY() : 0;
            this.ubicacion = geometryFactory.createPoint(new Coordinate(x, y));
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