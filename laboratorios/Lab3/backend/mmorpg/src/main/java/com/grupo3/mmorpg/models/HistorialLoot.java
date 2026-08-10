package com.grupo3.mmorpg.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento que representa el historial de distribución de loot en MongoDB.
 * Colección: historial_loot
 */
@Document(collection = "historial_loot")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialLoot {

    @Id
    private String idHistorial; // Cambiado a String para el ObjectId de MongoDB

    // En MongoDB guardamos las referencias (IDs) en lugar de hacer JOINs completos
    private String raidId;

    private String personajeId;

    private String itemId;

    // Asignamos la fecha actual por defecto en la creación del objeto
    private LocalDateTime fecha = LocalDateTime.now();

    private String estadoLoot;

    //nuevas variales agregadas
    private Boolean participoRaid = true;

    private String estadoPersonaje = "Activo";
}