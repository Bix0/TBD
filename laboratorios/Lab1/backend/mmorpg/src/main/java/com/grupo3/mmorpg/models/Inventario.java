package com.grupo3.mmorpg.models;

/**
 * Entidad que representa el Inventario de un personaje
 * Mapea a la tabla: Inventario
 */
public class Inventario {
    
    private Long id_inventario;
    private Long id_item;
    private Long id_personaje;
    private Integer cantidad;
    private Boolean equipado;
    
    // Constructor sin parámetros
    public Inventario() {
    }
    
    // Constructor completo
    public Inventario(Long id_inventario, Long id_item, Long id_personaje,
                      Integer cantidad, Boolean equipado) {
        this.id_inventario = id_inventario;
        this.id_item = id_item;
        this.id_personaje = id_personaje;
        this.cantidad = cantidad != null ? cantidad : 1;
        this.equipado = equipado != null ? equipado : false;
    }
    
    // Getters y Setters
    
    public Long getId_inventario() {
        return id_inventario;
    }
    
    public void setId_inventario(Long id_inventario) {
        this.id_inventario = id_inventario;
    }
    
    public Long getId_item() {
        return id_item;
    }
    
    public void setId_item(Long id_item) {
        this.id_item = id_item;
    }
    
    public Long getId_personaje() {
        return id_personaje;
    }
    
    public void setId_personaje(Long id_personaje) {
        this.id_personaje = id_personaje;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public Boolean getEquipado() {
        return equipado;
    }
    
    public void setEquipado(Boolean equipado) {
        this.equipado = equipado;
    }
    
    @Override
    public String toString() {
        return "Inventario{" +
                "id_inventario=" + id_inventario +
                ", id_item=" + id_item +
                ", id_personaje=" + id_personaje +
                ", cantidad=" + cantidad +
                ", equipado=" + equipado +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inventario inventario = (Inventario) o;
        return id_inventario != null && id_inventario.equals(inventario.id_inventario);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}