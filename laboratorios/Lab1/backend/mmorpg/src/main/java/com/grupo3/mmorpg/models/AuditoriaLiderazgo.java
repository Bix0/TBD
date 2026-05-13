package com.grupo3.mmorpg.models;

import java.time.LocalDateTime;

/**
 * Entidad que representa la auditoría de cambios de liderazgo en clanes
 * Mapea a la tabla: Auditoria_Liderazgo
 */
public class AuditoriaLiderazgo {
    
    private Long id_auditoria;
    private Long id_clan;
    private Long id_antiguo_lider;
    private Long id_nuevo_lider;
    private LocalDateTime fecha_cambio;
    
    // Constructor sin parámetros
    public AuditoriaLiderazgo() {
    }
    
    // Constructor completo
    public AuditoriaLiderazgo(Long id_auditoria, Long id_clan, Long id_antiguo_lider,
                              Long id_nuevo_lider, LocalDateTime fecha_cambio) {
        this.id_auditoria = id_auditoria;
        this.id_clan = id_clan;
        this.id_antiguo_lider = id_antiguo_lider;
        this.id_nuevo_lider = id_nuevo_lider;
        this.fecha_cambio = fecha_cambio;
    }
    
    // Getters y Setters
    
    public Long getId_auditoria() {
        return id_auditoria;
    }
    
    public void setId_auditoria(Long id_auditoria) {
        this.id_auditoria = id_auditoria;
    }
    
    public Long getId_clan() {
        return id_clan;
    }
    
    public void setId_clan(Long id_clan) {
        this.id_clan = id_clan;
    }
    
    public Long getId_antiguo_lider() {
        return id_antiguo_lider;
    }
    
    public void setId_antiguo_lider(Long id_antiguo_lider) {
        this.id_antiguo_lider = id_antiguo_lider;
    }
    
    public Long getId_nuevo_lider() {
        return id_nuevo_lider;
    }
    
    public void setId_nuevo_lider(Long id_nuevo_lider) {
        this.id_nuevo_lider = id_nuevo_lider;
    }
    
    public LocalDateTime getFecha_cambio() {
        return fecha_cambio;
    }
    
    public void setFecha_cambio(LocalDateTime fecha_cambio) {
        this.fecha_cambio = fecha_cambio;
    }
    
    @Override
    public String toString() {
        return "AuditoriaLiderazgo{" +
                "id_auditoria=" + id_auditoria +
                ", id_clan=" + id_clan +
                ", id_antiguo_lider=" + id_antiguo_lider +
                ", id_nuevo_lider=" + id_nuevo_lider +
                ", fecha_cambio=" + fecha_cambio +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditoriaLiderazgo that = (AuditoriaLiderazgo) o;
        return id_auditoria != null && id_auditoria.equals(that.id_auditoria);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}