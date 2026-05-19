package com.control2.geo.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.control2.geo.Entity.GeoPoint;
import com.control2.geo.Service.GeoPointService;

import lombok.RequiredArgsConstructor;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class GeoPointController {

    private final GeoPointService geoPointService;

    @GetMapping("/GeoPoints")
    public ResponseEntity<List<GeoPoint>> getAllGeoPoints() {
        List<GeoPoint> geoPoints = geoPointService.getAllGeoPoints();
        if(geoPoints.isEmpty()) {
            // Return 204 No Content if the list of geo points is empty
            return ResponseEntity.noContent().build();
        }
        // Return 200 OK with the list of geo points
        return ResponseEntity.ok(geoPoints);
    }

    @GetMapping("/GeoPoints/{id}")
    public ResponseEntity<GeoPoint> getGeoPointById(@PathVariable Long id) {
        GeoPoint geoPoint = geoPointService.getGeoPointById(id);
        if (geoPoint == null) {
            // Return 404 Not Found if the geo point is not found
            return ResponseEntity.notFound().build();
        }
        // Return 200 OK with the geo point
        return ResponseEntity.ok(geoPoint);
    }
}
