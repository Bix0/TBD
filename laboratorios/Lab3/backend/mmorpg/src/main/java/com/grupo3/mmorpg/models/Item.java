package com.grupo3.mmorpg.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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
public class Item {

    @Id
    private String idItem; // Cambiado a String para el ObjectId de MongoDB

    private String nombre;

    private Integer itemLvl;

    private Integer gananciaDkp = 0;

    // ¡Aquí está la magia de MongoDB en Java! 
    // Reemplaza por completo la necesidad de tener la clase ItemClasePermitida.java
    private List<String> clasesPermitidas;
}