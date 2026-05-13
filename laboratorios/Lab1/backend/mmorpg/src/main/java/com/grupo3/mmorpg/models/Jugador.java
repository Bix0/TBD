package com.grupo3.mmorpg.models;

/**
 * Entidad que representa un Jugador en el sistema MMORPG
 * Mapea a la tabla: Jugador
 */
public class Jugador {
    
    private Long id_jugador;
    private String username;
    private String password;
    private String rol;
    
    // Constructor sin parámetros (necesario para JdbcTemplate/ORM)
    public Jugador() {
    }
    
    // Constructor completo
    public Jugador(Long id_jugador, String username, String password, String rol) {
        this.id_jugador = id_jugador;
        this.username = username;
        this.password = password;
        this.rol = rol;
    }
    
    // Getters y Setters
    
    public Long getId_jugador() {
        return id_jugador;
    }
    
    public void setId_jugador(Long id_jugador) {
        this.id_jugador = id_jugador;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRol() {
        return rol;
    }
    
    public void setRol(String rol) {
        this.rol = rol;
    }
    
    @Override
    public String toString() {
        return "Jugador{" +
                "id_jugador=" + id_jugador +
                ", username='" + username + '\'' +
                ", rol='" + rol + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Jugador jugador = (Jugador) o;
        return id_jugador != null && id_jugador.equals(jugador.id_jugador);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}