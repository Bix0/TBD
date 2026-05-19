package com.control2.geo.Service;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import com.control2.geo.Dto.GeoPointRequest;
import com.control2.geo.Entity.GeoPoint;
import com.control2.geo.Repository.GeoPointRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeoPointService {

    private final GeoPointRepository geoPointRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);;

    public GeoPoint getGeoPointById(Long id) {
        return geoPointRepository.findById(id).orElseThrow(() -> new RuntimeException("GeoPoint no encontrado"));
    }

    public List<GeoPoint> getAllGeoPoints() {
        return geoPointRepository.findAll();
    }

    public Point createPoint(double latitude, double longitude) {
        if (latitude == 0.0 || longitude == 0.0) {
            return null;
        }
        // IMPORTANTE: El orden en JTS siempre es (Longitud, Latitud) -> (X, Y)
        Coordinate coordinate = new Coordinate(longitude, latitude);
        return geometryFactory.createPoint(coordinate);
    }    

    public GeoPoint createGeoPoint(GeoPointRequest dto) {
        GeoPoint geoPoint = new GeoPoint();
        geoPoint.setPoint(createPoint(dto.getLatitude(), dto.getLongitude()));
        geoPoint.setName(dto.getName());
        return geoPointRepository.save(geoPoint);
    }

    public String modifyGeoPoint(Long id, GeoPointRequest dto) {
        GeoPoint geoPoint = getGeoPointById(id);
        geoPoint.setPoint(createPoint(dto.getLatitude(), dto.getLongitude()));
        geoPoint.setName(dto.getName());
        geoPointRepository.save(geoPoint);
        return "GeoPoint modificado exitosamente";
    }

    public String deleteGeoPoint(Long id) {
        GeoPoint geoPoint = getGeoPointById(id);
        geoPointRepository.delete(geoPoint);
        return "GeoPoint eliminado exitosamente";
    }
}
