package com.grupo3.mmorpg.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad que representa la auditoría de cambios de liderazgo en clanes
 * Mapea a la tabla: Auditoria_Liderazgo
 *
 * Nota: En fases posteriores se agregará la columna espacial
 * coordenadas (Point) para el mapa histórico de "Sedes de Poder".
 */
@Entity
@Table(name = "Auditoria_Liderazgo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaLiderazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAuditoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_clan", nullable = false)
    @ToString.Exclude
    private Clan clan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_antiguo_lider")
    @ToString.Exclude
    private Personaje antiguoLider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nuevo_lider", nullable = false)
    @ToString.Exclude
    private Personaje nuevoLider;

    @Column(nullable = false)
    private LocalDateTime fechaCambio;
}
