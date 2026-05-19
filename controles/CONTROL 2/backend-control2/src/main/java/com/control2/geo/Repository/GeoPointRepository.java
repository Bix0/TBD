package com.control2.geo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.control2.geo.Entity.GeoPoint;

public interface GeoPointRepository extends JpaRepository<GeoPoint, Long>{

}
