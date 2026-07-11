package com.grupo3.mmorpg.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un Item (objeto/equipo)
 * Mapea a la tabla: Item
 */
@Entity
@Table(name = "Item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idItem;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Integer itemLvl;

    @Column(nullable = false)
    private Integer gananciaDkp = 0;
}
