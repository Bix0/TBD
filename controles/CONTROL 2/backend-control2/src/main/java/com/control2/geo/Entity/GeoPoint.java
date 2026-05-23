package com.control2.geo.Entity;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@Table(name = "GeoPoint")
@Entity
public class GeoPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long idGeoPoint;

    @Column(nullable = false)
    private String name;

    @JsonIgnore
    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point point;

    @Column(nullable = false)
    private String sector;

    // Metodos virtuales, que hacen? recogen la informacion del objeto point que
    // tenemos
    // guardado aqui mismo pero con cosas que un json puede leer como latitud y y
    // longitud x
    @JsonProperty("latitude")
    public Double getLatitude() {
        return point != null ? point.getY() : null;
    }

    @JsonProperty("longitude")
    public Double getLongitude() {
        return point != null ? point.getX() : null;
    }
}
