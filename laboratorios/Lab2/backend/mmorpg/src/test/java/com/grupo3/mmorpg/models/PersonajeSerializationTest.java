package com.grupo3.mmorpg.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonajeSerializationTest {

    @Test
    void serializaUbicacionActualComoLatitudYLongitud() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Personaje personaje = new Personaje();

        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(-33.45, -70.66));
        personaje.setUbicacionActual(point);

        String json = objectMapper.writeValueAsString(personaje);

        assertTrue(json.contains("latitud"));
        assertTrue(json.contains("longitud"));
        assertFalse(json.contains("ubicacionActual"));
    }
}
