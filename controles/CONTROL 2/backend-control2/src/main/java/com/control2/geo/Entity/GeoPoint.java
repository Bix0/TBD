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

    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point point;
}
