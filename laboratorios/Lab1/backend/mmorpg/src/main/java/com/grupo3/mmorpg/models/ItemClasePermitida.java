package com.grupo3.mmorpg.models;

/**
 * Entidad que representa las clases permitidas para un item
 * Mapea a la tabla: Item_Clase_Permitida
 */
public class ItemClasePermitida {
    
    private Long id_item;
    private String clase_permitida;
    
    // Constructor sin parámetros
    public ItemClasePermitida() {
    }
    
    // Constructor completo
    public ItemClasePermitida(Long id_item, String clase_permitida) {
        this.id_item = id_item;
        this.clase_permitida = clase_permitida;
    }
    
    // Getters y Setters
    
    public Long getId_item() {
        return id_item;
    }
    
    public void setId_item(Long id_item) {
        this.id_item = id_item;
    }
    
    public String getClase_permitida() {
        return clase_permitida;
    }
    
    public void setClase_permitida(String clase_permitida) {
        this.clase_permitida = clase_permitida;
    }
    
    @Override
    public String toString() {
        return "ItemClasePermitida{" +
                "id_item=" + id_item +
                ", clase_permitida='" + clase_permitida + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemClasePermitida that = (ItemClasePermitida) o;
        return id_item != null && id_item.equals(that.id_item) &&
                clase_permitida.equals(that.clase_permitida);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id_item, clase_permitida);
    }
}