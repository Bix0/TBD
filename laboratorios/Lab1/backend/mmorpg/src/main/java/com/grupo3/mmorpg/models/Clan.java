package com.grupo3.mmorpg.models;

/**
 * Entidad que representa un Clan
 * Mapea a la tabla: Clan
 */
public class Clan {
    
    private Long id_clan;
    private String nombre;
    private Long id_lider;
    
    // Constructor sin parámetros
    public Clan() {
    }
    
    // Constructor completo
    public Clan(Long id_clan, String nombre, Long id_lider) {
        this.id_clan = id_clan;
        this.nombre = nombre;
        this.id_lider = id_lider;
    }
    
    // Getters y Setters
    
    public Long getId_clan() {
        return id_clan;
    }
    
    public void setId_clan(Long id_clan) {
        this.id_clan = id_clan;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public Long getId_lider() {
        return id_lider;
    }
    
    public void setId_lider(Long id_lider) {
        this.id_lider = id_lider;
    }
    
    @Override
    public String toString() {
        return "Clan{" +
                "id_clan=" + id_clan +
                ", nombre='" + nombre + '\'' +
                ", id_lider=" + id_lider +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clan clan = (Clan) o;
        return id_clan != null && id_clan.equals(clan.id_clan);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}