package com.grupo3.mmorpg.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Entidad que representa las clases permitidas para un item
 * Mapea a la tabla: Item_Clase_Permitida
 * Tiene clave primaria compuesta: (id_item, clase_permitida)
 */
@Entity
@Table(name = "Item_Clase_Permitida")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemClasePermitida {

    @EmbeddedId
    private ItemClasePermitidaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idItem")
    @JoinColumn(name = "id_item", nullable = false)
    private Item item;

    /**
     * Clave primaria embebible para ItemClasePermitida
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemClasePermitidaId implements Serializable {
        @Column(name = "id_item")
        private Long idItem;

        @Column(name = "clase_permitida")
        private String clasePermitida;
    }
}
