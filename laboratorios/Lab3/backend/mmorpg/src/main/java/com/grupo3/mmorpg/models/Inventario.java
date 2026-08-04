package com.grupo3.mmorpg.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento que representa un registro en el Inventario de un personaje.
 * Colección: inventarios
 */
@Document(collection = "inventarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventario {

    @Id
    private String idInventario; // Cambiado a String para el ObjectId de MongoDB

    // En MongoDB guardamos las referencias (IDs) en lugar de hacer JOINs completos
    private String itemId;

    private String personajeId;

    private Integer cantidad = 1;

    private Boolean equipado = false;
}