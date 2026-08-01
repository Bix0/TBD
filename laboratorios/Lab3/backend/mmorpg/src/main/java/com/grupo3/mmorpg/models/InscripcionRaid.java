package com.grupo3.mmorpg.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entidad que representa la inscripción de un personaje a una Raid
 * Mapea a la tabla: Inscripcion_Raid
 */
@Entity
@Table(name = "Inscripcion_Raid")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionRaid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_raid", nullable = false)
    @ToString.Exclude
    private Raid raid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_personaje", nullable = false)
    @ToString.Exclude
    private Personaje personaje;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private Boolean asistio = false;
}
