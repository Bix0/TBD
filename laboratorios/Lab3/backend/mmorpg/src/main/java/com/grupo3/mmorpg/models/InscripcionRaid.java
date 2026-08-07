package com.grupo3.mmorpg.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento que representa la inscripción de un personaje a una Raid en MongoDB.
 * Colección: inscripciones_raid
 */
@Document(collection = "inscripciones_raid")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Estrategia de Índices (Laboratorio 3): Índice compuesto único para evitar que un personaje se inscriba dos veces a la misma raid
@CompoundIndexes({
        @CompoundIndex(name = "raid_personaje_unique_idx", def = "{'raidId': 1, 'personajeId': 1}", unique = true)
})
public class InscripcionRaid {

    @Id
    private String idInscripcion; // ObjectId de MongoDB mapeado como String

    // En MongoDB guardamos las referencias como IDs (Strings) en lugar de hacer JOINs completos
    @Indexed // Índice simple para acelerar la búsqueda de todas las inscripciones asociadas a una Raid
    private String raidId;

    @Indexed // Índice simple para acelerar la consulta del historial de raids en las que participa un personaje
    private String personajeId;

    private String estado;

    private Boolean asistio = false;

    // Nuevo campo para Requerimiento 4: Desempeño
    private Integer danoTotal = 0;
}