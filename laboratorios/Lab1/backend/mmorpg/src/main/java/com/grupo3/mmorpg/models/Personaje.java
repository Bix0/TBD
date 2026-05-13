package com.grupo3.mmorpg.models;

/**
 * Entidad que representa un Personaje (avatar) del jugador
 * Mapea a la tabla: Personaje
 */
public class Personaje {
    
    private Long id_personaje;
    private Long id_jugador;
    private Long id_clan;
    private String nombre;
    private String clase;
    private Integer nivel;
    private String faccion;
    private Integer item_level;
    private Integer puntos_merito;
    private String rol_clan;
    
    // Constructor sin parámetros
    public Personaje() {
    }
    
    // Constructor completo
    public Personaje(Long id_personaje, Long id_jugador, Long id_clan, String nombre,
                     String clase, Integer nivel, String faccion, Integer item_level,
                     Integer puntos_merito, String rol_clan) {
        this.id_personaje = id_personaje;
        this.id_jugador = id_jugador;
        this.id_clan = id_clan;
        this.nombre = nombre;
        this.clase = clase;
        this.nivel = nivel;
        this.faccion = faccion;
        this.item_level = item_level;
        this.puntos_merito = puntos_merito != null ? puntos_merito : 0;
        this.rol_clan = rol_clan;
    }
    
    // Getters y Setters
    
    public Long getId_personaje() {
        return id_personaje;
    }
    
    public void setId_personaje(Long id_personaje) {
        this.id_personaje = id_personaje;
    }
    
    public Long getId_jugador() {
        return id_jugador;
    }
    
    public void setId_jugador(Long id_jugador) {
        this.id_jugador = id_jugador;
    }
    
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
    
    public String getClase() {
        return clase;
    }
    
    public void setClase(String clase) {
        this.clase = clase;
    }
    
    public Integer getNivel() {
        return nivel;
    }
    
    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }
    
    public String getFaccion() {
        return faccion;
    }
    
    public void setFaccion(String faccion) {
        this.faccion = faccion;
    }
    
    public Integer getItem_level() {
        return item_level;
    }
    
    public void setItem_level(Integer item_level) {
        this.item_level = item_level;
    }
    
    public Integer getPuntos_merito() {
        return puntos_merito;
    }
    
    public void setPuntos_merito(Integer puntos_merito) {
        this.puntos_merito = puntos_merito;
    }
    
    public String getRol_clan() {
        return rol_clan;
    }
    
    public void setRol_clan(String rol_clan) {
        this.rol_clan = rol_clan;
    }
    
    @Override
    public String toString() {
        return "Personaje{" +
                "id_personaje=" + id_personaje +
                ", nombre='" + nombre + '\'' +
                ", clase='" + clase + '\'' +
                ", nivel=" + nivel +
                ", item_level=" + item_level +
                ", puntos_merito=" + puntos_merito +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Personaje personaje = (Personaje) o;
        return id_personaje != null && id_personaje.equals(personaje.id_personaje);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}