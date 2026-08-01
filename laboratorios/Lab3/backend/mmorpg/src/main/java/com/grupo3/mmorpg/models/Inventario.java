package com.grupo3.mmorpg.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entidad que representa el Inventario de un personaje
 * Mapea a la tabla: Inventario
 */
@Entity
@Table(name = "Inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInventario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item", nullable = false)
    @ToString.Exclude
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_personaje", nullable = false)
    @ToString.Exclude
    private Personaje personaje;

    @Column(nullable = false)
    private Integer cantidad = 1;

    @Column(nullable = false)
    private Boolean equipado = false;
}
