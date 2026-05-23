package com.control2.geo.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.control2.geo.Dto.GeoPointRequest;
import com.control2.geo.Entity.GeoPoint;
import com.control2.geo.Service.GeoPointService;
import com.control2.geo.security.UserPrincipal;

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

    @PostMapping("/GeoPoints/creategeopoint/{userId}")
    public ResponseEntity<String> createGeoPoint(@PathVariable Long userId, @RequestBody GeoPointRequest geoPointRequest, @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para crear un punto geográfico.");
        }
        geoPointService.createGeoPoint(geoPointRequest);
        return ResponseEntity.ok("GeoPoint creado exitosamente");
    }

    @PutMapping("/GeoPoints/modifygeopoint/{idGeoPoint}/{userId}")
    public ResponseEntity<String> modifyGeoPoint(@PathVariable Long idGeoPoint, @PathVariable Long userId, @RequestBody GeoPointRequest geoPointRequest, @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para modificar este punto geográfico.");
        }
        geoPointService.modifyGeoPoint(idGeoPoint, geoPointRequest);
        return ResponseEntity.ok("GeoPoint modificado exitosamente");
    }
    
    @DeleteMapping("/GeoPoints/deletegeopoint/{idGeoPoint}/{userId}")
    public ResponseEntity<String> deleteGeoPoint(@PathVariable Long idGeoPoint, @PathVariable Long userId, @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para eliminar este punto geográfico.");
        }
        geoPointService.deleteGeoPoint(idGeoPoint);
        return ResponseEntity.ok("GeoPoint eliminado exitosamente");
    }
}
