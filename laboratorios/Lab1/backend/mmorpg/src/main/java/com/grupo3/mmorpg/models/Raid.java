package com.grupo3.mmorpg.models;

import java.time.LocalDateTime;

/**
 * Entidad que representa una Raid (evento de grupo)
 * Mapea a la tabla: Raid
 */
public class Raid {
    
    private Long id_raid;
    private String nombre;
    private LocalDateTime fecha;
    private String estado;
    private Integer item_level_requerido;
    private Integer cupos_tanque;
    private Integer cupos_healer;
    private Integer cupos_dps;
    
    // Constructor sin parámetros
    public Raid() {
    }
    
    // Constructor completo
    public Raid(Long id_raid, String nombre, LocalDateTime fecha, String estado,
                Integer item_level_requerido, Integer cupos_tanque, Integer cupos_healer,
                Integer cupos_dps) {
        this.id_raid = id_raid;
        this.nombre = nombre;
        this.fecha = fecha;
        this.estado = estado;
        this.item_level_requerido = item_level_requerido;
        this.cupos_tanque = cupos_tanque;
        this.cupos_healer = cupos_healer;
        this.cupos_dps = cupos_dps;
    }
    
    // Getters y Setters
    
    public Long getId_raid() {
        return id_raid;
    }
    
    public void setId_raid(Long id_raid) {
        this.id_raid = id_raid;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public Integer getItem_level_requerido() {
        return item_level_requerido;
    }
    
    public void setItem_level_requerido(Integer item_level_requerido) {
        this.item_level_requerido = item_level_requerido;
    }
    
    public Integer getCupos_tanque() {
        return cupos_tanque;
    }
    
    public void setCupos_tanque(Integer cupos_tanque) {
        this.cupos_tanque = cupos_tanque;
    }
    
    public Integer getCupos_healer() {
        return cupos_healer;
    }
    
    public void setCupos_healer(Integer cupos_healer) {
        this.cupos_healer = cupos_healer;
    }
    
    public Integer getCupos_dps() {
        return cupos_dps;
    }
    
    public void setCupos_dps(Integer cupos_dps) {
        this.cupos_dps = cupos_dps;
    }
    
    @Override
    public String toString() {
        return "Raid{" +
                "id_raid=" + id_raid +
                ", nombre='" + nombre + '\'' +
                ", fecha=" + fecha +
                ", estado='" + estado + '\'' +
                ", item_level_requerido=" + item_level_requerido +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Raid raid = (Raid) o;
        return id_raid != null && id_raid.equals(raid.id_raid);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}