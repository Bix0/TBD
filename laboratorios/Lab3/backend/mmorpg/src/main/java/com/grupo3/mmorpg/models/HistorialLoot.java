package com.grupo3.mmorpg.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
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
// Estrategia de Índices (Laboratorio 3): Índice compuesto para consultar registros de botín por raid y personaje
@CompoundIndexes({
        @CompoundIndex(name = "raid_personaje_loot_idx", def = "{'raidId': 1, 'personajeId': 1}")
})
public class HistorialLoot {

    @Id
    private String idHistorial; // ObjectId de MongoDB mapeado como String

    // En MongoDB guardamos las referencias (IDs) en lugar de hacer JOINs completos
    @Indexed // Índice simple para acelerar la consulta de todo el loot entregado en una Raid específica
    private String raidId;

    @Indexed // Índice simple para acelerar el historial de ítems obtenidos por un personaje
    private String personajeId;

    private String itemId;

    // Asignamos la fecha actual por defecto en la creación del objeto
    private LocalDateTime fecha = LocalDateTime.now();

    private String estadoLoot;
}