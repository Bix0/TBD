package com.control2.geo.Service;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.control2.geo.Entity.GeoPoint;
import com.control2.geo.Repository.GeoPointRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeoPointService {

    private final GeoPointRepository geoPointRepository;

    public GeoPoint getGeoPointById(Long id) {
        return geoPointRepository.findById(id).orElseThrow(() -> new RuntimeException("GeoPoint no encontrado"));
    }

    public List<GeoPoint> getAllGeoPoints() {
        return geoPointRepository.findAll();
    }

    public String createGeoPoint(Point point) {
        GeoPoint geoPoint = new GeoPoint();
        geoPoint.setPoint(point);
        geoPointRepository.save(geoPoint);
        return "GeoPoint creado exitosamente";
    }

    public String modifyGeoPoint(Long id, Point point) {
        GeoPoint geoPoint = getGeoPointById(id);
        geoPoint.setPoint(point);
        geoPointRepository.save(geoPoint);
        return "GeoPoint modificado exitosamente";
    }

    public String deleteGeoPoint(Long id) {
        GeoPoint geoPoint = getGeoPointById(id);
        geoPointRepository.delete(geoPoint);
        return "GeoPoint eliminado exitosamente";
    }
}
