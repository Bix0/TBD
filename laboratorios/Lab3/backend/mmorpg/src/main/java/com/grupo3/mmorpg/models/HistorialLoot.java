package com.grupo3.mmorpg.models;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento que representa el historial de distribución de loot en MongoDB.
 * Colección: historial_loot
 */
@Document(collection = "historial_loot")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Estrategia de Índices (Laboratorio 3):
// - raid_personaje_loot_idx: compuesto para consultar registros de botín por raid y personaje
// - raid_item_unique_idx: ÚNICO compuesto {raidId, itemId}. Garantiza a nivel de base de datos que un
//   mismo ítem solo pueda asignarse UNA vez por raid, incluso con peticiones concurrentes
//   (la segunda transacción falla con DuplicateKeyException y hace rollback).
@CompoundIndexes({
    @CompoundIndex(
        name = "raid_personaje_loot_idx",
        def = "{'raidId': 1, 'personajeId': 1}"
    ),
    @CompoundIndex(
        name = "raid_item_unique_idx",
        def = "{'raidId': 1, 'itemId': 1}",
        unique = true
    ),
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

    // Campos exigidos por el validador $jsonSchema (MongoSchemaConfig):
    // el loot solo se registra si el personaje participó en la raid y no estaba caído.
    private Boolean participoRaid = true;

    private String estadoPersonaje = "Activo";
}
