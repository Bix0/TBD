package com.grupo3.mmorpg.models;

/**
 * Entidad que representa un Item (objeto/equipo)
 * Mapea a la tabla: Item
 */
public class Item {
    
    private Long id_item;
    private String nombre;
    private Integer item_lvl;
    private Integer ganancia_dkp;
    
    // Constructor sin parámetros
    public Item() {
    }
    
    // Constructor completo
    public Item(Long id_item, String nombre, Integer item_lvl, Integer ganancia_dkp) {
        this.id_item = id_item;
        this.nombre = nombre;
        this.item_lvl = item_lvl;
        this.ganancia_dkp = ganancia_dkp != null ? ganancia_dkp : 0;
    }
    
    // Getters y Setters
    
    public Long getId_item() {
        return id_item;
    }
    
    public void setId_item(Long id_item) {
        this.id_item = id_item;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public Integer getItem_lvl() {
        return item_lvl;
    }
    
    public void setItem_lvl(Integer item_lvl) {
        this.item_lvl = item_lvl;
    }
    
    public Integer getGanancia_dkp() {
        return ganancia_dkp;
    }
    
    public void setGanancia_dkp(Integer ganancia_dkp) {
        this.ganancia_dkp = ganancia_dkp;
    }
    
    @Override
    public String toString() {
        return "Item{" +
                "id_item=" + id_item +
                ", nombre='" + nombre + '\'' +
                ", item_lvl=" + item_lvl +
                ", ganancia_dkp=" + ganancia_dkp +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return id_item != null && id_item.equals(item.id_item);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}