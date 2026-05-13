package com.grupo3.mmorpg.models;

/**
 * Entidad que representa la inscripción de un personaje a una Raid
 * Mapea a la tabla: Inscripcion_Raid
 */
public class InscripcionRaid {
    
    private Long id_inscripcion;
    private Long id_raid;
    private Long id_personaje;
    private String estado;
    private Boolean asistio;
    
    // Constructor sin parámetros
    public InscripcionRaid() {
    }
    
    // Constructor completo
    public InscripcionRaid(Long id_inscripcion, Long id_raid, Long id_personaje,
                           String estado, Boolean asistio) {
        this.id_inscripcion = id_inscripcion;
        this.id_raid = id_raid;
        this.id_personaje = id_personaje;
        this.estado = estado;
        this.asistio = asistio != null ? asistio : false;
    }
    
    // Getters y Setters
    
    public Long getId_inscripcion() {
        return id_inscripcion;
    }
    
    public void setId_inscripcion(Long id_inscripcion) {
        this.id_inscripcion = id_inscripcion;
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
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public Boolean getAsistio() {
        return asistio;
    }
    
    public void setAsistio(Boolean asistio) {
        this.asistio = asistio;
    }
    
    @Override
    public String toString() {
        return "InscripcionRaid{" +
                "id_inscripcion=" + id_inscripcion +
                ", id_raid=" + id_raid +
                ", id_personaje=" + id_personaje +
                ", estado='" + estado + '\'' +
                ", asistio=" + asistio +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InscripcionRaid that = (InscripcionRaid) o;
        return id_inscripcion != null && id_inscripcion.equals(that.id_inscripcion);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}