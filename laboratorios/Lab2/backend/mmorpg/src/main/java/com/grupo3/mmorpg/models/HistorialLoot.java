package com.grupo3.mmorpg.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad que representa el historial de distribución de loot
 * Mapea a la tabla: Historial_Loot
 */
@Entity
@Table(name = "Historial_Loot")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialLoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistorial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_raid", nullable = false)
    @ToString.Exclude
    private Raid raid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_personaje", nullable = false)
    @ToString.Exclude
    private Personaje personaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item", nullable = false)
    @ToString.Exclude
    private Item item;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fecha;

    private String estadoLoot;
}
