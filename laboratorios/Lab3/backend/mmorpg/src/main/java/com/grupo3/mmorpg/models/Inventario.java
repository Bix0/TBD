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
 * Documento que representa un registro en el Inventario de un personaje.
 * Colección: inventarios
 */
@Document(collection = "inventarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Estrategia de Índices (Laboratorio 3): Índice compuesto para consultar rápidamente el inventario filtrado por personaje y si está equipado
@CompoundIndexes({
        @CompoundIndex(name = "personaje_equipado_idx", def = "{'personajeId': 1, 'equipado': 1}")
})
public class Inventario {

    @Id
    private String idInventario; // ObjectId de MongoDB mapeado como String

    // En MongoDB guardamos las referencias (IDs) en lugar de hacer JOINs completos
    @Indexed // Índice simple para acelerar búsquedas de todos los ítems que posee un objeto específico
    private String itemId;

    @Indexed // Índice simple para optimizar la carga del inventario por cada jugador/personaje
    private String personajeId;

    private Integer cantidad = 1;

    private Boolean equipado = false;
}