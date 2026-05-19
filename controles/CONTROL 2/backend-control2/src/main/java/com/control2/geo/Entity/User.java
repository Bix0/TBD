package com.control2.geo.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(name = "users")
@Entity
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long idUser;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password; //Need to be crypted with bcrypt

    // Se define explícitamente el tipo de columna espacial (SRID 4326)
    @ManyToOne
    @JoinColumn(name = "idGeoPoint", nullable = false)
    private GeoPoint geoPoint;
    

}
