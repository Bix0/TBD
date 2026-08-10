package com.grupo3.mmorpg.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Documento que representa un Item (objeto/equipo) en MongoDB
 * Colección: items
 */
@Document(collection = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Estrategia de Índices (Laboratorio 3): Índice compuesto para filtrar ítems por nivel y coste/ganancia de DKP
@CompoundIndexes({
        @CompoundIndex(name = "itemlvl_gananciadkp_idx", def = "{'itemLvl': 1, 'gananciaDkp': -1}")
})
public class Item {

    @Id
    private String idItem; // ObjectId de MongoDB mapeado como String

    @Indexed(unique = true) // Índice único para evitar nombres de ítems duplicados en el juego
    private String nombre;

    @Indexed // Índice simple para acelerar búsquedas y filtros por nivel de objeto (Item Level)
    private Integer itemLvl;

    private Integer gananciaDkp = 0;

    // ¡Aquí está la magia de MongoDB en Java!
    // Reemplaza por completo la necesidad de tener la clase ItemClasePermitida.java
    private List<String> clasesPermitidas;
}