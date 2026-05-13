package com.grupo3.mmorpg.models;

import java.time.LocalDateTime;

/**
 * Entidad que representa el historial de distribución de loot
 * Mapea a la tabla: Historial_Loot
 */
public class HistorialLoot {
    
    private Long id_historial;
    private Long id_raid;
    private Long id_personaje;
    private Long id_item;
    private LocalDateTime fecha;
    private String estado_loot;
    
    // Constructor sin parámetros
    public HistorialLoot() {
    }
    
    // Constructor completo
    public HistorialLoot(Long id_historial, Long id_raid, Long id_personaje,
                         Long id_item, LocalDateTime fecha, String estado_loot) {
        this.id_historial = id_historial;
        this.id_raid = id_raid;
        this.id_personaje = id_personaje;
        this.id_item = id_item;
        this.fecha = fecha;
        this.estado_loot = estado_loot;
    }
    
    // Getters y Setters
    
    public Long getId_historial() {
        return id_historial;
    }
    
    public void setId_historial(Long id_historial) {
        this.id_historial = id_historial;
    }
    
    public Long getId_raid() {
        return id_raid;
    }
    
    public void setId_raid(Long id_raid) {
        this.id_raid = id_raid;
    }
    
    public Long getId_personaje() {
        return id_personaje;
    }
    
    public void setId_personaje(Long id_personaje) {
        this.id_personaje = id_personaje;
    }
    
    public Long getId_item() {
        return id_item;
    }
    
    public void setId_item(Long id_item) {
        this.id_item = id_item;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
    public String getEstado_loot() {
        return estado_loot;
    }
    
    public void setEstado_loot(String estado_loot) {
        this.estado_loot = estado_loot;
    }
    
    @Override
    public String toString() {
        return "HistorialLoot{" +
                "id_historial=" + id_historial +
                ", id_raid=" + id_raid +
                ", id_personaje=" + id_personaje +
                ", id_item=" + id_item +
                ", fecha=" + fecha +
                ", estado_loot='" + estado_loot + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistorialLoot that = (HistorialLoot) o;
        return id_historial != null && id_historial.equals(that.id_historial);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}