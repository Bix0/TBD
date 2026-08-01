package com.grupo3.mmorpg.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento que representa la inscripción de un personaje a una Raid en MongoDB.
 * Colección: inscripciones_raid
 */
@Document(collection = "inscripciones_raid")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionRaid {

    @Id
    private String idInscripcion; // Cambiado a String para el ObjectId de MongoDB

    // En MongoDB guardamos las referencias como IDs (Strings) en lugar de hacer JOINs completos
    private String raidId;

    private String personajeId;

    private String estado;

    private Boolean asistio = false;
}